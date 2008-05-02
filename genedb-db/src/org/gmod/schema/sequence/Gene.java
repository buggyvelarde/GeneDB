package org.gmod.schema.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@SuppressWarnings("serial")
@Indexed
public class Gene extends Feature {
    private int start;
    private int stop;
    private String chr;
    private String strand;
    private int chrLen;
    private String synonym;

    @Transient
    @Field(name = "synonym", index = Index.TOKENIZED, store = Store.YES)
    public String getSynonym() {
        Collection<FeatureSynonym> synonyms = this.getFeatureSynonyms();
        String synonym = null;
        for (FeatureSynonym featureSynonym : synonyms) {
            synonym = synonym + featureSynonym.getSynonym().getName() + "\t";
        }
        return synonym;
    }

    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }

    public void setChrLen(int chrLen) {
        this.chrLen = chrLen;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    @Transient
    @Field(name = "protein", store = Store.YES)
    public String getProtein() {
        if (this.getUniqueName().contains("tRNA") || this.getUniqueName().contains("rRNA")) {
            return null;
        }
        Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForObjectId();
        for (FeatureRelationship relationship : objects) {
            Feature mrna = relationship.getFeatureBySubjectId();
            Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship relation : temps) {
                Feature tmp = relation.getFeatureBySubjectId();
                if (tmp.getCvTerm().getCvTermId() == 191)
                    return tmp.getUniqueName();
            }
        }
        return null;
    }

    @Transient
    @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProducts() {
        Feature protein = null;
        if (this.getUniqueName().contains("RNA")) {
            return null;
        }
        // System.out.println(this.getUniqueName() + "," +
        // this.getCvTerm().getName());
        Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForObjectId();
        for (FeatureRelationship relationship : objects) {
            Feature mrna = relationship.getFeatureBySubjectId();
            Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship relation : temps) {
                Feature tmp = relation.getFeatureBySubjectId();
                if (tmp.getCvTerm().getCvTermId() == 191)
                    protein = tmp;
            }
        }
        if (protein != null) {
            Collection<FeatureCvTerm> featureCvTerms = protein.getFeatureCvTerms();
            String products = "";
            for (FeatureCvTerm featureCvTerm : featureCvTerms) {
                if (featureCvTerm.getCvTerm().getCv().getName().equals("genedb_products")) {
                    products = products + featureCvTerm.getCvTerm().getName() + " ";
                }
            }
            return products;
        }
        return null;
    }

    @Transient
    @Field(name = "colour", index = Index.TOKENIZED, store = Store.YES)
    public String getColour() {
        Feature protein = null;
        if (this.getUniqueName().contains("tRNA") || this.getUniqueName().contains("rRNA")) {
            return null;
        }
        Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForObjectId();
        for (FeatureRelationship relationship : objects) {
            Feature mrna = relationship.getFeatureBySubjectId();
            Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship relation : temps) {
                Feature tmp = relation.getFeatureBySubjectId();
                if (tmp.getCvTerm().getCvTermId() == 191)
                    protein = tmp;
            }
        }
        if (protein != null) {
            Collection<FeatureProp> featureProps = protein.getFeatureProps();
            String colour = "";
            for (FeatureProp featureProp : featureProps) {
                if (featureProp.getCvTerm().getName().equals("colour")) {
                    colour = colour + featureProp.getValue() + " ";
                }
            }
            return colour;
        }
        return null;
    }

    @Transient
    @Field(name = "exonlocs", store = Store.YES)
    public String getExonLocs() {
        if (this.getUniqueName().contains("RNA")) {
            return null;
        }
        List<Feature> exons = new ArrayList<Feature>();
        String locs = "";
        Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForObjectId();
        if (objects.isEmpty()) {
            return null;
        }
        for (FeatureRelationship relationship : objects) {
            Feature mrna = relationship.getFeatureBySubjectId();
            Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship relation : temps) {
                Feature tmp = relation.getFeatureBySubjectId();
                if (tmp.getCvTerm().getCvTermId() == 234)
                    exons.add(tmp);
            }
        }
        int start = 999999999;
        int stop = 0;
        for (Feature exon : exons) {
            Collection<FeatureLoc> featureLocs = exon.getFeatureLocsForFeatureId();

            boolean first = true;
            for (FeatureLoc featureLoc : featureLocs) {
                if (first) {
                    Feature chromosome = featureLoc.getFeatureBySrcFeatureId();
                    byte[] residues = chromosome.getResidues();
                    int len = residues.length;
                    this.chrLen = len;

                    String chr = chromosome.getUniqueName();
                    this.chr = chr;

                    short strand = featureLoc.getStrand();
                    this.strand = Short.toString(strand);
                    first = false;
                }
                int min = featureLoc.getFmin();
                int max = featureLoc.getFmax();
                short strand = featureLoc.getStrand();
                String temp = "";
                if (strand == -1) {
                    temp = "(" + min + ".." + max + ")";
                } else {
                    temp = min + ".." + max;
                }
                if (start > min) {
                    start = min;
                }
                if (stop < max) {
                    stop = max;
                }
                if (locs != "") {
                    locs = locs + "," + temp;
                } else {
                    locs = temp;
                }

            }
        }

        this.start = start;
        this.stop = stop;
        return locs;
    }

    @Transient
    @Field(name = "chr", store = Store.YES)
    public String getChr() {
        return chr;
    }

    @Transient
    @Field(name = "start", store = Store.YES)
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Transient
    @Field(name = "stop", store = Store.YES)
    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    @Transient
    @Field(name = "strand", store = Store.YES)
    public String getStrand() {
        return strand;
    }

    @Transient
    @Field(name = "chrlen", store = Store.YES)
    public int getChrLen() {
        return chrLen;
    }
}
