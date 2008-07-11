package org.gmod.schema.general;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.cv.CvTermDbXRef;
import org.gmod.schema.cv.DbXRefProp;
import org.gmod.schema.organism.OrganismDbXRef;
import org.gmod.schema.phylogeny.PhylonodeDbXRef;
import org.gmod.schema.phylogeny.Phylotree;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureDbXRef;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="dbxref")
public class DbXRef implements Serializable {

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="dbXRef")
    private Collection<PhylonodeDbXRef> phylonodeDbXRefs;

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="dbXRef")
    private Collection<Phylotree> phylotrees;

    public Collection<PhylonodeDbXRef> getPhylonodeDbXRefs() {
        return this.phylonodeDbXRefs;
    }

    public void setPhylonodeDbXRefs(Collection<PhylonodeDbXRef> phylonodeDbXRefs) {
        this.phylonodeDbXRefs = phylonodeDbXRefs;
    }


    public Collection<Phylotree> getPhylotrees() {
        return this.phylotrees;
    }

    public void setPhylotrees(Collection<Phylotree> phylotrees) {
        this.phylotrees = phylotrees;
    }

    // Fields
    @SequenceGenerator(name="generator", sequenceName="dbxref_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int dbXRefId;

    @Column(name="version", unique=false, nullable=false, insertable=true, updatable=true)
     private String version;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="db_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Db db;

    @Column(name="accession", unique=false, nullable=false, insertable=true, updatable=true)
     private String accession;

    @Column(name="description", unique=false, nullable=true, insertable=true, updatable=true)
     private String description;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<CvTerm> cvTerms = new HashSet<CvTerm>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<DbXRefProp> dbXRefProps = new HashSet<DbXRefProp>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs = new HashSet<FeatureCvTermDbXRef>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<Feature> features = new HashSet<Feature>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<FeatureDbXRef> featureDbXRefs = new HashSet<FeatureDbXRef>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<PubDbXRef> pubDbXRefs = new HashSet<PubDbXRef>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<OrganismDbXRef> organismDbXRefs = new HashSet<OrganismDbXRef>(0);

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<CvTermDbXRef> cvTermDbXRefs = new HashSet<CvTermDbXRef>(0);

     // Constructors

    /** default constructor */
    private DbXRef() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public DbXRef(Db db, String accession) {
        this.version = "1";
        this.db = db;
        this.accession = accession;
    }


    // Property accessors
    private int getDbXRefId() {
        return this.dbXRefId;
    }

    private void setDbXRefId(int dbXRefId) {
        this.dbXRefId = dbXRefId;
    }


    private String getVersion() {
        return this.version;
    }

    private void setVersion(String version) {
        this.version = version;
    }

    public Db getDb() {
        return this.db;
    }

    private void setDb(Db db) {
        this.db = db;
    }

    public String getAccession() {
        return this.accession;
    }

    private void setAccession(String accession) {
        this.accession = accession;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<CvTerm> getCvTerms() {
        return this.cvTerms;
    }

    private Collection<DbXRefProp> getDbXRefProps() {
        return this.dbXRefProps;
    }

    private void setDbXRefProps(Collection<DbXRefProp> dbXRefProps) {
        this.dbXRefProps = dbXRefProps;
    }

    private Collection<FeatureCvTermDbXRef> getFeatureCvTermDbXRefs() {
        return this.featureCvTermDbXRefs;
    }

    private void setFeatureCvTermDbXRefs(Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs) {
        this.featureCvTermDbXRefs = featureCvTermDbXRefs;
    }

    private Collection<Feature> getFeatures() {
        return this.features;
    }

    private void setFeatures(Collection<Feature> features) {
        this.features = features;
    }

    private Collection<FeatureDbXRef> getFeatureDbXRefs() {
        return this.featureDbXRefs;
    }

    private void setFeatureDbXRefs(Collection<FeatureDbXRef> featureDbXRefs) {
        this.featureDbXRefs = featureDbXRefs;
    }

    private void setCvTerms(Collection<CvTerm> cvTerms) {
        this.cvTerms = cvTerms;
    }

    private Collection<PubDbXRef> getPubDbXRefs() {
        return this.pubDbXRefs;
    }

    private void setPubDbXRefs(Collection<PubDbXRef> pubDbXRefs) {
        this.pubDbXRefs = pubDbXRefs;
    }

    private Collection<OrganismDbXRef> getOrganismDbXRefs() {
        return this.organismDbXRefs;
    }

    private void setOrganismDbXRefs(Collection<OrganismDbXRef> organismDbXRefs) {
        this.organismDbXRefs = organismDbXRefs;
    }

    private Collection<CvTermDbXRef> getCvTermDbXRefs() {
        return this.cvTermDbXRefs;
    }

    private void setCvTermDbXRefs(Collection<CvTermDbXRef> cvTermDbXRefs) {
        this.cvTermDbXRefs = cvTermDbXRefs;
    }
}


