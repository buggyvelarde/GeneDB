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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="cvterm")
public class CvTerm implements Serializable {

    private Set<Phylotree> phylotrees = new HashSet<Phylotree>(0);
    private Set<PhylonodeProp> phylonodeProps = new HashSet<PhylonodeProp>(0);
    private Set<PhylonodeRelationship> phylonodeRelationships = new HashSet<PhylonodeRelationship>(0);
    private Set<Phylonode> phylonodes = new HashSet<Phylonode>(0);
   
    

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvterm")
    public Set<Phylotree> getPhylotrees() {
        return this.phylotrees;
    }
    
    public void setPhylotrees(Set<Phylotree> phylotrees) {
        this.phylotrees = phylotrees;
    }

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvterm")
    public Set<PhylonodeProp> getPhylonodeProps() {
        return this.phylonodeProps;
    }
    
    public void setPhylonodeProps(Set<PhylonodeProp> phylonodeProps) {
        this.phylonodeProps = phylonodeProps;
    }



    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvterm")
    public Set<PhylonodeRelationship> getPhylonodeRelationships() {
        return this.phylonodeRelationships;
    }
    
    public void setPhylonodeRelationships(Set<PhylonodeRelationship> phylonodeRelationships) {
        this.phylonodeRelationships = phylonodeRelationships;
    }

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="cvterm")
    public Set<Phylonode> getPhylonodes() {
        return this.phylonodes;
    }
    
    public void setPhylonodes(Set<Phylonode> phylonodes) {
        this.phylonodes = phylonodes;
    }
    
    
    
    
    
    
    
    
    
    
    
    // Fields    
    @Id
    @Column(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int cvTermId;
    
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=true, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;
     
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        @JoinColumn(name="cv_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Cv cv;
     
    @Column(name="name", unique=false, nullable=false, insertable=true, updatable=true, length=1024)
     private String name;
     
    @Column(name="definition", unique=false, nullable=true, insertable=true, updatable=true)
     private String definition;
     
    @Column(name="is_obsolete", unique=false, nullable=false, insertable=true, updatable=true)
     private int isObsolete;
     
    @Column(name="is_relationshiptype", unique=false, nullable=false, insertable=true, updatable=true)
     private int isRelationshipType;
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<AnalysisProp> analysisProps = new HashSet<AnalysisProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByTypeId")
     private Set<CvTermProp> cvTermPropsForTypeId = new HashSet<CvTermProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByCvtermId")
     private Set<CvTermProp> cvTermPropsForCvTermId = new HashSet<CvTermProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<DbXRefProp> dbXRefProps = new HashSet<DbXRefProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<Synonym> synonyms = new HashSet<Synonym>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<CvTermDbXRef> cvTermDbXRefs = new HashSet<CvTermDbXRef>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByTypeId")
     private Set<CvTermPath> cvTermPathsForTypeId = new HashSet<CvTermPath>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<FeatureCvTermProp> featureCvTermProps = new HashSet<FeatureCvTermProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<FeatureCvTerm> featureCvTerms = new HashSet<FeatureCvTerm>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByTypeId")
     private Set<CvTermRelationship> cvTermRelationshipsForTypeId = new HashSet<CvTermRelationship>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByObjectId")
     private Set<CvTermRelationship> cvTermRelationshipsForObjectId = new HashSet<CvTermRelationship>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<PubProp> pubProps = new HashSet<PubProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<OrganismProp> organismProps = new HashSet<OrganismProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermBySubjectId")
     private Set<CvTermRelationship> cvTermRelationshipsForSubjectId = new HashSet<CvTermRelationship>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByCvtermId")
     private Set<CvTermSynonym> cvTermSynonymsForCvTermId = new HashSet<CvTermSynonym>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<FeatureProp> featureProps = new HashSet<FeatureProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermBySubjectId")
     private Set<CvTermPath> cvTermPathsForSubjectId = new HashSet<CvTermPath>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByObjectId")
     private Set<CvTermPath> cvTermPathsForObjectId = new HashSet<CvTermPath>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvtermByTypeId")
     private Set<CvTermSynonym> cvTermSynonymsForTypeId = new HashSet<CvTermSynonym>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<Pub> pubs = new HashSet<Pub>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<FeatureRelationshipProp> featureRelationshipProps = new HashSet<FeatureRelationshipProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvTerm")
     private Set<Feature> features = new HashSet<Feature>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<PubRelationship> pubRelationships = new HashSet<PubRelationship>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cvterm")
     private Set<FeatureRelationship> featureRelationships = new HashSet<FeatureRelationship>(0);

     // Constructors

    /** default constructor */
    public CvTerm() {
    }

	/** minimal constructor */
    private CvTerm(DbXRef dbXRef, Cv cv, String name, int isObsolete, int isRelationshipType) {
        this.dbXRef = dbXRef;
        this.cv = cv;
        this.name = name;
        this.isObsolete = isObsolete;
        this.isRelationshipType = isRelationshipType;
    }
    /** full constructor */
    private CvTerm(DbXRef dbXRef, Cv cv, String name, String definition, int isObsolete, int isRelationshipType, Set<AnalysisProp> analysisProps, Set<CvTermProp> cvTermPropsForTypeId, Set<CvTermProp> cvTermPropsForCvTermId, Set<DbXRefProp> dbXRefProps, Set<Synonym> synonyms, Set<CvTermDbXRef> cvTermDbXRefs, Set<CvTermPath> cvTermPathsForTypeId, Set<FeatureCvTermProp> featureCvTermProps, Set<FeatureCvTerm> featureCvTerms, Set<CvTermRelationship> cvTermRelationshipsForTypeId, Set<CvTermRelationship> cvTermRelationshipsForObjectId, Set<PubProp> pubProps, Set<OrganismProp> organismProps, Set<CvTermRelationship> cvTermRelationshipsForSubjectId, Set<CvTermSynonym> cvTermSynonymsForCvTermId, Set<FeatureProp> featureProps, Set<CvTermPath> cvTermPathsForSubjectId, Set<CvTermPath> cvTermPathsForObjectId, Set<CvTermSynonym> cvTermSynonymsForTypeId, Set<Pub> pubs, Set<FeatureRelationshipProp> featureRelationshipProps, Set<Feature> features, Set<PubRelationship> pubRelationships, Set<FeatureRelationship> featureRelationships) {
       this.dbXRef = dbXRef;
       this.cv = cv;
       this.name = name;
       this.definition = definition;
       this.isObsolete = isObsolete;
       this.isRelationshipType = isRelationshipType;
       this.analysisProps = analysisProps;
       this.cvTermPropsForTypeId = cvTermPropsForTypeId;
       this.cvTermPropsForCvTermId = cvTermPropsForCvTermId;
       this.dbXRefProps = dbXRefProps;
       this.synonyms = synonyms;
       this.cvTermDbXRefs = cvTermDbXRefs;
       this.cvTermPathsForTypeId = cvTermPathsForTypeId;
       this.featureCvTermProps = featureCvTermProps;
       this.featureCvTerms = featureCvTerms;
       this.cvTermRelationshipsForTypeId = cvTermRelationshipsForTypeId;
       this.cvTermRelationshipsForObjectId = cvTermRelationshipsForObjectId;
       this.pubProps = pubProps;
       this.organismProps = organismProps;
       this.cvTermRelationshipsForSubjectId = cvTermRelationshipsForSubjectId;
       this.cvTermSynonymsForCvTermId = cvTermSynonymsForCvTermId;
       this.featureProps = featureProps;
       this.cvTermPathsForSubjectId = cvTermPathsForSubjectId;
       this.cvTermPathsForObjectId = cvTermPathsForObjectId;
       this.cvTermSynonymsForTypeId = cvTermSynonymsForTypeId;
       this.pubs = pubs;
       this.featureRelationshipProps = featureRelationshipProps;
       this.features = features;
       this.pubRelationships = pubRelationships;
       this.featureRelationships = featureRelationships;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermId()
     */
    public int getCvTermId() {
        return this.cvTermId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermId(int)
     */
    public void setCvTermId(int cvTermId) {
        this.cvTermId = cvTermId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getDbXRef()
     */
    public DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setDbXRef(org.genedb.db.jpa.DbXRef)
     */
    public void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCv()
     */
    public Cv getCv() {
        return this.cv;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCv(org.gmod.schema.cv.CvI)
     */
    public void setCv(Cv cv) {
        this.cv = cv;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getName()
     */
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getDefinition()
     */
    public String getDefinition() {
        return this.definition;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setDefinition(java.lang.String)
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getIsObsolete()
     */
    public int getIsObsolete() {
        return this.isObsolete;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setIsObsolete(int)
     */
    public void setIsObsolete(int isObsolete) {
        this.isObsolete = isObsolete;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getIsRelationshipType()
     */
    public int getIsRelationshipType() {
        return this.isRelationshipType;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setIsRelationshipType(int)
     */
    public void setIsRelationshipType(int isRelationshipType) {
        this.isRelationshipType = isRelationshipType;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getAnalsisProps()
     */
    private Collection<AnalysisProp> getAnalysisProps() {
        return this.analysisProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setAnalsisProps(java.util.Set)
     */
    private void setAnalysisProps(Set<AnalysisProp> analysisProps) {
        this.analysisProps = analysisProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermPropsForTypeId()
     */
    private Collection<CvTermProp> getCvTermPropsForTypeId() {
        return this.cvTermPropsForTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermPropsForTypeId(java.util.Set)
     */
    private void setCvTermPropsForTypeId(Set<CvTermProp> cvTermPropsForTypeId) {
        this.cvTermPropsForTypeId = cvTermPropsForTypeId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermPropsForCvTermId()
     */
    private Collection<CvTermProp> getCvTermPropsForCvTermId() {
        return this.cvTermPropsForCvTermId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermPropsForCvTermId(java.util.Set)
     */
    private void setCvTermPropsForCvTermId(Set<CvTermProp> cvTermPropsForCvTermId) {
        this.cvTermPropsForCvTermId = cvTermPropsForCvTermId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getDbXRefProps()
     */
    private Collection<DbXRefProp> getDbXRefProps() {
        return this.dbXRefProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setDbXRefProps(java.util.Set)
     */
    private void setDbXRefProps(Set<DbXRefProp> dbXRefProps) {
        this.dbXRefProps = dbXRefProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getSynonyms()
     */
    private Collection<Synonym> getSynonyms() {
        return this.synonyms;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setSynonyms(java.util.Set)
     */
    private void setSynonyms(Set<Synonym> synonyms) {
        this.synonyms = synonyms;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermDbXRefs()
     */
    private Collection<CvTermDbXRef> getCvTermDbXRefs() {
        return this.cvTermDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermDbXRefs(java.util.Set)
     */
    private void setCvTermDbXRefs(Set<CvTermDbXRef> cvTermDbXRefs) {
        this.cvTermDbXRefs = cvTermDbXRefs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermPathsForTypeId()
     */
    private Collection<CvTermPath> getCvTermPathsForTypeId() {
        return this.cvTermPathsForTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermPathsForTypeId(java.util.Set)
     */
    private void setCvTermPathsForTypeId(Set<CvTermPath> cvTermPathsForTypeId) {
        this.cvTermPathsForTypeId = cvTermPathsForTypeId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getFeatureCvTermProps()
     */
    private Collection<FeatureCvTermProp> getFeatureCvTermProps() {
        return this.featureCvTermProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setFeatureCvTermProps(java.util.Set)
     */
    private void setFeatureCvTermProps(Set<FeatureCvTermProp> featureCvTermProps) {
        this.featureCvTermProps = featureCvTermProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getFeatureCvTerms()
     */
    private Collection<FeatureCvTerm> getFeatureCvTerms() {
        return this.featureCvTerms;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setFeatureCvTerms(java.util.Set)
     */
    private void setFeatureCvTerms(Set<FeatureCvTerm> featureCvTerms) {
        this.featureCvTerms = featureCvTerms;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermRelationshipsForTypeId()
     */
    private Collection<CvTermRelationship> getCvTermRelationshipsForTypeId() {
        return this.cvTermRelationshipsForTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermRelationshipsForTypeId(java.util.Set)
     */
    private void setCvTermRelationshipsForTypeId(Set<CvTermRelationship> cvTermRelationshipsForTypeId) {
        this.cvTermRelationshipsForTypeId = cvTermRelationshipsForTypeId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermRelationshipsForObjectId()
     */
    private Collection<CvTermRelationship> getCvTermRelationshipsForObjectId() {
        return this.cvTermRelationshipsForObjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermRelationshipsForObjectId(java.util.Set)
     */
    private void setCvTermRelationshipsForObjectId(Set<CvTermRelationship> cvTermRelationshipsForObjectId) {
        this.cvTermRelationshipsForObjectId = cvTermRelationshipsForObjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getPubProps()
     */
    private Collection<PubProp> getPubProps() {
        return this.pubProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setPubProps(java.util.Set)
     */
    private void setPubProps(Set<PubProp> pubProps) {
        this.pubProps = pubProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getOrganismProps()
     */
    private Collection<OrganismProp> getOrganismProps() {
        return this.organismProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setOrganismProps(java.util.Set)
     */
    private void setOrganismProps(Set<OrganismProp> organismProps) {
        this.organismProps = organismProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermRelationshipsForSubjectId()
     */
    private Collection<CvTermRelationship> getCvTermRelationshipsForSubjectId() {
        return this.cvTermRelationshipsForSubjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermRelationshipsForSubjectId(java.util.Set)
     */
    private void setCvTermRelationshipsForSubjectId(Set<CvTermRelationship> cvTermRelationshipsForSubjectId) {
        this.cvTermRelationshipsForSubjectId = cvTermRelationshipsForSubjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermSynonymsForCvTermId()
     */
    private Collection<CvTermSynonym> getCvTermSynonymsForCvTermId() {
        return this.cvTermSynonymsForCvTermId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermSynonymsForCvTermId(java.util.Set)
     */
    private void setCvTermSynonymsForCvTermId(Set<CvTermSynonym> cvTermSynonymsForCvTermId) {
        this.cvTermSynonymsForCvTermId = cvTermSynonymsForCvTermId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getFeatureProps()
     */
    private Collection<FeatureProp> getFeatureProps() {
        return this.featureProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setFeatureProps(java.util.Set)
     */
    private void setFeatureProps(Set<FeatureProp> featureProps) {
        this.featureProps = featureProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermPathsForSubjectId()
     */
    private Collection<CvTermPath> getCvTermPathsForSubjectId() {
        return this.cvTermPathsForSubjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermPathsForSubjectId(java.util.Set)
     */
    private void setCvTermPathsForSubjectId(Set<CvTermPath> cvTermPathsForSubjectId) {
        this.cvTermPathsForSubjectId = cvTermPathsForSubjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermPathsForObjectId()
     */
    private Collection<CvTermPath> getCvTermPathsForObjectId() {
        return this.cvTermPathsForObjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermPathsForObjectId(java.util.Set)
     */
    private void setCvTermPathsForObjectId(Set<CvTermPath> cvTermPathsForObjectId) {
        this.cvTermPathsForObjectId = cvTermPathsForObjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getCvTermSynonymsForTypeId()
     */
    private Collection<CvTermSynonym> getCvTermSynonymsForTypeId() {
        return this.cvTermSynonymsForTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setCvTermSynonymsForTypeId(java.util.Set)
     */
    private void setCvTermSynonymsForTypeId(Set<CvTermSynonym> cvTermSynonymsForTypeId) {
        this.cvTermSynonymsForTypeId = cvTermSynonymsForTypeId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getPubs()
     */
    private Collection<Pub> getPubs() {
        return this.pubs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setPubs(java.util.Set)
     */
    private void setPubs(Set<Pub> pubs) {
        this.pubs = pubs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getFeatureRelationshipProps()
     */
    private Collection<FeatureRelationshipProp> getFeatureRelationshipProps() {
        return this.featureRelationshipProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setFeatureRelationshipProps(java.util.Set)
     */
    private void setFeatureRelationshipProps(Set<FeatureRelationshipProp> featureRelationshipProps) {
        this.featureRelationshipProps = featureRelationshipProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getFeatures()
     */
    private Collection<Feature> getFeatures() {
        return this.features;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setFeatures(java.util.Set)
     */
    private void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getPubRelationships()
     */
    private Collection<PubRelationship> getPubRelationships() {
        return this.pubRelationships;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setPubRelationships(java.util.Set)
     */
    private void setPubRelationships(Set<PubRelationship> pubRelationships) {
        this.pubRelationships = pubRelationships;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#getFeatureRelationships()
     */
    private Collection<FeatureRelationship> getFeatureRelationships() {
        return this.featureRelationships;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermI#setFeatureRelationships(java.util.Set)
     */
    private void setFeatureRelationships(Set<FeatureRelationship> featureRelationships) {
        this.featureRelationships = featureRelationships;
    }




}


