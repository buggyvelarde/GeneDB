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

import org.genedb.db.hibernate3gen.FeatureLoc;
import org.genedb.db.hibernate3gen.FeatureRelationship;
import org.genedb.db.loading.MiningUtils;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
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
public abstract class BaseUtrProcessor extends BaseFeatureProcessor {
    
    public BaseUtrProcessor() {
        super(new String[]{}, new String[]{}, new String[]{"db_xref"}, new String[] {"gene", "note"});
    }

    protected void processUTR(String type, org.genedb.db.jpa.Feature parent, StrandedFeature feat) {
        // TODO Doesn't cope with splicing
        Annotation an = feat.getAnnotation();
        Location loc = feat.getLocation();
        if (an.containsProperty("systematic_id")) {
            // Hopefully the systematic name of a gene
            String sysId = (String) an.getProperty("systematic_id");
            org.genedb.db.jpa.Feature gene = daoFactory.getFeatureDao().findByUniqueName(sysId);
            if (gene != null) {
                org.genedb.db.jpa.Feature utr = this.featureUtils
                .createFeature(type, this.gns.get5pUtr(sysId, 0), this.organism);
                FeatureRelationship utrFr = featureUtils.createRelationship(
                        utr, gene, REL_PART_OF);
                System.err.println("REL_PART_OF is '"+REL_PART_OF+"'");
                FeatureLoc utrFl = featureUtils.createLocation(parent, utr, 
                        loc.getMin(), loc.getMax(), (short)feat.getStrand().getValue());
                daoFactory.persist(utr);
                daoFactory.persist(utrFr);
                daoFactory.persist(utrFl);
            } else {
                // TODO Complain bitterly
            }
        } else {
            if (an.containsProperty("gene")) {
                logger.debug("No systematic id found for "+type+", but found /gene");
                //TODO check if only one result else complain bitterly
                String name = MiningUtils.getProperty("gene", an, null);
                List<org.genedb.db.jpa.Feature> genes = daoFactory.getFeatureDao().findByAnyCurrentName(name);
                if (genes != null && genes.size()==1) {
                    org.genedb.db.jpa.Feature gene = genes.get(0);
                    // FIXME - Always generating 5' name
                    String utrName = this.gns.get5pUtr(gene.getUniquename(), 0);
                    if ("three_prime_UTR".equals(type)) {
                        utrName = this.gns.get3pUtr(gene.getUniquename(), 0);
                    }
                    org.genedb.db.jpa.Feature utr = this.featureUtils
                    .createFeature(type, utrName, this.organism);
                    FeatureRelationship utrFr = featureUtils.createRelationship(
                            utr, gene, REL_PART_OF);
                    System.err.println("REL_PART_OF is '"+REL_PART_OF+"'");
                    FeatureLoc utrFl = featureUtils.createLocation(parent, utr, 
                            loc.getMin(), loc.getMax(), (short)((StrandedFeature)feat).getStrand().getValue());
                    daoFactory.persist(utr);
                    daoFactory.persist(utrFr);
                    daoFactory.persist(utrFl);
                }
            }
        }
    }

}
