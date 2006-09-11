/*
 * Copyright (c) 2002 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version
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
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adian Tivey</a>
 */
package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;

import org.gmod.schema.cv.CvTerm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This reads the GO data from annotation on a BaseRNA object. It copes with both new 
 * and old-style qualifiers
 * 
 * @author art
 *
 */
public class GoParser {
    
    protected static final Log logger = LogFactory.getLog(GoParser.class);
    private CvDao cvDao;
    
    
    public List<GoInstance> getAllGoTermsFromAnnotation(Annotation an) {
	List<GoInstance> ret = new ArrayList<GoInstance>();
	ret.addAll(getNewStyleGoTerm(an));
	ret.addAll(getOldStyleGoTerms(an, "function"));
	ret.addAll(getOldStyleGoTerms(an, "component"));
	ret.addAll(getOldStyleGoTerms(an, "process"));
	return ret;
    }


    // FIXME - Tidy validation parsing
    public List<GoInstance> getNewStyleGoTerm(Annotation an) {
	List<GoInstance> ret = new ArrayList<GoInstance>();
	List<String> terms = MiningUtils.getProperties("GO", an );
	if ( terms ==null) {
	    return ret;
	}
	for (String term : terms) {
	    GoInstance go = new GoInstance();
	    go.setFromEMBL(true);
	    int valid =0;
	    String realName = null;
	    String curatorName = null;
	    String evidence = null;

	    try {
		String[] sections = term.split(";");
		for (int j=0; j<sections.length ; j++) {
		    String expression = sections[j].trim();
		    int index = expression.indexOf("=");
		    if ( index == -1) {
			System.err.println("WARN: Got naked expression in GO term:" + term
				+ ": (maybe need qualifier=?). Trying to store anyway but please fix");
			go.addQualifier( expression );
			continue;
		    }
		    String key = expression.substring(0,index).trim();
		    String value = expression.substring(index+1).trim();

		    if ("qualifier".equals(key)) {
			go.addQualifier( value );
		    }

		    if ("aspect".equals(key)) {
                if (value.length() > 1) {
                    switch(Character.toUpperCase(value.charAt(0))) {
                        case 'F':
                            go.setSubtype( "function" );
                            break;
                        case 'C':
                            go.setSubtype( "component" );
                            break;
                        case 'P':
                            go.setSubtype( "process" );
                            break;
                        default:
                            logger.fatal("Got an invalid aspect in '"+term+"'");
                        throw new ParsingException();
                    }
		        } else {
		            //System.err.println("WARN: Aspect key found but no value");
                }
		    }

		    if ("GOid".equals(key)) {
			int bracket =  value.indexOf("(");
			if ( bracket != -1) {
			    value = value.substring(0,index).trim();
			}
			if ( value.length()==10) {
                value = value.substring(3);
            }
            if (value.length() != 7) {
			    System.err.println("WARN: GO id looks wrong: "+value);
			    continue;
			}
            go.setId(value);
			String name = lookUpGoName( value);
			go.setName( name );
			realName = name;
			if ( curatorName != null) {
			    if (!curatorName.equals(realName)) {
				System.err.println("WARN: GO:"+go.getId()+" has a mismatch between curator '"+curatorName+"' and real '"+realName+"' description");
			    }
			}
			valid+=1;
		    }

		    if ("term".equals(key)) {
			curatorName = value.trim();
			if ( realName != null) {
			    if (!curatorName.equals(realName)) {
				System.err.println("WARN: "+go.getId()+" has a mismatch between curator ("+curatorName+") and real ("+realName+") description");
			    }
			}
		    }

		    if ("date".equals(key)) {
			go.setDate( value );
		    }

		    if ("evidence".equals(key)) {
			go.setEvidence( GoEvidenceCode.valueOf(value));
			valid+=2;
			evidence=value;
		    }

		    if ("with".equals(key)) {
			go.setWith( value );
		    }

		    if ("from".equals(key)) {
			go.setFrom( value );
		    }

		    if ("db_xref".equals(key)) {
			go.setRef( value );
			valid+=4;
		    }
		}
		go.validate();
	    }
	    catch (IllegalArgumentException exp) {
		logger.fatal("GO: Illegal argument in '"+term+ "' - " + exp.getMessage());
		throw new ParsingException(exp);
	    }
	    // TODO Move following to validity check
	    //	if (GOInst.EV_IC.equals(evidence) && !fromSet) {
	    //	LogUtils.bprintln("WARN:GO: Ignoring invalid field due to missing from field "
	    //	+"with IC evidence code in "+rna.getId());
	    //	continue;
	    //	}
	    //	if ((GOInst.EV_IGI.equals(evidence)  || GOInst.EV_IPI.equals(evidence))
	    //	&& !withSet) {
	    //	LogUtils.bprintln("WARN:GO: Ignoring invalid field due to missing with field with "
	    //	+ evidence + " evidence code in " + rna.getId());
	    //	continue;
	    //	}
	    //	if (GOInst.EV_ISS.equals(evidence) && !withSet) {
	    //	LogUtils.bprintln("WARN:GO: Ignoring invalid field due to missing with field with "
	    //	+ evidence + " evidence code in " + rna.getId());
	    //	}
	    // Check qualification is valid
	    //	if (!c.isQualifierPSUValid()) {
	    //	System.err.println("WARN:GO: Ignoring invalid field due to missing/invalid qualifier value ("+c.getQualifierString()+") in "
	    //	+ rna.getId());
	    //	continue;
	    //	}
	    if (valid!=7 && !(valid==3 && GoEvidenceCode.ND.equals(evidence))) {
		logger.fatal("WARN:GO: Ignoring incomplete or invalid field: (count "
			+valid+") "+term+"'");
		//throw new ParsingException();
        continue;
	    }
        ret.add(go);
	}
	return ret;
    }


    private String lookUpGoName(String id) {
        CvTerm cvTerm = cvDao.getGoCvTermByAccViaDb(id);
        if (cvTerm == null) {
            return null;
        }
        return cvTerm.getName();
    }



    protected List<GoInstance> getOldStyleGoTerms(Annotation an, String category) {
	List<String> terms = MiningUtils.getProperties( "GO_" + category, an );
	List<GoInstance> ret = new ArrayList<GoInstance>();
	if (terms != null) {
	    for (int i = 0 ; i <terms.size() ; i++ ) {
		try {
		    if(terms.get(i) == null || "".equals(terms.get(i)) ){
			logger.fatal("WARN: Ignoring empty GO field: text= '"+terms.get(i)+"'");
			throw new ParsingException();
		    }

		    GoInstance go = new GoInstance();
		    go.setSubtype( category );
		    go.setFromEMBL(true);




		    String line = terms.get(i).trim();
		    String[] parts = line.split(";");

		    if (parts.length<4) {
			System.err.println("Not enough sections for old-style go:"+line);
		    }
		    for (int j=0; j < parts.length; j++) {
			parts[j] = parts[j].trim();
		    }

		    String id = parts[0];

		    int semicolon = line.indexOf(";");
		    String rest = null;

		    if ( semicolon != -1  ) {
			rest = line.substring(semicolon + 1);
			line = line.substring(0, semicolon);
		    }
		    int comma = id.indexOf("(");
		    if ( comma != -1 ) {
			id = id.substring(0, comma);
		    }
		    id = id.trim();
		    go.setId( id );
		    String name = lookUpGoName( id);
		    go.setName(name);
		    go.setWith("none");
		    go.setFrom("none");
		    if ( rest == null ) {
			//c.setRef( "none" );
			go.setEvidence(GoEvidenceCode.IEA);
		    } else {
			semicolon = rest.indexOf(";");
			if ( semicolon != -1 ) {
			    go.setRef( rest.substring(semicolon + 1).trim() );
			    go.setEvidence(GoEvidenceCode.valueOf(rest.substring(0, semicolon).trim()));
			} else {
			    go.setEvidence(GoEvidenceCode.valueOf(rest.trim()));
			}
		    }
		    ret.add(go);
		}
		catch (IllegalArgumentException exp) {
		    System.err.println("Skipping GO term because of exception");
		    exp.printStackTrace();
		}
	    }
	}
	return ret;
    }

    // ------------------------------------------------------------------------
    
    private int count;
//    private boolean mungeCase;

    public Map<String, List<GoInstance>> parseGoAssociationFile(Reader input, boolean discardDupNodes) throws IOException {
        count = 0;
        //this.mungeCase = mungeCase;
        final Map<String, List<GoInstance>> ret = new HashMap<String, List<GoInstance>>();
        CharSVParser parser = new CharSVParser(input, "\t", false, 0, "#" );
        
        parser.addCharSVParsingListener (new CharSVParsingListener() {
            public void rowParsed(CharSVParsingEvent ev) {
                count++;
                GoInstance go = new GoInstance();
                String systematicId = parseStringArray(ev.getTerms(), go);
                CollectionUtils.addItemToMultiValuedMap(systematicId, go, ret);
            }
        });

        parser.go();
        return ret;
    }
    


    public int getCount() {
        return count;
    }

    public String parseStringArray(String[] terms, GoInstance go) {

        count++;

        if (terms.length < 14) {
            logger.fatal("Not enough terms in rowParsed - only got '"+terms.length+"'");
            throw new ParsingException();
        }

        String systematicId = terms[1].trim();

        // TODO pombe hack - still necessary?
//        if ( mungeCase && geneId.endsWith("C") ) {
//            geneId = geneId.substring(0, geneId.length() - 1 ) + "c";
//        }


        String qualifier = terms[3];
        if ( qualifier != null && qualifier.length()>0) {
            go.addQualifier(qualifier);
        }

        String id = terms[4];
        if (!id.startsWith("GO:")) {
            logger.fatal("GO id doesn't start with GO: '"+id+"'");
            throw new ParsingException();
        }
        go.setId( terms[4].substring(3) );

        String name = lookUpGoName( go.getId());
        if ( name != null ) {
            go.setName( name );
        }

        go.setRef(terms[5]);
        go.setEvidence(GoEvidenceCode.valueOf(terms[6].trim()));
        go.setWithFrom(terms[7], go.getEvidence());

        // Aspect is not mandatory but always used in PSU
        String aspect = terms[8].substring(0,1).toUpperCase();

        if( "P".equals(aspect) ){
            go.setSubtype("process");
        } else {
            if( "C".equals(aspect) ){
                go.setSubtype("component");
            } else {
                if( "F".equals(aspect) ){
                    go.setSubtype("function");
                } else {
                    logger.fatal("WARN: Unexpected aspect '"+go.getAspect()+"' in GO association file");
                    throw new ParsingException();
                }
            }
        }
        
        go.setDate(terms[13]);
        
        go.setAttribution(terms[14]);

        return systematicId;

    }


    public void setCvDao(CvDao cvDao) {
    
        this.cvDao = cvDao;
    }

}
