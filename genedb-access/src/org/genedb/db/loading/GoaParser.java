/*
 * Copyright (c) 2002 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

/**
 * This reads a GO association file and stores GO annotation with
 * the corresponding "gene"
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
package org.genedb.db.loading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoaParser implements CharSVParsingListener {


    protected static final Log logger = LogFactory.getLog(GoaParser.class);
    private Map cache;
    private int count;
    private boolean mungeCase;
    private Map uniprotSysId;
    private boolean discardDupNodes;

    public GoaParser(Reader input, Map cache, boolean discardDupNodes, 
            	boolean mungeCase, Map uniprotSysId) throws IOException {
        count = 0;
        this.cache = cache;
        this.mungeCase = mungeCase;
        this.uniprotSysId = uniprotSysId;
        this.discardDupNodes = discardDupNodes;
        CharSVParser parser = new CharSVParser(input, "\\t", false, 0, "#" );

        parser.addCharSVParsingListener ( this );

        parser.go();

    }
    


    public int getCount() {
        return count;
    }

    public void rowParsed(CharSVParsingEvent ev) {

        count++;
        String[] terms = ev.getTerms();

        if (terms.length < 14) {
            System.err.println ("WARN: Not enough terms in rowParsed - only got "+terms.length);
            return;
        }

        String geneId = terms[1].trim();
        String origId = geneId;

        if ( mungeCase && geneId.endsWith("C") ) {
            geneId = geneId.substring(0, geneId.length() - 1 ) + "c";
        }

        if (uniprotSysId != null) {
            // geneId is actually a Uniprot Id!
            // Can we match up?
            geneId = (String) uniprotSysId.get(geneId);
            if (geneId != null) {
                //System.err.println("^^^I've got a match for "+origId+" with "+geneId);
            }
        }
        
        // TODO Need to look up id here

        GoInstance gp = new GoInstance();

        String qualifier = terms[3];
        if ( qualifier != null && qualifier.length()>0) {
            gp.addQualifier(qualifier);
        }

        String id = terms[4];
        if (!id.startsWith("GO:")) {
            System.err.println("WARN: GO id doesn't start with GO: *"+id+"*");
            return;
        }
        gp.setId( terms[4].substring(3) );

        String name = GoDictionary.getName( gp.getId() );
        if ( name != null ) {
            gp.setName( name );
        }

        gp.setRef(terms[5]);
        gp.setEvidence(GoEvidenceCode.valueOf(terms[6].trim()));
        gp.setWithFrom(terms[7]);

        // Aspect is not mandatory but always used in PSU
        String aspect = terms[8].substring(0,1).toUpperCase();

        if( "P".equals(aspect) ){
            gp.setSubtype("process");
        } else {
            if( "C".equals(aspect) ){
                gp.setSubtype("component");
            } else {
                if( "F".equals(aspect) ){
                    gp.setSubtype("function");
                } else {
                    logger.warn("WARN: Unexpected aspect *"+gp.getAspect()+"* in GO association file");
                    return;
                }
            }
        }
        
        gp.setDate(terms[13]);
        
        if (terms.length > 13) {
        		gp.setAttribution(terms[14]);
        }

        // Need to check that new Go assoc. isn't just parent of existing one
        if (discardDupNodes) {
            Set children = GoDictionary.getChildren(gp.getId());
       
            List classifications = gene.getGOInsts();
            if ( classifications == null ) {
                //System.err.println("Adding "+ gp.getId()+" to "+gene.getId()+" as no current classifications");
                gene.addGOInst(gp);
                return;
            }
            Iterator it = classifications.iterator();
            while ( it.hasNext()) {
                GoInstance c =  (GoInstance) it.next();
                if ( gp.getId().equals( c.getId()) ) {
                	//System.err.println("Rejecting "+ gp.getId()+" for "+gene.getId()+" as matches existing annotation");
                	return;
                }
                if (children.contains( c.getId()) ) {
                	//System.err.println("Rejecting "+ gp.getId()+" for "+gene.getId()+" as matches a child");
                	return;
                }
            }
            //System.err.println("Adding "+ gp.getId()+" to "+gene.getId()+" as no child clash");
        }
        gene.addGOInst(gp);

    }
}
