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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="gene")
@Indexed
public class Gene extends AbstractGene {

    Gene () {
        // empty
    }

    public Gene(Organism organism, String systematicId, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

    @Transient
    public Collection<MRNA> getCodingTranscripts() {
        Collection<MRNA> ret = new ArrayList<MRNA>();

        for (Transcript transcript : getTranscripts()) {
            if (transcript instanceof MRNA) {
                ret.add((MRNA) transcript);
            }
        }

        return ret;
    }

    @Transient @Field(name = "protein", store = Store.YES)
    public String getProteinUniqueNamesTabSeparated() {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (ProductiveTranscript transcript: getCodingTranscripts()) {
            if (first) {
                first = false;
            } else {
                ret.append('\t');
            }
            ret.append(transcript.getProteinUniqueName());
        }
        return ret.toString();
    }

    @Override
    @Transient @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsTabSeparatedString() {
        StringBuilder products = new StringBuilder();

        boolean first = true;
        for (ProductiveTranscript transcript : getCodingTranscripts()) {
            if (first) {
                first = false;
            } else {
                products.append('\t');
            }
            products.append(transcript.getProductsAsTabSeparatedString());
        }
        return products.toString();
    }


    public static Gene make(Feature parent, StrandedLocation location, String systematicId, Organism organism, Timestamp now) {
        Gene gene = new Gene(organism, systematicId, false, false, now);
        parent.addLocatedChild(gene, location);
        return gene;
    }

    @Transactional
    public static Gene makeHierarchy(Feature parent, StrandedLocation location, String systematicId,
            Class<? extends Transcript> transcriptType, boolean coding) {

        Organism organism = parent.getOrganism();

        Timestamp now = new Timestamp(new Date().getTime());


      Gene gene = make(parent, location, systematicId, organism, now);

      // TODO Do by reflection
      String transcriptId = systematicId + ":mRNA";
      Transcript transcript = MRNA.make(parent, location, transcriptId, organism, now);

      gene.addTranscript(transcript);

      // Store exons
      int exonCount = 0;
      for (SingleLocation exonLocation : location.getLocations()) {
          exonCount++;

          String exonSystematicId = systematicId + ":exon:"+ exonCount;
          Exon exon = Exon.make(parent, exonLocation, exonSystematicId, organism, now);

          transcript.addExon(exon);
      }
//          Feature exon = this.featureUtils
//          .createFeature("exon", this.gns.getExon(systematicId,
//                  1, exonCount), this.organism);
//          FeatureRelationship exonFr = this.featureUtils.createRelationship(
//                  exon, rna, REL_PART_OF, exonCount -1);
//          FeatureLoc exonFl = this.featureUtils.createLocation(parent, exon, l
//                  .getMin()-1, l.getMax(), strand);
      if (transcript instanceof ProductiveTranscript) {
         Polypeptide polypeptide = Polypeptide.make(parent, location, systematicId+":pep", organism, now);
         ProductiveTranscript pt = (ProductiveTranscript) transcript;
         pt.setProtein(polypeptide);
      }

        return gene;
    }


    public void addTranscript(Transcript transcript) {
        addFeatureRelationship(transcript, "relationship", "part_of");
    }
}