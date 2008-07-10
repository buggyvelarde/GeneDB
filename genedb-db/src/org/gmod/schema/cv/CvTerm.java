package org.gmod.schema.cv;



import org.gmod.schema.analysis.AnalysisProp;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.OrganismProp;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.phylogeny.PhylonodeProp;
import org.gmod.schema.phylogeny.PhylonodeRelationship;
import org.gmod.schema.phylogeny.Phylotree;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubProp;
import org.gmod.schema.pub.PubRelationship;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermProp;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.FeatureRelationshipProp;
import org.gmod.schema.sequence.Synonym;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import java.io.Serializable;
import java.util.Collection;

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
@Table(name="cvterm")
public class CvTerm implements Serializable {

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvTerm")
    private Collection<Phylotree> phylotrees;

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvTerm")
    private Collection<PhylonodeProp> phylonodeProps;

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvTerm")
    private Collection<PhylonodeRelationship> phylonodeRelationships;

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvTerm")
    private Collection<Phylonode> phylonodes;

    public Collection<Phylotree> getPhylotrees() {
        return this.phylotrees;
    }

    public void setPhylotrees(Collection<Phylotree> phylotrees) {
        this.phylotrees = phylotrees;
    }


    public Collection<PhylonodeProp> getPhylonodeProps() {
        return this.phylonodeProps;
    }

    public void setPhylonodeProps(Collection<PhylonodeProp> phylonodeProps) {
        this.phylonodeProps = phylonodeProps;
    }

    public Collection<PhylonodeRelationship> getPhylonodeRelationships() {
        return this.phylonodeRelationships;
    }

    public void setPhylonodeRelationships(Collection<PhylonodeRelationship> phylonodeRelationships) {
        this.phylonodeRelationships = phylonodeRelationships;
    }

    public Collection<Phylonode> getPhylonodes() {
        return this.phylonodes;
    }

    public void setPhylonodes(Collection<Phylonode> phylonodes) {
        this.phylonodes = phylonodes;
    }


    // Fields
    @SequenceGenerator(name="generator",sequenceName="cvterm_cvterm_id_seq" )
    @Id @GeneratedValue(generator="generator")
    @Column(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int cvTermId;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=true, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="cv_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Cv cv;

    @Column(name="name", unique=false, nullable=false, insertable=true, updatable=true, length=1024)
    @Field(index = Index.UN_TOKENIZED,store=Store.YES)
     private String name;

    @Column(name="definition", unique=false, nullable=true, insertable=true, updatable=true)
     private String definition;

    @Column(name="is_obsolete", unique=false, nullable=false, insertable=true, updatable=true)
     private int isObsolete;

    @Column(name="is_relationshiptype", unique=false, nullable=false, insertable=true, updatable=true)
     private int isRelationshipType;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<AnalysisProp> analysisProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByTypeId")
     private Collection<CvTermProp> cvTermPropsForTypeId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByCvTermId")
     private Collection<CvTermProp> cvTermPropsForCvTermId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<DbXRefProp> dbXRefProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<Synonym> synonyms;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<CvTermDbXRef> cvTermDbXRefs;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByTypeId")
     private Collection<CvTermPath> cvTermPathsForTypeId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<FeatureCvTermProp> featureCvTermProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<FeatureCvTerm> featureCvTerms;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByTypeId")
     private Collection<CvTermRelationship> cvTermRelationshipsForTypeId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByObjectId")
     private Collection<CvTermRelationship> cvTermRelationshipsForObjectId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<PubProp> pubProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<OrganismProp> organismProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermBySubjectId")
     private Collection<CvTermRelationship> cvTermRelationshipsForSubjectId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByCvTermId")
     private Collection<CvTermSynonym> cvTermSynonymsForCvTermId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<FeatureProp> featureProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermBySubjectId")
     private Collection<CvTermPath> cvTermPathsForSubjectId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByObjectId")
     private Collection<CvTermPath> cvTermPathsForObjectId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTermByTypeId")
     private Collection<CvTermSynonym> cvTermSynonymsForTypeId;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<Pub> pubs;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<FeatureRelationshipProp> featureRelationshipProps;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<Feature> features;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<PubRelationship> pubRelationships;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Collection<FeatureRelationship> featureRelationships;

     // Constructors

    /** default constructor */
    public CvTerm() {
        // Deliberately empty default constructor
    }

    /** useful constructor! */
    public CvTerm(final Cv cv, final DbXRef dbXRef, final String name, final String definition) {
       this.dbXRef = dbXRef;
       this.cv = cv;
       this.name = name;
       this.definition = definition;
    }

    // Property accessors
    public int getCvTermId() {
        return this.cvTermId;
    }

    public void setCvTermId(int cvTermId) {
        this.cvTermId = cvTermId;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    public void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public Cv getCv() {
        return this.cv;
    }

    public void setCv(Cv cv) {
        this.cv = cv;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getIsObsolete() {
        return this.isObsolete;
    }

    public void setIsObsolete(int isObsolete) {
        this.isObsolete = isObsolete;
    }


    public int getIsRelationshipType() {
        return this.isRelationshipType;
    }

    public void setIsRelationshipType(int isRelationshipType) {
        this.isRelationshipType = isRelationshipType;
    }

    private Collection<AnalysisProp> getAnalysisProps() {
        return this.analysisProps;
    }

    private void setAnalysisProps(Collection<AnalysisProp> analysisProps) {
        this.analysisProps = analysisProps;
    }

    private Collection<CvTermProp> getCvTermPropsForTypeId() {
        return this.cvTermPropsForTypeId;
    }

    private void setCvTermPropsForTypeId(Collection<CvTermProp> cvTermPropsForTypeId) {
        this.cvTermPropsForTypeId = cvTermPropsForTypeId;
    }

    private Collection<CvTermProp> getCvTermPropsForCvTermId() {
        return this.cvTermPropsForCvTermId;
    }

    private void setCvTermPropsForCvTermId(Collection<CvTermProp> cvTermPropsForCvTermId) {
        this.cvTermPropsForCvTermId = cvTermPropsForCvTermId;
    }

    private Collection<DbXRefProp> getDbXRefProps() {
        return this.dbXRefProps;
    }

    private void setDbXRefProps(Collection<DbXRefProp> dbXRefProps) {
        this.dbXRefProps = dbXRefProps;
    }

    private Collection<Synonym> getSynonyms() {
        return this.synonyms;
    }

    private void setSynonyms(Collection<Synonym> synonyms) {
        this.synonyms = synonyms;
    }

    private Collection<CvTermDbXRef> getCvTermDbXRefs() {
        return this.cvTermDbXRefs;
    }

    private void setCvTermDbXRefs(Collection<CvTermDbXRef> cvTermDbXRefs) {
        this.cvTermDbXRefs = cvTermDbXRefs;
    }

    private Collection<CvTermPath> getCvTermPathsForTypeId() {
        return this.cvTermPathsForTypeId;
    }

    private void setCvTermPathsForTypeId(Collection<CvTermPath> cvTermPathsForTypeId) {
        this.cvTermPathsForTypeId = cvTermPathsForTypeId;
    }

    private Collection<FeatureCvTermProp> getFeatureCvTermProps() {
        return this.featureCvTermProps;
    }

    private void setFeatureCvTermProps(Collection<FeatureCvTermProp> featureCvTermProps) {
        this.featureCvTermProps = featureCvTermProps;
    }

    public Collection<FeatureCvTerm> getFeatureCvTerms() {
        return this.featureCvTerms;
    }

    private void setFeatureCvTerms(Collection<FeatureCvTerm> featureCvTerms) {
        this.featureCvTerms = featureCvTerms;
    }

    private Collection<CvTermRelationship> getCvTermRelationshipsForTypeId() {
        return this.cvTermRelationshipsForTypeId;
    }

    private void setCvTermRelationshipsForTypeId(Collection<CvTermRelationship> cvTermRelationshipsForTypeId) {
        this.cvTermRelationshipsForTypeId = cvTermRelationshipsForTypeId;
    }

    public Collection<CvTermRelationship> getCvTermRelationshipsForObjectId() {
        return this.cvTermRelationshipsForObjectId;
    }

    public void setCvTermRelationshipsForObjectId(Collection<CvTermRelationship> cvTermRelationshipsForObjectId) {
        this.cvTermRelationshipsForObjectId = cvTermRelationshipsForObjectId;
    }

    private Collection<PubProp> getPubProps() {
        return this.pubProps;
    }

    private void setPubProps(Collection<PubProp> pubProps) {
        this.pubProps = pubProps;
    }

    private Collection<OrganismProp> getOrganismProps() {
        return this.organismProps;
    }

    private void setOrganismProps(Collection<OrganismProp> organismProps) {
        this.organismProps = organismProps;
    }

    public Collection<CvTermRelationship> getCvTermRelationshipsForSubjectId() {
        return this.cvTermRelationshipsForSubjectId;
    }

    public void setCvTermRelationshipsForSubjectId(Collection<CvTermRelationship> cvTermRelationshipsForSubjectId) {
        this.cvTermRelationshipsForSubjectId = cvTermRelationshipsForSubjectId;
    }

    private Collection<CvTermSynonym> getCvTermSynonymsForCvTermId() {
        return this.cvTermSynonymsForCvTermId;
    }

    private void setCvTermSynonymsForCvTermId(Collection<CvTermSynonym> cvTermSynonymsForCvTermId) {
        this.cvTermSynonymsForCvTermId = cvTermSynonymsForCvTermId;
    }

    private Collection<FeatureProp> getFeatureProps() {
        return this.featureProps;
    }

    private void setFeatureProps(Collection<FeatureProp> featureProps) {
        this.featureProps = featureProps;
    }

    private Collection<CvTermPath> getCvTermPathsForSubjectId() {
        return this.cvTermPathsForSubjectId;
    }

    private void setCvTermPathsForSubjectId(Collection<CvTermPath> cvTermPathsForSubjectId) {
        this.cvTermPathsForSubjectId = cvTermPathsForSubjectId;
    }

    private Collection<CvTermPath> getCvTermPathsForObjectId() {
        return this.cvTermPathsForObjectId;
    }

    private void setCvTermPathsForObjectId(Collection<CvTermPath> cvTermPathsForObjectId) {
        this.cvTermPathsForObjectId = cvTermPathsForObjectId;
    }

    private Collection<CvTermSynonym> getCvTermSynonymsForTypeId() {
        return this.cvTermSynonymsForTypeId;
    }

    private void setCvTermSynonymsForTypeId(Collection<CvTermSynonym> cvTermSynonymsForTypeId) {
        this.cvTermSynonymsForTypeId = cvTermSynonymsForTypeId;
    }

    private Collection<Pub> getPubs() {
        return this.pubs;
    }

    private void setPubs(Collection<Pub> pubs) {
        this.pubs = pubs;
    }

    private Collection<FeatureRelationshipProp> getFeatureRelationshipProps() {
        return this.featureRelationshipProps;
    }

    private void setFeatureRelationshipProps(Collection<FeatureRelationshipProp> featureRelationshipProps) {
        this.featureRelationshipProps = featureRelationshipProps;
    }

    private Collection<Feature> getFeatures() {
        return this.features;
    }

    private void setFeatures(Collection<Feature> features) {
        this.features = features;
    }

    private Collection<PubRelationship> getPubRelationships() {
        return this.pubRelationships;
    }

    private void setPubRelationships(Collection<PubRelationship> pubRelationships) {
        this.pubRelationships = pubRelationships;
    }

    private Collection<FeatureRelationship> getFeatureRelationships() {
        return this.featureRelationships;
    }

    private void setFeatureRelationships(Collection<FeatureRelationship> featureRelationships) {
        this.featureRelationships = featureRelationships;
    }
}

