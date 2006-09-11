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

import static org.genedb.db.loading.EmblQualifiers.QUAL_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_COLOUR;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_FASTA_FILE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_GENE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_PSU_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_GO;
import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIMARY;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRODUCT;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;

import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.sequence.Feature;

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
public class Misc_feature_Processor extends BaseFeatureProcessor {

    public Misc_feature_Processor() {
        super(new String[]{}, 
                new String[]{}, 
                new String[]{QUAL_D_COLOUR, "algorithm", "cleavage", "type", "partial",
                QUAL_PRIMARY, QUAL_SYS_ID,QUAL_PRIMARY, "label"}, 
                new String[]{QUAL_NOTE, QUAL_D_GENE, QUAL_PRODUCT, "coord", "id", 
                QUAL_D_PSU_DB_XREF, QUAL_DB_XREF, QUAL_GO, "mutation", "citation"},
                new String[]{QUAL_D_FASTA_FILE});
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
        // TODO Write this
//        logger.debug("Entering processing for repeat");
//        Location loc = f.getLocation();
//        Annotation an = f.getAnnotation();
//        short strand = (short)f.getStrand().getValue();
//        String systematicId = "repeat"+loc.getMin()+"-"+loc.getMax(); 
//        
//        org.genedb.db.jpa.Feature repeat = this.featureUtils.createFeature("repeat", systematicId,
//                this.organism);
//        this.daoFactory.persist(repeat);
//        //FeatureRelationship trnaFr = featureUtils.createRelationship(mRNA, REL_DERIVES_FROM);
//        FeatureLoc trnaFl = featureUtils.createLocation(parent,repeat,loc.getMin(),loc.getMax(),
//                                                        strand);
//        this.daoFactory.persist(trnaFl);
//        //featureLocs.add(pepFl);
//        //featureRelationships.add(pepFr);
//        
//        FeatureProp fp = createFeatureProp(repeat, an, "colour", "colour", CV_MISC);
//        this.daoFactory.persist(fp);
//        createFeaturePropsFromNotes(repeat, an, MISC_NOTE);

    }


    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }
    
}
