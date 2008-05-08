package org.gmod.schema.sequence;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.gmod.schema.analysis.AnalysisFeature;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.utils.CollectionUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;


@Entity
@Table(name="feature")
@Indexed
public class Feature implements java.io.Serializable {

    @GenericGenerator(name="generator", strategy="seqhilo", parameters = {  @Parameter(name="max_lo", value="100"), @Parameter(name="sequence", value="feature_feature_id_seq") } )
    @Id @GeneratedValue(generator="generator")
    @Column(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
    private int featureId;

    @ManyToOne(cascade={})
    @JoinColumn(name="organism_id", unique=false, nullable=false, insertable=true, updatable=true)
    @IndexedEmbedded(depth=1)
    private Organism organism;

    @ManyToOne(cascade={})
    @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
    @IndexedEmbedded(depth=2)
    private CvTerm cvTerm;

    @Column(name="name", unique=false, nullable=true, insertable=true, updatable=true)
    @Field(index=Index.TOKENIZED, store=Store.YES)
    private String name;

    @Column(name="uniquename", unique=false, nullable=false, insertable=true, updatable=true)
    @Field(index=Index.TOKENIZED, store=Store.YES)
    private String uniqueName;

    @Column(name="seqlen", unique=false, nullable=true, insertable=true, updatable=true)
    private Integer seqLen = -1;

    @Column(name="md5checksum", unique=false, nullable=true, insertable=true, updatable=true, length=32)
    private String md5Checksum;

    @Column(name="is_analysis", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean analysis;

    @Column(name="is_obsolete", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean obsolete;

    @Column(name="timeaccessioned", unique=false, nullable=false, insertable=true, updatable=true, length=29)
    private Timestamp timeAccessioned;

    @Column(name="timelastmodified", unique=false, nullable=false, insertable=true, updatable=true, length=29)
    private Timestamp timeLastModified;

    // -------------------------------------------------------------------------------
    // Unsorted properties below here

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER, mappedBy="feature")
    private Collection<Phylonode> phylonodes;

    @ManyToOne(cascade={}, fetch=FetchType.EAGER)
    @JoinColumn(name="dbxref_id", unique=false, nullable=true, insertable=true, updatable=true)
    private DbXRef dbXRef;

    @Column(name="residues", unique=false, nullable=true, insertable=true, updatable=true)
    private byte residues[];

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="featureBySrcfeatureId")
    @OrderBy("rank ASC")
    private List<FeatureLoc> featureLocsForSrcFeatureId;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="featureByObjectId")
    private Collection<FeatureRelationship> featureRelationshipsForObjectId;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="featureBySubjectId")
    private Collection<FeatureRelationship> featureRelationshipsForSubjectId;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="feature")
    private Collection<FeatureDbXRef> featureDbXRefs;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="featureByFeatureId")
    private Collection<FeatureLoc> featureLocsForFeatureId;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="feature")
    private Collection<FeatureCvTerm> featureCvTerms;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="feature")
    //@Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    private Collection<FeatureProp> featureProps;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="feature")
    private Collection<FeaturePub> featurePubs;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="feature")
    private Collection<AnalysisFeature> analysisFeatures;

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="feature")
    private Collection<FeatureSynonym> featureSynonyms;
    
    /**
     * This featureLoc field does not participate in the Hibernate mapping.
     * It's provided as a convenience for the client, and can be used to
     * cache a FeatureLoc of interest, but is neither automatically populated
     * nor persisted. It is (at the time of writing) used only by Artemis.
     */
    private FeatureLoc featureLoc;

    private Logger logger = Logger.getLogger(Feature.class);

    // Constructors

    /** default constructor */
    public Feature() {
    }

	/** minimal constructor */
    public Feature(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis, boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        this.organism = organism;
        this.cvTerm = cvTerm;
        this.uniqueName = uniqueName;
        this.analysis = analysis;
        this.obsolete = obsolete;
        this.timeAccessioned = timeAccessioned;
        this.timeLastModified = timeLastModified;
    }

    
    // Property accessors

    public int getFeatureId() {
        return this.featureId;
    }
    
    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public Organism getOrganism() {
        return this.organism;
    }
    
    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    public void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    /**
     * Get the human-readable form of the feature eg the gene name
     * 
     * @return the name, may be null
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Set the human-readable form of the feature eg the gene name
     * 
     * @param name the human-readable name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Fetch the unique name (systematic id) for the feature 
     * 
     * @return the unique name, not null
     */
    public String getUniqueName() {
        return this.uniqueName;
    }
    
    /**
     * Set the unique name (systematic id) for the feature
     * 
     * @param uniqueName the unique name, not null
     */
    public void setUniqueName(String uniqueName) {
        if (uniqueName  == null)
            throw new IllegalArgumentException("setUniqueName: the unique name cannot be null");
        this.uniqueName = uniqueName;
    }
    
    public byte[] getResidues() {
        return this.residues;
    }
    
    /**
     * Fetch a subset of the sequence (may be lazy) 
     * 
     * @param min the lower bound, in interbase coordinates
     * @param max the upper bound, in interbase coordinates
     * @return
     */
    public byte[] getResidues(int min, int max) {
        byte[] results = new byte[max - min];
        System.arraycopy(getResidues(), 0, results, 0, max);
        return results;
    }
    
    public void setResidues(byte[] residues) {
        this.residues = residues;
        if (residues == null) {
            seqLen = 0;
            md5Checksum = "";
            return;
        }
        seqLen = residues.length;
        this.md5Checksum = calcMD5(this.residues);
    }
    

    /**
     * Fetch the length of the sequence. Find it from the parent if necessary 
     * 
     * @return the length
     */
    public int getSeqLen() {
        if (this.seqLen.intValue() == -1 && residues != null) {
            return getResidues().length;
        }
        return this.seqLen.intValue();
    }
    
    
    public void setSeqLen(Integer seqLen) {
        this.seqLen = seqLen;
    }
    

    public String getMd5Checksum() {
        return this.md5Checksum;
    }
    
    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }
    

    public boolean isAnalysis() {
        return this.analysis;
    }
    
    public void setAnalysis(boolean analysis) {
        this.analysis = analysis;
    }
    

    public boolean isObsolete() {
        return this.obsolete;
    }
    
    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }
    

    public Date getTimeAccessioned() {
        return this.timeAccessioned;
    }
    
    public void setTimeAccessioned(Timestamp timeAccessioned) {
        this.timeAccessioned = timeAccessioned;
    }
    

    public Timestamp getTimeLastModified() {
        return this.timeLastModified;
    }
    
    public void setTimeLastModified(Timestamp timeLastModified) {
        this.timeLastModified = timeLastModified;
    }
    
    public List<FeatureLoc> getFeatureLocsForSrcFeatureId() {
        return (featureLocsForSrcFeatureId = CollectionUtils.safeGetter(featureLocsForSrcFeatureId));
    }
    
    /**
     * Returns the unique rank=0 FeatureLoc associated with this feature.
     * Every feature should have one, so this method will not return null
     * unless something is wrong in the database.
     * 
     * @return the unique rank=0 FeatureLoc associated with this feature
     */
    public FeatureLoc getRankZeroFeatureLoc()
    {
        List<FeatureLoc> featureLocs = getFeatureLocsForSrcFeatureId();
        if (featureLocs.size() == 0) {
            logger.error(String.format("getRankZeroFeatureLoc: Feature '%s' has no FeatureLocs", uniqueName));
            return null;
        }
        return featureLocs.get(0);
    }
    
    public void setFeatureLocsForSrcFeatureId(List<FeatureLoc> featureLocsForSrcFeatureId) {
        this.featureLocsForSrcFeatureId = featureLocsForSrcFeatureId;
    }

    public Collection<FeatureRelationship> getFeatureRelationshipsForObjectId() {
        return (featureRelationshipsForObjectId = CollectionUtils.safeGetter(featureRelationshipsForObjectId));
    }
    
    public void setFeatureRelationshipsForObjectId(Collection<FeatureRelationship> featureRelationshipsForObjectId) {
        this.featureRelationshipsForObjectId = featureRelationshipsForObjectId;
    }

    public Collection<FeatureRelationship> getFeatureRelationshipsForSubjectId() {
        return (featureRelationshipsForSubjectId = CollectionUtils.safeGetter(featureRelationshipsForSubjectId));
    }
    
    public void setFeatureRelationshipsForSubjectId(Collection<FeatureRelationship> featureRelationshipsForSubjectId) {
        this.featureRelationshipsForSubjectId = featureRelationshipsForSubjectId;
    }
    
    public void addFeatureRelationshipsForSubjectId(FeatureRelationship featureRelationshipForSubjectId) {
      featureRelationshipForSubjectId.setFeatureBySubjectId(this);
      this.featureRelationshipsForSubjectId.add(featureRelationshipForSubjectId);
    }

    public Collection<FeatureDbXRef> getFeatureDbXRefs() {
        return this.featureDbXRefs;
    }
    
    public void setFeatureDbXRefs(Collection<FeatureDbXRef> featureDbXRefs) {
        this.featureDbXRefs = featureDbXRefs;
    }

    public Collection<FeatureLoc> getFeatureLocsForFeatureId() {
        return (featureLocsForFeatureId = CollectionUtils.safeGetter(featureLocsForFeatureId));
    }
    
    public void addFeatureLocsForFeatureId(FeatureLoc featureLocForFeatureId) {
        featureLocForFeatureId.setFeatureByFeatureId(this);
        getFeatureLocsForFeatureId().add(featureLocForFeatureId);
    }

    public Collection<FeatureCvTerm> getFeatureCvTerms() {
        return this.featureCvTerms;
    }
    
    public void setFeatureCvTerms(Collection<FeatureCvTerm> featureCvTerms) {
        this.featureCvTerms = featureCvTerms;
    }

    public Collection<FeatureProp> getFeatureProps() {
        return (featureProps = CollectionUtils.safeGetter(featureProps));
    }
    
    public void addFeatureProp(FeatureProp featureProp) {
        featureProp.setFeature(this);
        getFeatureProps().add(featureProp);
    }

    public Collection<FeaturePub> getFeaturePubs() {
        return this.featurePubs;
    }
    
    public void setFeaturePubs(Collection<FeaturePub> featurePubs) {
        this.featurePubs = featurePubs;
    }

    public Collection<AnalysisFeature> getAnalysisFeatures() {
        return this.analysisFeatures;
    }
    
    public void setAnalysisFeatures(Collection<AnalysisFeature> analysisFeatures) {
        this.analysisFeatures = analysisFeatures;
    }

    public Collection<FeatureSynonym> getFeatureSynonyms() {
        return (featureSynonyms = CollectionUtils.safeGetter(featureSynonyms));
    }
    
    public void setFeatureSynonyms(Collection<FeatureSynonym> featureSynonyms) {
        this.featureSynonyms = featureSynonyms;
    }

    /**
     * Get the display name for the gene, preferrably the name, 
     * otherwise the display name   
     * 
     * @return the preferred display name, never null 
     */
    public String getDisplayName() {
        return (getName() != null) ? getName() : getUniqueName(); 
    }

    public void setFeatureLocsForFeatureId(Collection<FeatureLoc> featureLocsForFeatureId) {
        this.featureLocsForFeatureId = featureLocsForFeatureId;
    }

    public void setFeatureProps(Collection<FeatureProp> featureProps) {
        this.featureProps = featureProps;
    }
    

    public Collection<Phylonode> getPhylonodes() {
        return this.phylonodes;
    }

    public void setPhylonodes(Collection<Phylonode> phylonodes) {
        this.phylonodes = phylonodes;
    }

    private String calcMD5(byte[] residue) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
//          MessageDigest tc1 = md.clone();
            byte[] md5Bytes = md5.digest(residue);

            StringBuilder hexValue = new StringBuilder();
            for (int i=0 ; i<md5Bytes.length ; i++) {
                int val = md5Bytes[i] & 0xff; 
                if (val < 16) hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        }
        catch (NoSuchAlgorithmException exp) {
            // Shouldn't happen - MD5 is a supported algorithm
            throw new RuntimeException("Could not find MD5 algorithm", exp);
        }
    }
    
    /**
     * Returns the value of the featureLoc field.
     * 
     * This featureLoc field does not participate in the Hibernate mapping.
     * It's provided as a convenience for the client, and can be used to
     * cache a FeatureLoc of interest, but is neither automatically populated
     * nor persisted. In particular, it is (at the time of writing) used by Artemis.
     */
    public FeatureLoc getFeatureLoc() {
      return featureLoc;
    }

    /**
     * Sets the value of the featureLoc field.
     * 
     * This featureLoc field does not participate in the Hibernate mapping.
     * It's provided as a convenience for the client, and can be used to
     * cache a FeatureLoc of interest, but is neither automatically populated
     * nor persisted. In particular, it is (at the time of writing) used by Artemis.
     */
    public void setFeatureLoc(FeatureLoc featureLoc) {
      this.featureLoc = featureLoc;
    }

}
