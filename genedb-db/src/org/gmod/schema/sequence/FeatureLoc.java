package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;


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
@Table(name="featureloc")
public class FeatureLoc implements Serializable {

    // Fields 
    
    @SequenceGenerator(name="generator", sequenceName="featureloc_featureloc_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="featureloc_id", unique=false, nullable=false, insertable=true, updatable=true)
    private int featureLocId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="srcfeature_id", unique=false, nullable=true, insertable=true, updatable=true)
    private Feature featureBySrcFeatureId;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature featureByFeatureId;
     
    @Column(name="fmin", unique=false, nullable=true, insertable=true, updatable=true)
    private Integer fmin;
     
    @Column(name="is_fmin_partial", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean fminPartial;
     
    @Column(name="fmax", unique=false, nullable=true, insertable=true, updatable=true)
    private Integer fmax;
     
    @Column(name="is_fmax_partial", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean fmaxPartial;
     
    @Column(name="strand", unique=false, nullable=true, insertable=true, updatable=true)
    private Short strand;
     
    @Column(name="phase", unique=false, nullable=true, insertable=true, updatable=true)
    private Integer phase;
     
    @Column(name="residue_info", unique=false, nullable=true, insertable=true, updatable=true)
    private String residueInfo;
     
    @Column(name="locgroup", unique=false, nullable=false, insertable=true, updatable=true)
    private int locGroup;
     
    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
    private int rank;
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureloc")
     private Set<FeatureLocPub> featureLocPubs = new HashSet<FeatureLocPub>(0);

     // Constructors

    /** default constructor */
    public FeatureLoc() {
    }

	/** minimal constructor */
    private FeatureLoc(Feature featureBySrcFeatureId, boolean fminPartial, boolean fmaxPartial, int locGroup, int rank) {
        this.featureBySrcFeatureId = featureBySrcFeatureId;
        this.fminPartial = fminPartial;
        this.fmaxPartial = fmaxPartial;
        this.locGroup = locGroup;
        this.rank = rank;
    }
    /** large constructor */
    public FeatureLoc(Feature featureBySrcfeatureId, Feature featureByFeatureId, Integer fmin, boolean fminPartial, Integer fmax, boolean fmaxPartial, Short strand, Integer phase, int locGroup, int rank) {
       this.featureBySrcFeatureId = featureBySrcfeatureId;
       this.featureByFeatureId = featureByFeatureId;
       this.fmin = fmin;
       this.fminPartial = fminPartial;
       this.fmax = fmax;
       this.fmaxPartial = fmaxPartial;
       this.strand = strand;
       this.phase = phase;
       this.locGroup = locGroup;
       this.rank = rank;
    }
    
   
    // Property accessors


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getFeatureBySrcfeatureId()
     */
    private Feature getFeatureBySrcFeatureId() {
        return this.featureBySrcFeatureId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFeatureBySrcfeatureId(org.genedb.db.jpa.Feature)
     */
    private void setFeatureBySrcFeatureId(Feature featureBySrcFeatureId) {
        this.featureBySrcFeatureId = featureBySrcFeatureId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getFeatureByFeatureId()
     */
    private Feature getFeatureByFeatureId() {
        return this.featureByFeatureId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFeatureByFeatureId(org.genedb.db.jpa.Feature)
     */
    public void setFeatureByFeatureId(Feature featureByFeatureId) {
        this.featureByFeatureId = featureByFeatureId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getFmin()
     */
    public Integer getFmin() {
        return this.fmin;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFmin(java.lang.Integer)
     */
    public void setFmin(Integer fmin) {
        this.fmin = fmin;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#isFminPartial()
     */
    private boolean isFminPartial() {
        return this.fminPartial;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFminPartial(boolean)
     */
    private void setFminPartial(boolean fminPartial) {
        this.fminPartial = fminPartial;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getFmax()
     */
    public Integer getFmax() {
        return this.fmax;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFmax(java.lang.Integer)
     */
    public void setFmax(Integer fmax) {
        this.fmax = fmax;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#isFmaxPartial()
     */
    private boolean isFmaxPartial() {
        return this.fmaxPartial;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFmaxPartial(boolean)
     */
    private void setFmaxPartial(boolean fmaxPartial) {
        this.fmaxPartial = fmaxPartial;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getStrand()
     */
    private Short getStrand() {
        return this.strand;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setStrand(java.lang.Short)
     */
    private void setStrand(Short strand) {
        this.strand = strand;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getPhase()
     */
    private Integer getPhase() {
        return this.phase;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setPhase(java.lang.Integer)
     */
    private void setPhase(Integer phase) {
        this.phase = phase;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getResidueInfo()
     */
    private String getResidueInfo() {
        return this.residueInfo;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setResidueInfo(java.lang.String)
     */
    private void setResidueInfo(String residueInfo) {
        this.residueInfo = residueInfo;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getLocgroup()
     */
    private int getLocGroup() {
        return this.locGroup;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setLocgroup(int)
     */
    private void setLocGroup(int locGroup) {
        this.locGroup = locGroup;
    }
    
 
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getRank()
     */
    private int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setRank(int)
     */
    private void setRank(int rank) {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#getFeaturelocPubs()
     */
    private Set<FeatureLocPub> getFeatureLocPubs() {
        return this.featureLocPubs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocI#setFeaturelocPubs(java.util.Set)
     */
    private void setFeaturelocPubs(Set<FeatureLocPub> featureLocPubs) {
        this.featureLocPubs = featureLocPubs;
    }

    @SuppressWarnings("unused")
    private int getFeatureLocId() {
        return this.featureLocId;
    }

    @SuppressWarnings("unused")
    private void setFeatureLocId(int featureLocId) {
        this.featureLocId = featureLocId;
    }




}


