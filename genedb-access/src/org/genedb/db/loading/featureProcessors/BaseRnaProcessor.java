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

import static org.genedb.db.loading.EmblQualifiers.QUAL_CURATION;
import static org.genedb.db.loading.EmblQualifiers.QUAL_C_CURATION;
import static org.genedb.db.loading.EmblQualifiers.QUAL_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_COLOUR;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_FASTA_FILE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_GENE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_PSU_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_EVIDENCE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_GO;
import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_OBSOLETE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIMARY;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIVATE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRODUCT;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PSEUDO;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYNONYM;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;
import static org.genedb.db.loading.EmblQualifiers.QUAL_TEMP_SYS_ID;

import org.genedb.db.loading.MiningUtils;
import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;


/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 * 
 * 
 * @author Chinmay Patel (cp2)
 */
public abstract class BaseRnaProcessor extends BaseFeatureProcessor {
    
    
    public BaseRnaProcessor() {
        super(new String[]{}, 
                new String[]{}, 
                new String[]{QUAL_SYS_ID, QUAL_TEMP_SYS_ID, QUAL_PRIMARY, QUAL_D_COLOUR, 
                QUAL_PSEUDO, QUAL_D_FASTA_FILE},
                new String[]{QUAL_EVIDENCE, QUAL_C_CURATION, QUAL_OBSOLETE, QUAL_DB_XREF,
                QUAL_D_PSU_DB_XREF, QUAL_PRODUCT, QUAL_D_GENE,QUAL_NOTE,QUAL_SYNONYM, QUAL_GO, 
                QUAL_CURATION, QUAL_OBSOLETE},
                new String[]{QUAL_D_FASTA_FILE});
    }

    protected void processRna(Feature parent, StrandedFeature f, String type, int offset) {
        logger.debug("Entering processing for "+type);
        Location loc = f.getLocation().translate(offset);
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();
        String systematicId = findName(an, type);
        if (systematicId == null) {
            System.err.println("Skipping '"+type+"' as no id found");
            return;
        }
        
        org.gmod.schema.sequence.Feature rna = this.featureUtils.createFeature(type, systematicId, this.organism);
        sequenceDao.persist(rna);
        //FeatureRelationship trnaFr = featureUtils.createRelationship(mRNA, REL_DERIVES_FROM);
        FeatureLoc rnaFl = this.featureUtils.createLocation(parent,rna,loc.getMin()-1,loc.getMax(),
                                                        strand);
        sequenceDao.persist(rnaFl);
        //featureLocs.add(pepFl);
        //featureRelationships.add(pepFr);
        
        FeatureProp fp = createFeatureProp(rna, an, "colour", "colour", CV_MISC);
        sequenceDao.persist(fp);
        createFeaturePropsFromNotes(rna, an, QUAL_NOTE, MISC_NOTE);
        createFeaturePropsFromNotes(rna, an, QUAL_CURATION, MISC_CURATION);
        createFeaturePropsFromNotes(rna, an, QUAL_PRIVATE, MISC_PRIVATE);
        createDbXRefs(rna, an);
    }
    
    protected String findName(Annotation an, String type) {
        String[] keys = {QUAL_SYS_ID, "temporary_systematic_id", "gene"};
        for (String key : keys) {
            if (an.containsProperty(key)) {
                return MiningUtils.getProperty(key, an, null);
            }
        }
        //throw new RuntimeException("No systematic id found for "+type+" entry");
        return null;
    }
    
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SECOND;
    }

}