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
import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;

import org.genedb.db.loading.EmblQualifiers;
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
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 * 
 * 
 * @author Adrian Tivey (art)
 */
public class LTR_Processor extends BaseFeatureProcessor {

    public LTR_Processor() {
        super(new String[]{},
                new String[]{},
                new String[]{QUAL_D_COLOUR},
                new String[]{QUAL_NOTE},
                new String[]{});
        
		handledQualifiers = new String[]{"LTR:colour", "LTR:note"};
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
        // TODO is LTR a SO feature?
        logger.debug("Entering processing for long_terminal_repeat (LTR)");
        Location loc = f.getLocation();
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();
        String systematicId = "LTR"+loc.getMin()+"-"+loc.getMax(); 
        
        org.gmod.schema.sequence.Feature ltr = this.featureUtils.createFeature("long_terminal_repeat", systematicId,
                this.organism);
        sequenceDao.persist(ltr);

        FeatureLoc ltrFl = featureUtils.createLocation(parent,ltr,loc.getMin()-1,loc.getMax(),
                                                        strand);
        sequenceDao.persist(ltrFl);
      
        FeatureProp fp = createFeatureProp(ltr, an, "colour", "colour", CV_GENEDB);
        sequenceDao.persist(fp);
        createFeaturePropsFromNotes(ltr, an, EmblQualifiers.QUAL_NOTE, MISC_NOTE);
    }


    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }
    
}
