package org.gmod.schema.general;


import static javax.persistence.GenerationType.SEQUENCE;


import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.cv.CvTermDbXRef;
import org.gmod.schema.cv.DbXRefProp;
import org.gmod.schema.organism.OrganismDbXRef;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureDbXRef;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

    // Fields    
    @SequenceGenerator(name="generator", sequenceName="dbxref_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int dbXRefId;
     
    @Column(name="version", unique=false, nullable=false, insertable=true, updatable=true)
     private String version;
     
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        @JoinColumn(name="db_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Db db;
     
    @Column(name="accession", unique=false, nullable=false, insertable=true, updatable=true)
     private String accession;
     
    @Column(name="description", unique=false, nullable=true, insertable=true, updatable=true)
     private String description;
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<DbXRefProp> dbXRefProps = new HashSet<DbXRefProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<FeatureCvTermDbXRef> featureCvTermDbXRefs = new HashSet<FeatureCvTermDbXRef>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<Feature> features = new HashSet<Feature>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<FeatureDbXRef> featureDbXRefs = new HashSet<FeatureDbXRef>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<CvTerm> cvTerms = new HashSet<CvTerm>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<PubDbXRef> pubDbXRefs = new HashSet<PubDbXRef>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<OrganismDbXRef> organismDbXRefs = new HashSet<OrganismDbXRef>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbxref")
     private Set<CvTermDbXRef> cvTermDbXRefs = new HashSet<CvTermDbXRef>(0);

     // Constructors

    /** default constructor */
    public DbXRef() {
    }

	/** minimal constructor */
    private DbXRef(String version, Db db, String accession) {
        this.version = version;
        this.db = db;
        this.accession = accession;
    }
    /** full constructor */
    private DbXRef(String version, Db db, String accession, String description, Set<DbXRefProp> dbXRefProps, Set<FeatureCvTermDbXRef> featureCvTermDbXRefs, Set<Feature> features, Set<FeatureDbXRef> featureDbXRefs, Set<CvTerm> cvTerms, Set<PubDbXRef> pubDbXRefs, Set<OrganismDbXRef> organismDbXRefs, Set<CvTermDbXRef> cvTermDbXRefs) {
       this.version = version;
       this.db = db;
       this.accession = accession;
       this.description = description;
       this.dbXRefProps = dbXRefProps;
       this.featureCvTermDbXRefs = featureCvTermDbXRefs;
       this.features = features;
       this.featureDbXRefs = featureDbXRefs;
       this.cvTerms = cvTerms;
       this.pubDbXRefs = pubDbXRefs;
       this.organismDbXRefs = organismDbXRefs;
       this.cvTermDbXRefs = cvTermDbXRefs;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getDbXRefId()
     */
    private int getDbXRefId() {
        return this.dbXRefId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setDbXRefId(int)
     */
    private void setDbXRefId(int dbXRefId) {
        this.dbXRefId = dbXRefId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getVersion()
     */
    private String getVersion() {
        return this.version;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setVersion(java.lang.String)
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getDb()
     */
    public Db getDb() {
        return this.db;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setDb(org.gmod.schema.general.DbI)
     */
    public void setDb(Db db) {
        this.db = db;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getAccession()
     */
    public String getAccession() {
        return this.accession;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setAccession(java.lang.String)
     */
    public void setAccession(String accession) {
        this.accession = accession;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getDescription()
     */
    public String getDescription() {
        return this.description;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setDescription(java.lang.String)
     */
    private void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getDbXRefProps()
     */
    private Set<DbXRefProp> getDbXRefProps() {
        return this.dbXRefProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setDbXRefProps(java.util.Set)
     */
    private void setDbXRefProps(Set<DbXRefProp> dbXRefProps) {
        this.dbXRefProps = dbXRefProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getFeatureCvTermDbXRefs()
     */
    private Set<FeatureCvTermDbXRef> getFeatureCvTermDbXRefs() {
        return this.featureCvTermDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setFeatureCvTermDbXRefs(java.util.Set)
     */
    private void setFeatureCvTermDbXRefs(Set<FeatureCvTermDbXRef> featureCvTermDbXRefs) {
        this.featureCvTermDbXRefs = featureCvTermDbXRefs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getFeatures()
     */
    private Set<Feature> getFeatures() {
        return this.features;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setFeatures(java.util.Set)
     */
    private void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getFeatureDbXRefs()
     */
    private Set<FeatureDbXRef> getFeatureDbXRefs() {
        return this.featureDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setFeatureDbXRefs(java.util.Set)
     */
    private void setFeatureDbXRefs(Set<FeatureDbXRef> featureDbXRefs) {
        this.featureDbXRefs = featureDbXRefs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getCvTerms()
     */
    public Set<CvTerm> getCvTerms() {
        return this.cvTerms;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setCvTerms(java.util.Set)
     */
    private void setCvTerms(Set<CvTerm> cvTerms) {
        this.cvTerms = cvTerms;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getPubDbXRefs()
     */
    private Set<PubDbXRef> getPubDbXRefs() {
        return this.pubDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setPubDbXRefs(java.util.Set)
     */
    private void setPubDbXRefs(Set<PubDbXRef> pubDbXRefs) {
        this.pubDbXRefs = pubDbXRefs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getOrganismDbXRefs()
     */
    private Set<OrganismDbXRef> getOrganismDbXRefs() {
        return this.organismDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setOrganismDbXRefs(java.util.Set)
     */
    private void setOrganismDbXRefs(Set<OrganismDbXRef> organismDbXRefs) {
        this.organismDbXRefs = organismDbXRefs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#getCvTermDbXRefs()
     */
    private Set<CvTermDbXRef> getCvTermDbXRefs() {
        return this.cvTermDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefI#setCvTermDbXRefs(java.util.Set)
     */
    private void setCvTermDbXRefs(Set<CvTermDbXRef> cvTermDbXRefs) {
        this.cvTermDbXRefs = cvTermDbXRefs;
    }




}


