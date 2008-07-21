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

import org.gmod.schema.feature.FivePrimeUTR;
import org.gmod.schema.feature.ThreePrimeUTR;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.utils.StrandedLocation;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.StrandedFeature.Strand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
        super(new String[]{},
                new String[]{},
                new String[]{QUAL_SYS_ID, QUAL_DB_XREF, QUAL_D_PSU_DB_XREF},
                new String[]{QUAL_D_GENE, QUAL_NOTE},
                new String[]{});
    }

    protected void processUTR(String type, Feature parent, StrandedFeature feat, int offset) {
        
        //logger.warn("Trying to process "+type);
        
        Annotation an = feat.getAnnotation();
        StrandedLocation loc = feat.getLocation().translate(offset);
        Feature above = null;
        above = tieFeatureByNameInQualifier("systematic_id", parent, feat, an, loc);
        if (above == null) {
            System.out.print("type : " + type);
        	above = tieFeatureByNameInQualifier("temporary_systematic_id", parent, feat, an, loc);
        }
        if (above == null) {
            above = tieFeatureByNameInQualifier("gene", parent, feat, an, loc);
        }
        if (above == null) {
            above = tieToGeneByLocation(type, parent, feat, loc);
            
        }
        if (above == null) {
            logger.warn("Got all the way through and not handled");
            return;
        }
        storeUtr(type, above, parent, feat, loc);
        
        // TODO Other properties/db_xrefs etc
        
    }

    private Feature tieToGeneByLocation(String type, Feature parent, StrandedFeature feat, StrandedLocation loc) {

        // Try and store via location

        Strand strand = feat.getStrand();
        int leftMost = loc.getMin()-1;
        int rightMost = loc.getMax();
        //logger.warn("Trying to tie UTR via location min='"+leftMost+"' max='"+rightMost+"' strand='"+strand.getToken()+"'");
        
        boolean reverse = false;
        if (strand.equals(StrandedFeature.NEGATIVE)) {
            reverse = true;
        }
        List<Feature> features = new ArrayList<Feature>(0);
            
        if ("three_prime_UTR".equals(type)) {
            if (reverse) {
                // On the left on reverse strand
                features = sequenceDao.getFeaturesByRange(rightMost, rightMost, strand.getValue(), parent, "gene");
            } else {
                // On the right on positive strand
                features = sequenceDao.getFeaturesByRange(leftMost, leftMost, strand.getValue(), parent, "gene");
            }
        } else {
            // 5'UTR
            if (reverse) {
                // On the right on reverse strand
                features = sequenceDao.getFeaturesByRange(leftMost, leftMost, strand.getValue(), parent, "gene");
            } else {
                // On the left on forward strand
                features = sequenceDao.getFeaturesByRange(rightMost, rightMost, strand.getValue(), parent, "gene");
            }
        }
        if (features.size() == 0) {
            logger.warn("Looking to place UTR by location but can't find gene");
            return null;
        }
        if (features.size() > 1) {
            logger.warn("Looking to place UTR by location but can't find unique gene ('"+features.size()+"' found)");
            return null;
        }
        return features.get(0);

    }

    
    @SuppressWarnings("unchecked")
    private void storeUtr(String type, Feature above, Feature parent, StrandedFeature feat, StrandedLocation loc) {
        // FIXME Doesn't cope with splicing

       Feature gene = null;
       Feature transcript = null;
       
       if (above.getType().getName().equals("gene")) {
           //logger.info("Trying to store '"+type+"' for gene '"+above.getUniquename()+"'");
           gene = above;
           Collection<FeatureRelationship> frs = gene.getFeatureRelationshipsForObjectId(); 
           logger.info("The number of possible transcripts is '"+frs.size()+"'");
           for (FeatureRelationship fr : frs) {
               System.out.println(fr.getSubjectFeature().getUniqueName());
           }
           for (FeatureRelationship fr : frs) {
               transcript = fr.getSubjectFeature();
               break;
           }
       } else {
           transcript = above;
           //logger.info("Trying to store '"+type+"' for transcript '"+above.getUniquename()+"'");
           Collection<FeatureRelationship> frs = transcript.getFeatureRelationshipsForSubjectId(); 
           //logger.info("The number of possible genes is '"+frs.size()+"'");
           for (FeatureRelationship fr : frs) {
               gene = fr.getObjectFeature();
               break;
           }
       }
        
       Iterator<StrandedLocation> it = loc.blockIterator();
       int exonCount = 0;
       while (it.hasNext()) {
           String utrName = this.gns.get5pUtr(transcript.getUniqueName(), exonCount);
           if ("three_prime_UTR".equals(type)) {
               utrName = this.gns.get3pUtr(transcript.getUniqueName(), exonCount);
           }
           exonCount++;
           StrandedLocation exonLoc = it.next();
           StrandedLocation location = new StrandedLocation(exonLoc);
           System.out.println("creating utr with name " + "exon:"+(exonCount-1)+":"+utrName);
           
           Feature utr;
           if ("three_prime_UTR".equals(type)) {
        	   utr = ThreePrimeUTR.make(transcript, location, utrName, organism, now);
           } else {
        	   utr = FivePrimeUTR.make(transcript, location, utrName, organism, now);
           }
           
           //Feature utr = this.featureUtils.createFeature(type, "exon:"+(exonCount-1)+":"+utrName, this.organism);
           //FeatureRelationship utrFr = this.featureUtils.createRelationship(utr, transcript, REL_PART_OF, exonCount-1);
           //FeatureLoc utrFl = this.featureUtils.createLocation(parent, utr, 
           //        exonLoc.getMin()-1, exonLoc.getMax(), (short)feat.getStrand().getValue());

       }
        
        //changeFeatureBounds(loc, gene);
        //changeFeatureBounds(loc, transcript);
        
    }

    private void changeFeatureBounds(StrandedLocation loc, Feature feature) {
        int newMin = loc.getMin()-1;
        int newMax = loc.getMax();
        Collection<FeatureLoc> locs = feature.getFeatureLocsForFeatureId();
        FeatureLoc currentLoc = locs.iterator().next();  // FIXME - Assumes that only 1 feature loc.  
        int currentMin = currentLoc.getFmin();
        int currentMax = currentLoc.getFmax();
        if (currentMin > newMin) {
            currentLoc.setFmin(newMin);
            //logger.info("Would like to change min to '"+newMin+"' from '"+currentMin+"' for '"+feature.getUniquename()+"'");
        }
        if (currentMax < newMax) {
            currentLoc.setFmax(newMax);
            //logger.info("Would like to change max to '"+newMax+"' from '"+currentMax+"' for '"+feature.getUniquename()+"'");
        }
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.FOURTH;
    }
        


}
