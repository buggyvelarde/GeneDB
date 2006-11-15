package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.utils.Rankable;

import java.io.Serializable;
import java.util.Collection;
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
@Table(name="feature_cvterm")
public class FeatureCvTerm implements Serializable, Rankable {

    // Fields 
    
    @SequenceGenerator(name="generator", sequenceName="feature_cvterm_feature_cvterm_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature feature;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;
     
    @Column(name="is_not", unique=false, nullable=false, insertable=true, updatable=true)
     private boolean not;
     
    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
    private int rank;
    
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureCvterm")
     private Collection<FeatureCvTermProp> featureCvTermProps = new HashSet<FeatureCvTermProp>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureCvterm")
     private Collection<FeatureCvTermPub> featureCvTermPubs = new HashSet<FeatureCvTermPub>(0);
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureCvterm")
     private Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs = new HashSet<FeatureCvTermDbXRef>(0);

     // Constructors

    /** default constructor */
    public FeatureCvTerm() {
    }

	/** minimal constructor */
    public FeatureCvTerm(CvTerm cvTerm, Feature feature, Pub pub, boolean not,int rank) {
        this.cvTerm = cvTerm;
        this.feature = feature;
        this.pub = pub;
        this.not = not;
        this.rank = rank;
    }
    /** full constructor */
    private FeatureCvTerm(CvTerm cvTerm, Feature feature, Pub pub, boolean not, int rank,Set<FeatureCvTermProp> featureCvTermProps, Set<FeatureCvTermPub> featureCvTermPubs, Set<FeatureCvTermDbXRef> featureCvTermDbXRefs) {
       this.cvTerm = cvTerm;
       this.feature = feature;
       this.pub = pub;
       this.not = not;
       this.featureCvTermProps = featureCvTermProps;
       this.featureCvTermPubs = featureCvTermPubs;
       this.featureCvTermDbXRefs = featureCvTermDbXRefs;
       this.rank = rank;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getFeatureCvTermId()
     */
    public int getFeatureCvTermId() {
        return this.featureCvTermId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setFeatureCvTermId(int)
     */
    public void setFeatureCvTermId(int featureCvTermId) {
        this.featureCvTermId = featureCvTermId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getCvterm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setCvterm(org.gmod.schema.cv.CvTermI)
     */
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getFeature()
     */
    public Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setFeature(org.genedb.db.jpa.Feature)
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getPub()
     */
    public Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setPub(org.gmod.schema.pub.PubI)
     */
    public void setPub(Pub pub) {
        this.pub = pub;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#isNot()
     */
    public boolean isNot() {
        return this.not;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setNot(boolean)
     */
    public void setNot(boolean not) {
        this.not = not;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getFeatureCvtermprops()
     */
    public Collection<FeatureCvTermProp> getFeatureCvTermProps() {
        return this.featureCvTermProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setFeatureCvtermprops(java.util.Set)
     */
    public void setFeatureCvTermProps(Collection<FeatureCvTermProp> featureCvTermProps) {
        this.featureCvTermProps = featureCvTermProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getFeatureCvtermPubs()
     */
    private Collection<FeatureCvTermPub> getFeatureCvTermPubs() {
        return this.featureCvTermPubs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setFeatureCvtermPubs(java.util.Set)
     */
    private void setFeatureCvTermPubs(Collection<FeatureCvTermPub> featureCvTermPubs) {
        this.featureCvTermPubs = featureCvTermPubs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#getFeatureCvtermDbxrefs()
     */
    private Collection<FeatureCvTermDbXRef> getFeatureCvTermDbXRefs() {
        return this.featureCvTermDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermI#setFeatureCvtermDbxrefs(java.util.Set)
     */
    private void setFeatureCvTermDbXRefs(Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs) {
        this.featureCvTermDbXRefs = featureCvTermDbXRefs;
    }

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}




}


