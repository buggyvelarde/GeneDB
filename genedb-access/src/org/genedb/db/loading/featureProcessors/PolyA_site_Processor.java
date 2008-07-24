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
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_GENE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_PSU_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;

import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.utils.LocationUtils;
import org.gmod.schema.utils.StrandedLocation;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.Location;

import java.util.List;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 *
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 *
 *
 * @author Adrian Tivey (art)
 */
public class PolyA_site_Processor extends BaseFeatureProcessor {

    public PolyA_site_Processor() {
        super(new String[]{},
                new String[]{},
                new String[]{QUAL_SYS_ID, QUAL_DB_XREF, QUAL_D_PSU_DB_XREF},
                new String[]{QUAL_D_GENE, QUAL_NOTE},
                new String[]{});
    }

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature feat, int offset) {
        String type = "polyA_site";
        logger.warn("Trying to process polyA_site");

        Annotation an = feat.getAnnotation();
        Location loc = feat.getLocation().translate(offset);
        StrandedLocation location = LocationUtils.make(loc, feat.getStrand());
        Feature above = null;
        above = tieFeatureByNameInQualifier("systematic_id", parent, feat, an, location);
        if (above == null) {
            above = tieFeatureByNameInQualifier("temporary_systematic_id", parent, feat, an, location);
        }
        if (above == null) {
            above = tieFeatureByNameInQualifier("gene", parent, feat, an, location);
        }
        if (above == null) {
            above = tieToFeatureByLocation("polyA_site", "polyA_signal_sequence", parent, feat, location);

        }
        if (above == null) {
            above = tieToFeatureByLocation("polyA_site", "three_prime_UTR", parent, feat, location);

        }
        if (above == null) {
            logger.warn("Got all the way through and not handled");
            return;
        }
        storeFeature(type, above, parent, feat, loc);

        // TODO Other properties/db_xrefs etc

    }

    private Feature tieToFeatureByLocation(String type, String aboveType, Feature parent, StrandedFeature feat, StrandedLocation loc) {

        // Try and store via location

        Strand strand = feat.getStrand();
        int leftMost = loc.getMin()-1;
        int rightMost = loc.getMax();
        logger.warn("Trying to tie polyA_site via location min='"+leftMost+"' max='"+rightMost+"' strand='"+strand.getToken()+"'");

        List<Feature> features = sequenceDao.getFeaturesByRange(leftMost, rightMost, strand.getValue(), parent, aboveType);

        if (features.size() == 0) {
            logger.warn("Looking to place polyA_site by location but can't find polyA_signal");
            return null;
        }
        if (features.size() > 1) {
            logger.warn("Looking to place polyA_site by location but can't find unique polyA_signal ('"+features.size()+"' found)");
            return null;
        }
        return features.get(0);

    }


    private void storeFeature(String type, Feature above, Feature parent, StrandedFeature feat, Location loc) {

       String utrName = this.gns.get5pUtr(above.getUniqueName(), 0);
//       org.gmod.schema.mapped.Feature utr = this.featureUtils.createFeature(type, utrName, this.organism);
//       FeatureRelationship utrFr = featureUtils.createRelationship(utr, above, REL_PART_OF, 0);
//       FeatureLoc utrFl = featureUtils.createLocation(parent, utr,
//                loc.getMin()-1, loc.getMax(), (short)feat.getStrand().getValue());
//        sequenceDao.persist(utr);
//        sequenceDao.persist(utrFr);
//        sequenceDao.persist(utrFl);

    }


    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }

}
