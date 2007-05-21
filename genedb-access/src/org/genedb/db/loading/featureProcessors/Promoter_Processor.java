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
import org.gmod.schema.sequence.FeatureRelationship;

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
 * @author Adrian Tivey (art)
 */
public class Promoter_Processor extends BaseFeatureProcessor {
    
    public Promoter_Processor() {
        super(new String[]{},
                new String[]{},
                new String[]{QUAL_D_GENE},
                new String[]{QUAL_NOTE, QUAL_D_COLOUR},
                new String[]{});
    }

    private static int promoterCount = 1;
    
    @Override
    public void processStrandedFeature(final Feature parent, final StrandedFeature feat, final int offset) {
        
        //logger.warn("Trying to process "+type);
        
        final Annotation an = feat.getAnnotation();
        final Location loc = feat.getLocation().translate(offset);
        Feature above = null;
        above = tieFeatureByNameInQualifier("gene", parent, feat, an, loc);
        if (above == null) {
            logger.warn("Unable to tie promoter to a gene");
            return;
        }


        
        // TODO Other properties/db_xrefs etc
        // FIXME May have multiple promoters
        // FIXME Naming wrong
        // FIXME related to transcript or gene
        final Feature promoter = this.featureUtils.createFeature("promoter", "promoter:"+(promoterCount++)+":"+above.getUniqueName(), this.organism);
        final FeatureRelationship promoterFr = featureUtils.createRelationship(promoter, above, REL_PART_OF, promoterCount-1);
        final FeatureLoc promoterFl = featureUtils.createLocation(parent, promoter, 
                loc.getMin()-1, loc.getMax(), (short)feat.getStrand().getValue());

        sequenceDao.persist(promoter);
        sequenceDao.persist(promoterFr);
        sequenceDao.persist(promoterFl);
        
        createFeaturePropsFromNotes(promoter, an, QUAL_NOTE, MISC_CURATION);
        
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.FIFTH;
    }


}
