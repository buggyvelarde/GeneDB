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

import org.genedb.db.loading.EmblQualifiers;
import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;

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
public class CDS_motif_Processor extends BaseFeatureProcessor {

    public CDS_motif_Processor() {
        super(new String[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                new String[]{});
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
        logger.debug("Entering processing for CDS_motif");
        Location loc = f.getLocation();
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();
        String systematicId = "cdsMotif"+loc.getMin()+"-"+loc.getMax();

        //Feature cdsMotif = this.featureUtils.createFeature("polypeptide_domain", systematicId,
        //        this.organism);
        //this.sequenceDao.persist(cdsMotif);

        //FeatureLoc trnaFl = featureUtils.createLocation(parent,cdsMotif,loc.getMin(),loc.getMax(),
        //                                                strand);
        //this.sequenceDao.persist(trnaFl);
        //createFeaturePropsFromNotes(cdsMotif, an, EmblQualifiers.QUAL_NOTE, MISC_NOTE, 0);
    }


    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }

}
