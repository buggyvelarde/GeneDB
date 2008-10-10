package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.SingleLocation;
import org.gmod.schema.utils.StrandedLocation;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

public class GeneBuilder {

    public static Gene makeHierarchy(Feature parent, StrandedLocation location, String systematicId,
            Class<? extends Transcript> transcriptType, boolean coding) {

        Organism organism = parent.getOrganism();

        Timestamp now = new Timestamp(new Date().getTime());


      Gene gene = new Gene(organism, systematicId, false, false, now); // parent, location, systematicId, organism, now);
      parent.addLocatedChild(gene, location);
      // TODO Do by reflection
      String transcriptId = systematicId + ":mRNA";
      Transcript transcript = new MRNA(organism, transcriptId, false, false, now); //parent, location, transcriptId, organism, now);

      gene.addTranscript(transcript);
      parent.addLocatedChild(transcript, location);

      if (transcript instanceof ProductiveTranscript) {
          Polypeptide polypeptide = Polypeptide.make(parent, location, systematicId+":pep", organism, now);
          ProductiveTranscript pt = (ProductiveTranscript) transcript;
          pt.setProtein(polypeptide);
       }

      // Store exons
      int exonCount = 0;
      for (SingleLocation exonLocation : location.getLocations()) {
          exonCount++;

          String exonSystematicId = systematicId + ":exon:"+ exonCount;
          AbstractExon exon = transcript.createExon(exonSystematicId, exonLocation.getMin(), exonLocation.getMax());

      }
//          Feature exon = this.featureUtils
//          .createFeature("exon", this.gns.getExon(systematicId,
//                  1, exonCount), this.organism);
//          FeatureRelationship exonFr = this.featureUtils.createRelationship(
//                  exon, rna, REL_PART_OF, exonCount -1);
//          FeatureLoc exonFl = this.featureUtils.createLocation(parent, exon, l
//                  .getMin()-1, l.getMax(), strand);


        return gene;
    }
}
