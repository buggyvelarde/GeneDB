/*
 * Copyright (c) 2006 Genome Research Limited.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB. If not, write to the Free
 * Software Foundation Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307
 * USA
 */

/**
 * 
 * 
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
package org.genedb.db.loading.featureProcessors;

import static org.genedb.db.loading.EmblQualifiers.QUAL_D_COLOUR;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_GENE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;

import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 * 
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 * 
 * 
 * FT   cDNA_match      complement(join(405099..405380,406082..406193))
 * FT                   /note="source  czapPFDd2.1, Debopam Chakrabarti"
 * FT                   /note="best BLAT alignment"
 * FT                   /note="percentage identity=100"
 * FT                   /method="BLAT"
 * FT                   /note="query N97834"
 * @author Adrian Tivey (art)
 */
public class cDNA_match_Processor extends BaseFeatureProcessor {

    private static int numProcessed = 0;
    
    public cDNA_match_Processor() {
        super(new String[]{}, 
                new String[]{}, 
                new String[]{},
                new String[]{QUAL_NOTE, "comment"}, 
                new String[]{});
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
    	
        numProcessed = numProcessed + 5;
        //logger.info("Entering processing for repeat '"+numProcessed+"'");
        Location loc = f.getLocation();
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();
        
        String soType = "cDNA_match";
        
        String systematicId = this.organism.getAbbreviation() + "_cDNAMatch_" + numProcessed;
        
        if (!cvDao.existsNameInOntology(soType, CV_SO)) {
        	logger.warn("Can't create repeat feature of type '"+soType+"' at location '"+loc+"'");
        } else {
            if (sequenceDao.getFeaturesByUniqueName(systematicId).size() == 0) {
            	Feature match = this.featureUtils.createFeature(soType, systematicId,
            			this.organism);
            	sequenceDao.persist(match);
            	FeatureLoc repeatFl = featureUtils.createLocation(parent,match,loc.getMin()-1,loc.getMax(),
            			strand);
            	sequenceDao.persist(repeatFl);
            	int rank = createFeaturePropsFromNotes(match, an, QUAL_NOTE, MISC_NOTE, 0);
            	createFeaturePropsFromNotes(match, an, "comment", MISC_NOTE, rank);
            	//FeatureProp fp = createFeatureProp(repeat, an, "colour", "colour", CV_MISC);
            }
        }
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }

}
