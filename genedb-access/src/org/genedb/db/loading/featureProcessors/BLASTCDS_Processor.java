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

import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.mapped.Feature;

import org.biojava.bio.seq.StrandedFeature;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 * 
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 * 
 * 
 * @author Adrian Tivey (art)
 */
public class BLASTCDS_Processor extends BaseFeatureProcessor {

    public BLASTCDS_Processor() {
        super(new String[]{}, 
                new String[]{}, 
                new String[]{}, 
                new String[]{}, 
                new String[]{});
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
   
        return; // FIXME
        // TODO - How to store these
        
//        logger.debug("Entering processing for BLASTCDS");
//        Location loc = f.getLocation();
//        Annotation an = f.getAnnotation();
//        short strand = (short)f.getStrand().getValue();
//        String systematicId = MiningUtils.getProperty("temporary_systematic_id", an, "");
//        
//        Feature blast_cds = this.featureUtils.createFeature("match", systematicId,this.organism);
//        this.sequenceDao.persist(blast_cds);
//
//        FeatureLoc blastFl = this.featureUtils.createLocation(parent,blast_cds,loc.getMin(),loc.getMax(),strand);
//        this.sequenceDao.persist(blastFl);
//
//        FeatureProp hseqname = createFeatureProp(blast_cds, an, "hseqname", "hseqname", CV_GENEDB);
//        this.sequenceDao.persist(hseqname);
//        
//        FeatureProp score = createFeatureProp(blast_cds, an, "score", "score", CV_GENEDB);
//        this.sequenceDao.persist(score);
//        
//        FeatureProp pvalue = createFeatureProp(blast_cds, an, "p_value", "p_value", CV_GENEDB);
//        this.sequenceDao.persist(pvalue);
//        
//        FeatureProp percent_id = createFeatureProp(blast_cds, an, "percent_similarity", "percent_id", CV_GENEDB);
//        this.sequenceDao.persist(percent_id);
        
        //FeatureProp cstring = createFeatureProp(blast_cds, an, "cigar_string", "cigar_string", CV_MISC);
        //this.sequenceDao.persist(cstring);

    }


    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }
    
}
