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
public class Repeat_region_Processor extends BaseFeatureProcessor {

    private static int numProcessed = 0;

    public Repeat_region_Processor() {
        super(new String[]{},
                new String[]{},
                new String[]{QUAL_D_COLOUR},
                new String[]{QUAL_NOTE, QUAL_D_GENE},
                new String[]{});
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {

        numProcessed = numProcessed + 5;
        //logger.info("Entering processing for repeat '"+numProcessed+"'");
        Location loc = f.getLocation();
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();

        String soType = "repeat_region";
        if (an.containsProperty("rpt_type")) {
            String repeatType = (String) an.getProperty("rpt_type");
            soType = (repeatType == null) ? "repeat_region" : repeatType;
            if ("direct".equals(repeatType)) {
                soType = "direct_repeat";
            }
        }




        /*String systematicId = "repeat"+(loc.getMin()-1)+"-"+loc.getMax();
        if (f.getStrand().equals(StrandedFeature.NEGATIVE)) {
        	systematicId = systematicId + "_rev"; // FIXME Temp hack
        }
        if (an.containsProperty("systematic_id")) {
            systematicId = (String) an.getProperty("systematic_id");
        }*/
        String systematicId = this.organism.getAbbreviation() + "_REP_" + Integer.toString(numProcessed);

        if (!cvDao.existsNameInOntology(soType, CV_SO)) {
        	logger.warn("Can't create repeat feature of type '"+soType+"' at location '"+loc+"'");
        } else {
            if (sequenceDao.getFeaturesByUniqueNamePattern(systematicId).size() == 0) {
            	//Feature repeat = this.featureUtils.createFeature(soType, systematicId,
            	//		this.organism);
            	//sequenceDao.persist(repeat);
            	//FeatureRelationship trnaFr = featureUtils.createRelationship(mRNA, REL_DERIVES_FROM);
            	//FeatureLoc repeatFl = featureUtils.createLocation(parent,repeat,loc.getMin()-1,loc.getMax(),
            	//		strand);
            	//sequenceDao.persist(repeatFl);
            	//featureLocs.add(pepFl);
            	//featureRelationships.add(pepFr);
            	//createFeaturePropsFromNotes(repeat, an, QUAL_NOTE, MISC_NOTE, 0);
            	//FeatureProp fp = createFeatureProp(repeat, an, "colour", "colour", CV_MISC);
            	//this.daoFactory.persist(fp);
            	//createFeaturePropsFromNotes(repeat, an, MISC_NOTE);
            }
        }
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }

}
