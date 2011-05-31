package org.gmod.schema.mapped;

import org.genedb.db.analyzers.AllNamesAnalyzer;
import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.helpers.LocationBridge;
import org.genedb.util.SequenceUtils;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProteinMatch;
import org.gmod.schema.feature.Region;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.utils.CollectionUtils;
import org.gmod.schema.utils.SimilarityI;
import org.gmod.schema.utils.StrandedLocation;

import org.apache.log4j.Logger;
import org.apache.solr.analysis.ISOLatin1AccentFilterFactory;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.testng.v6.Lists;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;


@Configurable
@Entity
/*
 * We have wondered whether to add the annotation
 *
 *   @Proxy(lazy=false)
 *
 * here. This would prevent proxy objects being created for Feature
 * entities - though not for subclasses. For example, if we had that
 * annotation then session.load(Feature.class, featureId) would return
 * a proper Feature object of the appropriate class, rather than a
 * proxy subclass of Feature. This would allow instanceof checks to
 * work as expected.
 *
 * The danger is that we might be implicitly relying on proxy creation
 * without realising it, and that this change might cause unanticipated
 * problems. Instead we have made a couple of other changes that will
 * alleviate problems in practice: using session.get rather than session.load
 * in genedb-web's IndexSynchroniser; and adding explicit checks for
 * proxies to the getSubjectFeature and getObjectFeature methods of
 * FeatureRelationship.
 *
 * This decision should be kept under review.
 *
 * -rh11, 2009-05-06
 *
 * Note that some methods are synchronized on internal locks. However most of the
 * code base assumes a single-threading model. So it's safer to synchronize externally
 * if necessary.
 *
 *
 *
 */
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_id")
@Table(name = "feature")
@Indexed
public abstract class Feature implements java.io.Serializable, HasPubsAndDbXRefs {

    @Autowired
    protected transient CvDao cvDao;

    @Autowired
    protected transient GeneralDao generalDao;

    @Autowired
    protected transient SequenceDao sequenceDao;

    @Autowired
    @Qualifier("sessionFactory")
    protected transient SessionFactory sessionFactory;

    @GenericGenerator(name = "generator", strategy = "seqhilo", parameters = {
            @Parameter(name = "max_lo", value = "100"),
            @Parameter(name = "sequence", value = "feature_feature_id_seq") })
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "feature_id", unique = true, nullable = false, insertable = true, updatable = false)
    @DocumentId
    private int featureId;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "organism_id", unique = false, nullable = false, insertable = true, updatable = true)
    @IndexedEmbedded(depth = 1)
    private Organism organism;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = false, updatable = false)
    @IndexedEmbedded(depth = 2)
    private CvTerm type;

    @Column(name = "name", unique = false, nullable = true, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    private String name;

    @Column(name = "uniquename", unique = false, nullable = false, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    private String uniqueName;

    @Column(name = "seqlen", unique = false, nullable = true, insertable = true, updatable = true)
    private Integer seqLen = -1;

    @Column(name = "md5checksum", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    private String md5Checksum;

    @Column(name = "is_analysis", unique = false, nullable = false, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED, store = Store.NO)
    private boolean analysis;

    @Column(name = "is_obsolete", unique = false, nullable = false, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    private boolean obsolete;

    @Column(name = "timeaccessioned", unique = false, nullable = false, insertable = true, updatable = true, length = 29)
    private Timestamp timeAccessioned;

    @Column(name = "timelastmodified", unique = false, nullable = false, insertable = true, updatable = true, length = 29)
    private Timestamp timeLastModified;

    // -------------------------------------------------------------------------------
    // Unsorted properties below here

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "feature")
    private Collection<Phylonode> phylonodes;

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "dbxref_id", unique = false, nullable = true, insertable = true, updatable = true)
    private DbXRef dbXRef;

    @Column(name = "residues", unique = false, nullable = true, insertable = true, updatable = true)
    @Basic(fetch = FetchType.LAZY)
    private String residues;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "sourceFeature")
    private Set<FeatureLoc> featureLocsForSrcFeatureId;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "objectFeature")
    protected Set<FeatureRelationship> featureRelationshipsForObjectId;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "subjectFeature")
    protected Set<FeatureRelationship> featureRelationshipsForSubjectId;


    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    private Set<FeatureDbXRef> featureDbXRefs;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @OrderBy("locGroup ASC, rank ASC")
    private List<FeatureLoc> featureLocs;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<FeatureCvTerm> featureCvTerms;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<FeatureProp> featureProps;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<FeaturePub> featurePubs;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    private Set<AnalysisFeature> analysisFeatures;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "feature")
    private Set<FeatureSynonym> featureSynonyms;

    @Transient
    private Logger logger = Logger.getLogger(Feature.class);

    // Constructors

    /** default constructor */
    public Feature() {
        // Deliberately empty
    }

    /** minimal constructor */
    public Feature(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        this.organism = organism;
        this.type = cvTerm;
        this.uniqueName = uniqueName;
        this.analysis = analysis;
        this.obsolete = obsolete;
        this.timeAccessioned = timeAccessioned;
        this.timeLastModified = timeLastModified;
    }

    protected Feature(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        // Note that this constructor does not initialise type. The type_id column
        // will still be correctly populated when this object is persisted, because it's
        // the discriminator
        this.organism = organism;
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

    public Organism getOrganism() {
        return this.organism;
    }

    public CvTerm getType() {
        return this.type;
    }

    void setType(CvTerm type) {
        this.type = type;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    public void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    /**
     * Get the human-readable name of the feature, such as the gene name
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
     * TODO Should update synonyms table
     */
    public void setUniqueName(String uniqueName) {
        if (uniqueName == null) {
            throw new NullPointerException("setUniqueName: the unique name cannot be null");
        }
        this.uniqueName = uniqueName;
    }

    public String getResidues() {
        return this.residues;
    }

    /**
     * Fetch a subset of the sequence (may be lazy)
     *
     * @param min the lower bound, in interbase coordinates
     * @param max the upper bound, in interbase coordinates
     * @return
     */
    public String getResidues(int min, int max) {
        return getResidues(min, max, false);
    }

    /**
     * Fetch a substring of the sequence, possibly reverse-complemented
     * @param min the lower bound, in interbase coordinates
     * @param max the upper bound, in interbase coordinates
     * @param reverseComplement whether to take the reverse complement
     * @return the subsequence
     */
    public String getResidues(int min, int max, boolean reverseComplement) {
        String sequence = getResidues().substring(min, max);
        if (reverseComplement) {
            return SequenceUtils.reverseComplement(sequence);
        }
        return sequence;
    }

    public void setResidues(String residues) {
        this.residues = residues;
        if (residues == null) {
            seqLen = 0;
            md5Checksum = "";
            return;
        }
        seqLen = residues.length();
        this.md5Checksum = calcMD5(this.residues);
    }

    /**
     * Fetch the length of the sequence. Find it from the parent if necessary
     *
     * @return the length
     */
    public int getSeqLen() {
        if (this.seqLen != null && this.seqLen.intValue() == -1 && residues != null) {
            return getResidues().length();
        }
        if (this.seqLen == null) {
            return 0;
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

    /**
     * Return the last modified timestamp, if the feature has been modified since loading. This is defined as the 'time last modified' =
     * 'time last accessioned'.
     *
     * @return the time last modified, or null if the feature hasn't been modified since creation
     */

    public Timestamp getTimeLastModified() {
	if (this.timeLastModified.compareTo(this.timeAccessioned) == 0) {
	    return null;
	}
        return this.timeLastModified;
    }

    public void setTimeLastModified(Timestamp timeLastModified) {
        this.timeLastModified = timeLastModified;
    }

    @Transient
    private Object featureLocsForSrcFeatureIdLock = new Object();

    public Collection<FeatureLoc> getFeatureLocsForSrcFeatureId() {
        synchronized (featureLocsForSrcFeatureIdLock) {
            if (featureLocsForSrcFeatureId == null) {
                featureLocsForSrcFeatureId = new HashSet<FeatureLoc>();
            }
            return featureLocsForSrcFeatureId;
        }
    }

    /**
     * Returns the unique rank=0 FeatureLoc associated with this feature. Every
     * feature should have one, so this method will not return null unless
     * something is wrong in the database.
     *
     * @return the unique rank=0 FeatureLoc associated with this feature
     */
    @Transient
    public FeatureLoc getRankZeroFeatureLoc() {
        List<FeatureLoc> featureLocs = getFeatureLocs();
        if (featureLocs.size() == 0) {
            logger.error(String.format("getRankZeroFeatureLoc: Feature '%s' has no FeatureLocs",
                uniqueName));
            return null;
        }
        for (FeatureLoc featureLoc : featureLocs) {
            if (featureLoc == null) {
                logger.warn(String.format("Feature '%s' has a null featureLoc", uniqueName));
            } else {
                return featureLoc;
            }
        }
        logger.error(String.format("Feature '%s' has no non-null featureLocs", uniqueName));
        return null;
    }

    public Collection<FeatureRelationship> getFeatureRelationshipsForObjectId() {
        if (featureRelationshipsForObjectId == null) {
            featureRelationshipsForObjectId = new HashSet<FeatureRelationship>();
        }
        return featureRelationshipsForObjectId;
    }

    public Collection<FeatureRelationship> getFeatureRelationshipsForSubjectId() {
        return (featureRelationshipsForSubjectId = CollectionUtils
                .safeGetter(featureRelationshipsForSubjectId));
    }

    public void addFeatureRelationshipsForSubjectId(
            FeatureRelationship featureRelationshipForSubjectId) {
        if (featureRelationshipsForSubjectId == null) {
            featureRelationshipsForSubjectId = new HashSet<FeatureRelationship>();
        }
        featureRelationshipForSubjectId.setSubjectFeature(this);
        this.featureRelationshipsForSubjectId.add(featureRelationshipForSubjectId);
    }

    public void addFeatureRelationshipsForObjectId(
            FeatureRelationship featureRelationshipForObjectId) {
        if (featureRelationshipsForObjectId == null) {
            featureRelationshipsForObjectId = new HashSet<FeatureRelationship>();
        }
        featureRelationshipForObjectId.setObjectFeature(this);
        this.featureRelationshipsForObjectId.add(featureRelationshipForObjectId);
    }

    public Collection<FeatureDbXRef> getFeatureDbXRefs() {
        return Collections.unmodifiableCollection(this.featureDbXRefs);
    }

    public void addFeatureDbXRef(FeatureDbXRef featureDbXRef) {
        if (this.featureDbXRefs == null) {
            this.featureDbXRefs = new HashSet<FeatureDbXRef>();
        }
        this.featureDbXRefs.add(featureDbXRef);
        featureDbXRef.setFeature(this);
    }

    public FeatureDbXRef addDbXRef(DbXRef dbXRef) {
        return addDbXRef(dbXRef, true);
    }

    public FeatureDbXRef addDbXRef(DbXRef dbXRef, boolean current) {
        FeatureDbXRef featureDbXRef = new FeatureDbXRef(dbXRef, this, current);
        addFeatureDbXRef(featureDbXRef);
        return featureDbXRef;
    }

    @Transient
    private Object featureLocsLock = new Object();

    public List<FeatureLoc> getFeatureLocs() {
        synchronized (featureLocsLock) {
            return (featureLocs = CollectionUtils.safeGetter(featureLocs));
        }
    }

    public FeatureLoc getFeatureLoc(int locGroup, int rank) {
        synchronized (featureLocsLock) {
            for (FeatureLoc featureLoc: getFeatureLocs()) {
                if (featureLoc.getLocGroup() == locGroup && featureLoc.getRank() == rank) {
                    return featureLoc;
                }
            }
        }
        return null;
    }

    //Added on 27.8.2009 by nds (remove if redundant)
    public FeatureLoc getFeatureLocOnThisSrcFeature(Feature srcFeature){
        synchronized (featureLocsLock) {
            for (FeatureLoc featureLoc: getFeatureLocs()) {
                if (featureLoc.getSourceFeature().getFeatureId() == srcFeature.getFeatureId()) {
                    return featureLoc;
                }
            }
        }
        return null;

    }

    public void addFeatureLoc(FeatureLoc featureLoc) {
        synchronized (featureLocsLock) {
            featureLoc.setFeature(this);
            getFeatureLocs().add(featureLoc);
        }
    }

    protected void removeFeatureLoc(FeatureLoc featureLoc) {
        synchronized (featureLocsLock) {
            Iterator<FeatureLoc> it = featureLocs.iterator();
            while (it.hasNext()) {
                if (it.next().getFeatureLocId() == featureLoc.getFeatureLocId()) {
                    logger.trace(String.format("Removing FeatureLoc (ID %d) from feature '%s'",
                        featureLoc.getFeatureLocId(), getUniqueName()));
                    it.remove();
                }
            }
        }
        featureLoc.setSourceFeature(null);
    }

    @Transient
    private Object featureCvTermsLock = new Object();

    public Collection<FeatureCvTerm> getFeatureCvTerms() {
        synchronized (featureCvTermsLock) {
            if (this.featureCvTerms == null) {
                featureCvTerms = new HashSet<FeatureCvTerm>();
            }
            return this.featureCvTerms;
        }
    }
    
    
    public void addFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        synchronized (featureCvTermsLock) {
            if (this.featureCvTerms == null) {
                featureCvTerms = new HashSet<FeatureCvTerm>();
            }
            featureCvTerms.add(featureCvTerm);
        }
    }

    @Transient
    private Object featurePropsLock = new Object();

    public Collection<FeatureProp> getFeatureProps() {
        return (featureProps = CollectionUtils.safeGetter(featureProps));
    }

    public void addFeatureProp(FeatureProp featureProp) {
        featureProp.setFeature(this);
        getFeatureProps().add(featureProp);
    }

    public void removeFeatureProp(String cv, String term){
        synchronized (featurePropsLock) {
                Iterator<FeatureProp> it = getFeatureProps().iterator();
                while (it.hasNext()) {
                    FeatureProp current = it.next();
                    if ((current.getType().getName().equals(term)) && (current.getType().getCv().getName()).equals(cv)) {
                        logger.trace(String.format("Removing FeatureProp (ID %d) from feature '%s'",
                            current.getFeaturePropId(), getUniqueName()));
                        it.remove();
                    }
                }
            }

    }

    @Transient
    private Object featurePubsLock = new Object();

    public Collection<FeaturePub> getFeaturePubs() {
        synchronized(featurePubsLock) {
            if (featurePubs == null) {
                featurePubs = new HashSet<FeaturePub>();
            }
            return Collections.unmodifiableCollection(this.featurePubs);
        }
    }

    public void addFeaturePub(FeaturePub featurePub) {
        synchronized(featurePubsLock) {
            if (featurePubs == null) {
                featurePubs = new HashSet<FeaturePub>();
            }
            this.featurePubs.add(featurePub);
            featurePub.setFeature(this);
        }
    }

    public FeaturePub addPub(Pub pub) {
        logger.trace(String.format("Adding pub '%s' to %s",
            pub.getUniqueName(), this.toString()));
        synchronized(featurePubsLock) {
            if (featurePubs == null) {
                featurePubs = new HashSet<FeaturePub>();
            }
            FeaturePub featurePub = new FeaturePub(this, pub);
            this.featurePubs.add(featurePub);
            return featurePub;
        }
    }

    @Transient
    public Collection<Pub> getPubs() {
        synchronized(featurePubsLock) {
            if (featurePubs == null) {
                featurePubs = new HashSet<FeaturePub>();
            }

            Collection<Pub> pubs = new HashSet<Pub>();
            for(FeaturePub featurePub: this.featurePubs) {
                pubs.add(featurePub.getPub());
            }

            return pubs;
        }
    }

    @Transient
    private Object featureSynonymsLock = new Object();

    public Collection<FeatureSynonym> getFeatureSynonyms() {
        synchronized (featureSynonymsLock) {
            if (featureSynonyms == null) {
                featureSynonyms = new HashSet<FeatureSynonym>();
            }
            return Collections.unmodifiableCollection(featureSynonyms);
        }
    }

    public void addFeatureSynonym(FeatureSynonym featureSynonym) {
        synchronized(featureSynonymsLock) {
            if (featureSynonyms == null) {
                featureSynonyms = new HashSet<FeatureSynonym>();
            }
            this.featureSynonyms.add(featureSynonym);
            featureSynonym.setFeature(this);
        }
    }

    /**
     * Get the display name for the gene: preferably the name, otherwise the
     * display name
     *
     * @return the preferred display name, never null
     */
    @Transient
    public String getDisplayName() {
        return (getName() != null) ? getName() : getUniqueName();
    }

    @Transient
    public Collection<String> getPreviousSystematicIds() {
        Set<String> ret = new HashSet<String>();
        for (FeatureSynonym featureSynonym: getFeatureSynonyms()) {
            Synonym synonym = featureSynonym.getSynonym();
            if (("previous_systematic_id".equals(synonym.getType().getName()))
            && !featureSynonym.isCurrent()) {
                ret.add(synonym.getSynonymSGML());
            }
        }
        return ret;
    }

    /**
     * Get all synonyms, of any type.
     *
     * @return a collection of synonym objects
     */
    @Transient
    public Collection<Synonym> getSynonyms() {
        Collection<Synonym> ret = new HashSet<Synonym>();
        for (FeatureSynonym featureSynonym: getFeatureSynonyms()) {
            ret.add(featureSynonym.getSynonym());
        }
        return ret;
    }

    @Transient
    public Collection<Synonym> getSynonyms(String type) {
        Collection<Synonym> ret = new HashSet<Synonym>();
        for (FeatureSynonym featureSynonym: getFeatureSynonyms()) {
            Synonym synonym = featureSynonym.getSynonym();
            if (synonym.getType().getName().equals(type)) {
                ret.add(synonym);
            }
        }
        return ret;
    }

    @Transient
    public <T extends Feature> Collection<T> getRelatedFeatures(Class<T> featureClass,
        String relationshipTypeCvName, String relationshipTypeCvTerm) {

        Collection<T> relatedFeatures = new HashSet<T>();

        for(FeatureRelationship featureRelationship: getFeatureRelationshipsForSubjectIdFilteredByCvNameAndTermName(relationshipTypeCvName, relationshipTypeCvTerm)) {
            Feature object = featureRelationship.getObjectFeature();
            if (featureClass.isInstance(object)) {
                relatedFeatures.add(featureClass.cast(object));
            }
        }

        return relatedFeatures;
    }

    public Collection<Phylonode> getPhylonodes() {
        return this.phylonodes;
    }

    private String calcMD5(String residue) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            StringBuilder hexValue = new StringBuilder();
            for (byte b: md5.digest(residue.getBytes())) {
                hexValue.append(String.format("%02x", b));
            }

            return hexValue.toString();
        } catch (NoSuchAlgorithmException exp) {
            // Shouldn't happen - MD5 is a supported algorithm
            throw new RuntimeException("Could not find MD5 algorithm", exp);
        }
    }

    @Transient
    @Field(name = "synonym", index = Index.TOKENIZED, store = Store.YES)
    @Analyzer(impl = AllNamesAnalyzer.class)
	protected String getSynonymsAsSpaceSeparatedString() {
        List<String> synonyms = Lists.newArrayList();
        for (Synonym synonym: getSynonyms()) {
            synonyms.add(synonym.getName());
        }
        return allNamesSupport(synonyms);
    }

    /**
     * A string containing all the names by which this feature is known;
     * indexed by Lucene and used for searches.
     *
     * @return
     */
    @Transient
    @Field(name = "allNames", index = Index.TOKENIZED, store = Store.YES)
    @Analyzer(impl = AllNamesAnalyzer.class)
    public String getAllNames() {
    	List<String> names = generateNamesList();
        
        /*
         * Commented by gv1.
         * 
         * This method (commented out below) for getting the gene name using the colon (:) convention doesn't
         * work for recently loaded genes that follow a dot (.) convention for different transcripts. Extending
         * the same approach for dots won't work easily because dots are used throughout names, and relying on
         * naming conventions is an approach clearly fails when the conventions change.
         * 
         * Instead, one should override the generateNamesList method to in Feature types that require
         * custom all names entries. This has been done in the <Polypeptide> and <Transcript> classes.
         * 
         */
        //        if (sysId.indexOf(':') != -1) {
        //            names.add(sysId.substring(0, sysId.lastIndexOf(':')));
        //        }

        StringBuilder ret = new StringBuilder(allNamesSupport(names));
        ret.append(" ");
        ret.append(getSynonymsAsSpaceSeparatedString());
        return ret.toString();
    }
    
    /**
     * Override this to generate a custom names list for a feature.
     * 
     * @return a list of names
     */
    protected List<String> generateNamesList() {
    	List<String> names = Lists.newArrayList();
        if (getName() != null) {
            names.add(getName());
        }
        String sysId = getUniqueName();
        names.add(sysId);
        return names;
    }

    protected String allNamesSupport(List<String> names) {
    	List<String> newNames = Lists.newArrayList();
    	for (String name : names) {
			if (name.contains("-")) {
				newNames.add(name.replaceAll("-", ""));
			}
		}
    	names.addAll(newNames);
    	return StringUtils.collectionToDelimitedString(names, " ");
    }

    @Transient
    @Field(name = "start", store = Store.YES)
    @FieldBridge(impl = LocationBridge.class)
    public int getStart() {
        FeatureLoc loc = getRankZeroFeatureLoc();
        if (loc == null) {
            return 0;
        }
        return loc.getFmin();
    }

    @Transient
    @Field(name = "stop", store = Store.YES)
    @FieldBridge(impl = LocationBridge.class)
    public int getStop() {
        return getRankZeroFeatureLoc().getFmax();
    }

    @Transient
    @Field(name = "strand", index=Index.UN_TOKENIZED, store = Store.YES)
    public int getStrand() {
        FeatureLoc loc = getRankZeroFeatureLoc();
        if (loc == null) {
            return 0;
        }
        Short strand = loc.getStrand();
        if (strand != null) {
            return strand;
        }
        return 0;
    }

    @Transient
    @Field(name = "chr", index=Index.UN_TOKENIZED, store = Store.YES)
    String getChr() {
        FeatureLoc loc = getRankZeroFeatureLoc();
        if (loc == null) {
            return null;
        }
        return loc.getSourceFeature().getUniqueName();
    }

    @Transient
    @Field(name = "chrId", index=Index.UN_TOKENIZED, store = Store.YES)
    int getChrId() {
        FeatureLoc loc = getRankZeroFeatureLoc();
        if (loc == null) {
            return -1;
        }
        return loc.getSourceFeature().getFeatureId();
    }

    @Transient
    @Field(name = "chrlen", store = Store.YES)
    int getChrLen() {
        FeatureLoc loc = getRankZeroFeatureLoc();
        if (loc == null) {
            return 0;
        }
        return loc.getSourceFeature().getSeqLen();
    }

    /**
     * Get the value of a feature property with the given CV name and term name.
     * Note that this method does not make it possible to distinguish between
     * a missing property and a property with a <code>null</code> value. See
     * {@link #hasProperty(String,String)}.
     *
     * @param cvName the name of the controlled vocabulary
     * @param termName the name of the term
     * @return the value of the property, or <code>null</code> if there is no such property
     */
    @Transient
    public String getProperty(String cvName, String termName) {
        for (FeatureProp featureProp : this.getFeatureProps()) {
            CvTerm term = featureProp.getType();
            if (term.getCv().getName().equals(cvName) && term.getName().equals(termName)) {
                return featureProp.getValue();
            }
        }
        return null;
    }

    /**
     * Test whether this feature has a property with the given CV name and term name.
     *
     * @param cvName the name of the controlled vocabulary
     * @param termName the name of the term
     * @return <code>true</code> if there is such a property, or <code>false</code> if not
     */
    @Transient
    public boolean hasProperty(String cvName, String termName) {
        for (FeatureProp featureProp : this.getFeatureProps()) {
            CvTerm term = featureProp.getType();
            if (term.getCv().getName().equals(cvName) && term.getName().equals(termName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a FeatureRelationship of which this Feature is the object.
     *
     * @param subject the subject of the relationship
     * @param cvName the CV to which the relationship type belongs
     * @param termName the CV term denoting the relationship type
     * @return the newly-created FeatureRelationship object
     */
    protected FeatureRelationship addFeatureRelationship(Feature subject, String cvName,
            String termName) {
        CvTerm type = cvDao.getCvTermByNameAndCvName(termName, cvName);
        if (type == null) {
            throw new RuntimeException(String.format("Failed to find term '%s' in cv '%s'", termName, cvName));
        }
        logger.trace(String.format("Creating feature relationship (type '%s') from '%s' to '%s'",
            type, subject.getUniqueName(), this.getUniqueName()));
        FeatureRelationship relationship =  new FeatureRelationship(subject, this, type, 0);
        if (this.featureRelationshipsForObjectId == null) {
            this.featureRelationshipsForObjectId = new HashSet<FeatureRelationship>();
        }
        this.featureRelationshipsForObjectId.add(relationship);

        if (subject.featureRelationshipsForSubjectId == null) {
            subject.featureRelationshipsForSubjectId = new HashSet<FeatureRelationship>();
        }
        subject.featureRelationshipsForSubjectId.add(relationship);
        return relationship;
    }

    public FeatureLoc addLocatedChild(Feature child, StrandedLocation location) {
        FeatureLoc loc = new FeatureLoc(this, child, location);

        synchronized (featureLocsForSrcFeatureIdLock) {
            if (this.featureLocsForSrcFeatureId == null) {
                this.featureLocsForSrcFeatureId = new HashSet<FeatureLoc>();
            }
            this.featureLocsForSrcFeatureId.add(loc);
        }

        synchronized (child.featureLocsLock) {
            if (child.featureLocs == null) {
                child.featureLocs = new ArrayList<FeatureLoc>();
            }
            child.featureLocs.add(loc);
        }

        return loc;
    }

    public FeatureLoc addLocatedChild(Feature child, int fmin, int fmax) {
        return addLocatedChild(child, fmin, fmax, 0, null);
    }
    public FeatureLoc addLocatedChild(Feature child, int fmin, int fmax, int strand, Integer phase) {
        return addLocatedChild(child, fmin, fmax, strand, phase, 0);
    }
    public FeatureLoc addLocatedChild(Feature child, int fmin, int fmax, int strand, Integer phase, int rank) {
        return addLocatedChild(child, fmin, fmax, strand, phase, 0, rank);
    }
    public FeatureLoc addLocatedChild(Feature child, int fmin, int fmax, int strand, Integer phase, int locgroup, int rank) {
        FeatureLoc loc = new FeatureLoc(this, child, fmin, fmax, strand, phase, locgroup, rank);

        System.err.println(String.format("Adding location for '%s' on '%s' %d-%d strand %d with phase=%s, locgroup=%d, rank=%d",
                child.getUniqueName(), this.getUniqueName(), fmin, fmax, strand, phase, locgroup, rank));

        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Adding location for '%s' on '%s' %d-%d strand %d with phase=%s, locgroup=%d, rank=%d",
                child.getUniqueName(), this.getUniqueName(), fmin, fmax, strand, phase, locgroup, rank));
        }
        if (this.featureLocsForSrcFeatureId == null) {
            this.featureLocsForSrcFeatureId = new HashSet<FeatureLoc>();
        }
        this.featureLocsForSrcFeatureId.add(loc);

        if (child.featureLocs == null) {
            child.featureLocs = new ArrayList<FeatureLoc>();
        }
        child.featureLocs.add(loc);

        /*
         * The following is not currently done, and enabling it would cause loading problems
         * because the redundant locations are also added explicitly in various places.
         *
         * TODO decide whether or not we want these redundant locations automatically added here
         *
        // If we ourselves have featurelocs (e.g. we are a contig and have a supercontig),
        // give the child redundant locations to these.
        if (locgroup == 0) {
            for (FeatureLoc featureLoc: getFeatureLocs()) {
                logger.trace(String.format("Adding redundant featureloc (locgroup=%d) for '%s' on '%s' with rank %d",
                    1 + featureLoc.getLocGroup(), child.getUniqueName(), featureLoc.getSourceFeature().getUniqueName(),
                    rank));

                featureLoc.getSourceFeature().addLocatedChild(child,
                    fmin + featureLoc.getFmin(), fmax + featureLoc.getFmin(), strand, phase, 1 + featureLoc.getLocGroup(), rank);
            }
        }
        */
        return loc;
    }

    public FeatureProp addFeatureProp(String value, String cvName, String termName, int rank) {
        CvTerm type = cvDao.getCvTermByNameAndCvName(termName, cvName);
        if (type == null) {
            throw new RuntimeException(String.format("Failed to find term '%s' in cv '%s'", termName, cvName));
        }
        FeatureProp fp = new FeatureProp(this, type, value, rank);
        if (featureProps == null) {
            featureProps = new HashSet<FeatureProp>();
        }
        this.featureProps.add(fp);
        return fp;
    }

    public List<FeatureProp> getFeaturePropsFilteredByCvNameAndTermName(String cvName, String termName) {
        CvTerm type = cvDao.getCvTermByNameAndCvName(termName, cvName);
        if (type == null) {
            throw new RuntimeException(String.format("Failed to find term '%s' in cv '%s'", termName, cvName));
        }

        List<FeatureProp> ret = new ArrayList<FeatureProp>();
        for (FeatureProp featureProp : getFeatureProps()) {
            if (featureProp.getType().equals(type)) {
                ret.add(featureProp);
            }
        }
        if (ret.size() == 0) {
            return Collections.emptyList();
        }
        return ret;
    }

    /**
     * Get the value of the feature property with the specified type.
     * If there is no such property, return <code>null</code>. If there is
     * more than one such property, throw a RuntimeException.
     *
     * @param cvName    the name of the controlled vocabulary to which the property type belongs
     * @param termName  the property type (within the specified vocabulary)
     * @return the value of the feature property with the specified type,
     *          or <code>null</code> if there is no such property.
     */
    public String getFeatureProp(String cvName, String termName) {
        List<FeatureProp> props = getFeaturePropsFilteredByCvNameAndTermName(cvName, termName);
        if (props.isEmpty()) {
            return null;
        }
        if (props.size() > 1) {
            throw new RuntimeException(String.format("Feature '%s' has more than one '%s:%s' property",
                getUniqueName(), cvName, termName));
        }
        return props.get(0).getValue();
    }


    public FeatureCvTerm addCvTerm(String cvName, String cvTermName) {
        return addCvTerm(cvName, cvTermName, null);
    }

    public FeatureCvTerm addCvTerm(String cvName, String cvTermName, String dbName) {
        return addCvTerm(cvName, cvTermName, dbName, true);
    }
    public FeatureCvTerm addCvTerm(String cvName, String cvTermName, boolean createIfNotFound) {
        return addCvTerm(cvName, cvTermName, null, createIfNotFound);
    }
    public FeatureCvTerm addCvTerm(String cvName, String cvTermName, String dbName, boolean createIfNotFound) {
        if (createIfNotFound) {
            return addCvTerm(cvDao.findOrCreateCvTermByNameAndCvName(cvTermName, cvName, dbName));
        } else {
            CvTerm cvTerm = cvDao.getCvTermByNameAndCvName(cvTermName, cvName);
            if (cvTerm == null) {
                return null;
            }
            return addCvTerm(cvTerm);
        }
    }

    public FeatureCvTerm addCvTerm(CvTerm cvTerm) {
        FeatureCvTerm featureCvTerm = new FeatureCvTerm(cvTerm, this, nullPub(), false, 0);
        addFeatureCvTerm(featureCvTerm);
        return featureCvTerm;
    }

    @Transient
    public Feature getPrimarySourceFeature() {
        FeatureLoc featureLoc = this.getRankZeroFeatureLoc();
        if (featureLoc == null) {
            return null;
        }
        return featureLoc.getSourceFeature();
    }

    @Transient
    public Iterable<Feature> getSourceFeatures() {
        List<Feature> sourceFeatures = new ArrayList<Feature>();
        for (FeatureLoc featureLoc: this.getFeatureLocs()) {
            Feature sourceFeature = featureLoc.getSourceFeature();
            if (sourceFeature == null) {
                logger.warn(String.format("Feature '%s' has a location (FeatureLoc ID %d) with no source feature",
                    this.getUniqueName(), featureLoc.getFeatureLocId()));
            } else {
                sourceFeatures.add(sourceFeature);
            }
        }
        return sourceFeatures;
    }


    public FeatureSynonym addSynonym(String synonymString) {
        return addSynonym("synonym", synonymString);
    }

    /*
     * Avoid the overhead of transaction-creation and synchronisation
     * where possible. This requires (build-time or load-time) AspectJ
     * weaving, because we're calling a transactional method from
     * within this object.
     */
    protected Pub nullPub() {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        return (Pub) session.load(Pub.class, 1);
    }

    protected FeatureSynonym addSynonym(String synonymType, String synonymString) {
        logger.trace(String.format("Adding synonym '%s' of type '%s' to feature '%s'",
            synonymString, synonymType, getUniqueName()));
        return addSynonym(generalDao.getOrCreateSynonym(synonymType, synonymString));
    }

    public FeatureSynonym addSynonym(Synonym synonym) {
        return addSynonym(synonym, true, false);
    }

    public FeatureSynonym addSynonym(Synonym synonym, boolean isCurrent, boolean isInternal) {
        Pub nullPub = nullPub();
        FeatureSynonym featureSynonym = new FeatureSynonym(synonym, this, nullPub , isCurrent, isInternal);
        this.addFeatureSynonym(featureSynonym);
        return featureSynonym;
    }

    /**
     * Move the lower bound of this feature leftwards. The new location is
     * specified relative to the primary source feature, but all <code>FeatureLoc</code>s
     * will be updated by the same relative amount.
     *
     * @param fmin the new <code>fmin</code>, relative to the primary location
     */
    public void lowerFminTo(int fmin) {
        FeatureLoc primaryLoc = getRankZeroFeatureLoc();
        logger.trace(String.format("Feature '%s' starts at %s:%d; lowering to %d",
            getUniqueName(), primaryLoc.getSourceFeature().getUniqueName(), primaryLoc.getFmin(), fmin));

        int lowerByBases = primaryLoc.getFmin() - fmin;
        if (lowerByBases <= 0) {
            logger.trace("Nothing to be done for lowerFminTo");
            return;
        }
        Iterator<FeatureLoc> it = featureLocs.iterator();
        while (it.hasNext()) {
            FeatureLoc featureLoc = it.next();

            int newFmin = featureLoc.getFmin() - lowerByBases;
            if (newFmin < 0) {
                if (featureLoc.getRank() == 0) {
                    throw new RuntimeException(String.format("New fmin (%d) of feature '%s' relative to '%s' is before start",
                        newFmin, getUniqueName(), featureLoc.getSourceFeature().getUniqueName()));
                }

                logger.warn(String.format("Removing secondary location of '%s' on '%s' because new fmin would be %d",
                    getUniqueName(), featureLoc.getSourceFeature().getUniqueName(), newFmin));

                it.remove();
                featureLoc.setSourceFeature(null);
            } else {
                featureLoc.setFmin(newFmin);
            }
        }
    }

    /**
     * Move the upper bound of this feature rightwards. The new location is
     * specified relative to the primary source feature, but all <code>FeatureLoc</code>s
     * will be updated by the same relative amount.
     *
     * @param fmax the new <code>fmax</code>, relative to the primary location
     */
    public void raiseFmaxTo(int fmax) {
        FeatureLoc primaryLoc = getRankZeroFeatureLoc();
        logger.trace(String.format("Feature '%s' stops at %s:%d; raising to %d",
            getUniqueName(), primaryLoc.getSourceFeature().getUniqueName(), primaryLoc.getFmax(), fmax));

        int raiseByBases = fmax - primaryLoc.getFmax();
        if (raiseByBases <= 0) {
            logger.trace("Nothing to be done for raiseFmaxTo");
            return;
        }

        Iterator<FeatureLoc> it = featureLocs.iterator();
        while (it.hasNext()) {
            FeatureLoc featureLoc = it.next();

            int newFmax = featureLoc.getFmax() + raiseByBases;
            if (newFmax > featureLoc.getSourceFeature().getSeqLen()) {
                if (featureLoc.getRank() == 0) {
                    throw new RuntimeException(String.format("New fmax (%d) of feature '%s' relative to '%s' is after end (%d)",
                        newFmax, getUniqueName(), featureLoc.getSourceFeature().getUniqueName(), featureLoc.getSourceFeature().getSeqLen()));
                }

                logger.warn(String.format("Removing secondary location of '%s' on '%s' because new fmax would be %d (source seqlen = %d)",
                    getUniqueName(), featureLoc.getSourceFeature().getUniqueName(), newFmax, featureLoc.getSourceFeature().getSeqLen()));

                it.remove();
                featureLoc.setSourceFeature(null);
            } else {
                featureLoc.setFmax(newFmax);
            }

            generalDao.saveOrUpdate(featureLoc);
        }
    }


    public List<FeatureCvTerm> getFeatureCvTermsFilteredByCvNameStartsWith(String prefix) {
        List<FeatureCvTerm> ret = new ArrayList<FeatureCvTerm>();
        for (FeatureCvTerm featureCvTerm : this.getFeatureCvTerms()) {
            if (featureCvTerm.getCvTerm().getCv().getName().startsWith(prefix)) {
                ret.add(featureCvTerm);
            }
        }
        return ret;
    }

    public List<FeatureRelationship> getFeatureRelationshipsForSubjectIdFilteredByCvNameAndTermName(
            String cvName, String termName) {
        List<FeatureRelationship> ret = new ArrayList<FeatureRelationship>();
        CvTerm type = cvDao.getCvTermByNameAndCvName(termName, cvName);
        if (type == null) {
            throw new RuntimeException(String.format("Failed to find term '%s' in cv '%s'", termName, cvName));
        }
        Collection<FeatureRelationship> featureRels = getFeatureRelationshipsForSubjectId();
        for (FeatureRelationship featureRel : featureRels) {
            if (featureRel.getType().equals(type)) {
                ret.add(featureRel);
            }
        }
        if (ret.size() == 0) {
            return Collections.emptyList();
        }
        return ret;
    }

    public List<FeatureCvTerm> getFeatureCvTermsFilteredByCvNameAndCvTermName(String cvName,
            String termName) {

        CvTerm type = cvDao.getCvTermByNameAndCvName(termName, cvName);
        if (type == null) {
            throw new RuntimeException(String.format("Failed to find term '%s' in cv '%s'", termName, cvName));
        }

        List<FeatureCvTerm> ret = new ArrayList<FeatureCvTerm>();
        for (FeatureCvTerm featureCvTerm : this.getFeatureCvTerms()) {
            if (featureCvTerm.getCvTerm().equals(type)) {
                ret.add(featureCvTerm);
            }
        }
        return ret;
    }

    @Transactional
    public void addSimilarity(SimilarityI similarity) {
        logger.trace(String.format("Adding similarity '%s' to feature '%s' (ID=%d)",
            similarity, getUniqueName(), getFeatureId()));

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        Analysis analysis = similarity.getAnalysis();
        if (analysis == null) {
            analysis = new Analysis();
            analysis.setProgram(similarity.getAnalysisProgram());
            analysis.setProgramVersion(similarity.getAnalysisProgramVersion());
            session.persist(analysis);
        }

        String matchUniqueName = String.format("MATCH_%s", similarity.getUniqueIdentifier());
        ProteinMatch match = new ProteinMatch(getOrganism(), matchUniqueName);
        session.persist(match);

        AnalysisFeature analysisFeature = new AnalysisFeature(analysis, match);
        analysisFeature.setRawScore(similarity.getRawScore());
        analysisFeature.setSignificance(similarity.getEValue());
        analysisFeature.setIdentity(similarity.getId());
        session.persist(analysisFeature);

        match.addFeatureProp(String.format("%.02g", similarity.getUngappedId()), "genedb_misc", "ungapped id", 0);
        match.addFeatureProp(similarity.getOverlap() + " aa overlap", "genedb_misc", "overlap", 0);

        String targetUniqueName = String.format("%s_%s", organism.getCommonName(), similarity.getUniqueIdentifier());
        Region target = new Region(getOrganism(), targetUniqueName, true, false, new Timestamp(System.currentTimeMillis()));
        target.setSeqLen(similarity.getLength());
        target.setDbXRef(similarity.getPrimaryDbXRef());
        for (DbXRef dbXRef: similarity.getSecondaryDbXRefs()) {
            target.addDbXRef(dbXRef);
        }
        session.persist(target);

        target.addFeatureProp(similarity.getOrganismName(), "feature_property", "organism", 0);
        target.addFeatureProp(similarity.getGeneName(), "sequence", "gene", 0);
        target.addFeatureProp(similarity.getProduct(), "genedb_misc", "product", 0);

        this.addLocatedChild  (match, similarity.getQueryStart() - 1,  similarity.getQueryEnd(),
            0 /*strand*/, null /*phase*/, 0 /*locgroup */, 0 /*rank*/);
        target.addLocatedChild(match, similarity.getTargetStart() - 1, similarity.getTargetEnd(),
            0 /*strand*/, null /*phase*/, 0 /*locgroup */, 1 /*rank*/);
    }

    /**
     * Delete this feature from the database.
     * This method is overridden in subclasses to automatically remove
     * dependent features too.
     */
    public void delete() {
        logger.trace(String.format("Deleting feature '%s'", getUniqueName()));
        cvDao.delete(this);
    }

    @Transient
    private Object analysisFeaturesLock = new Object();

    public Collection<AnalysisFeature> getAnalysisFeatures() {
        synchronized(analysisFeaturesLock) {
            if (analysisFeatures == null) {
                analysisFeatures = new HashSet<AnalysisFeature>();
            }
            return Collections.unmodifiableCollection(this.analysisFeatures);
        }
    }

    
    /**
     * Get the AnalysisFeature object associated with this feature, assuming there is at most one.
     * @return the associated AnalysisFeature object, or <code>null</code> if there isn't one.
     * @throws RuntimeException if there is more than one AnalysisFeature associated with this feature
     */
    @Transient
    public AnalysisFeature getAnalysisFeature() {
        synchronized(analysisFeaturesLock) {
            Collection<AnalysisFeature> analysisFeatures = getAnalysisFeatures();
            if (analysisFeatures == null || analysisFeatures.isEmpty()) {
                return null;
            }
            if (analysisFeatures.size() > 1) {
                throw new RuntimeException(String.format("Feature '%s' (ID=%d) has more than one AnalysisFeature",
                    getUniqueName(), getFeatureId()));
            }
            return analysisFeatures.iterator().next();
        }
    }

    /**
     * Create an AnalysisFeature that associates this feature with the specified
     * analysis.
     * @param analysis the analysis. May not be null.
     * @return the newly-created AnalysisFeatuer
     * @throws NullPointerException if the suppled analysis is null
     */
    public AnalysisFeature createAnalysisFeature(Analysis analysis) {
	return this.createAnalysisFeature(analysis, null, null);
    }


    /**
     * Create an AnalysisFeature that associates this feature with the specified
     * analysis.
     * @param analysis the analysis. May not be null.
     * @param score score generated by the analysis as a String. May be null.
     * @param evalue evalue generated by the analysis as a String. May be null.
     * @return the newly-created AnalysisFeature
     * @throws NullPointerException if the suppled analysis is null
     */
    public AnalysisFeature createAnalysisFeature(Analysis analysis, String score, String evalue) {
        if (analysis == null) {
            throw new NullPointerException("Analysis is null in createAnalysisFeature");
        }
        logger.trace(String.format("Adding AnalysisFeature to '%s' (ID=%d)", getUniqueName(), getFeatureId()));
        synchronized(analysisFeaturesLock) {
            AnalysisFeature analysisFeature = new AnalysisFeature(analysis, this);
            if (analysisFeatures == null) {
                analysisFeatures = new HashSet<AnalysisFeature>();
            }
	    if (score != null) {
		double scoreDouble = Double.valueOf(score.trim()).doubleValue();
		analysisFeature.setRawScore(scoreDouble);
	    }
	    if (evalue != null) {
		double evalueDouble = Double.valueOf(evalue.trim()).doubleValue();
		analysisFeature.setSignificance(evalueDouble);
	    }
            analysisFeatures.add(analysisFeature);
            return analysisFeature;
        }
    }
    
    
    @Transient
    @Analyzer(impl = AllNamesAnalyzer.class)
    @Field(name = "dbxref", index = Index.TOKENIZED, store = Store.YES)
    public String getDbxrefsAsSpaceSeparatedString() {
    	List<String> dbxrefs = new ArrayList<String>();
    	
    	addDbxref(dbxrefs, this.dbXRef);
    	
    	for (FeatureDbXRef fdbx : this.getFeatureDbXRefs()) {
    		addDbxref(dbxrefs, fdbx.getDbXRef());
    	}
    	
    	for (FeaturePub fp : this.getFeaturePubs()) {
    		for (PubDbXRef pd : fp.getPub().getPubDbXRefs()) {
    			addDbxref(dbxrefs, pd.getDbXRef());
    		}
    	}
    	
    	for (FeatureCvTerm fct : this.getFeatureCvTerms()) {
    		for (FeatureCvTermPub fctp : fct.getFeatureCvTermPubs()) {
    			for (PubDbXRef pd : fctp.getPub().getPubDbXRefs())
    			{
    				addDbxref(dbxrefs, pd.getDbXRef());
    			}
    		}
    	}
    	
    	return StringUtils.collectionToDelimitedString(dbxrefs, " ");
    }
    
    @Transient
    private void addDbxref(List<String> dbxrefs, DbXRef dbxref ) {
    	if (dbxref == null) {
    		return;
    	}
    	if (dbxref.getAccession() == null) {
    		return;
    	}
    	if (dbxref.getDb() == null) {
    		return;
    	}
    	if (dbxref.getDb().getName() == null) {
    		return;
    	}
    	
    	dbxrefs.add(dbxref.getDb().getName()+ ":" +dbxref.getAccession());
    	dbxrefs.add(dbxref.getAccession());
    }
    
    
    
    @Override
    public String toString() {
        return String.format("feature '%s' (ID=%d)", getUniqueName(), getFeatureId());
    }

    /* The hashCode() and equals() methods were generated by Eclipse
     * using the fields uniqueName, type, and organism, which constitute
     * a unique key in the database table.
     */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((organism == null) ? 0 : organism.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Feature other = (Feature) obj;
        if (organism == null) {
            if (other.organism != null)
                return false;
        } else if (!organism.equals(other.organism))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (uniqueName == null) {
            if (other.uniqueName != null)
                return false;
        } else if (!uniqueName.equals(other.uniqueName))
            return false;
        return true;
    }
}
