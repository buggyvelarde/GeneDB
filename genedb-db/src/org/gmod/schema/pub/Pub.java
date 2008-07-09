package org.gmod.schema.pub;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.phylogeny.PhylonodePub;
import org.gmod.schema.phylogeny.PhylotreePub;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermPub;
import org.gmod.schema.sequence.FeatureLocPub;
import org.gmod.schema.sequence.FeaturePropPub;
import org.gmod.schema.sequence.FeaturePub;
import org.gmod.schema.sequence.FeatureRelationshipPropPub;
import org.gmod.schema.sequence.FeatureRelationshipPub;
import org.gmod.schema.sequence.FeatureSynonym;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
@Table(name="pub")
public class Pub implements Serializable {

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="pub")
    private Collection<PhylotreePub> phylotreePubs;

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="pub")
    private Collection<PhylonodePub> phylonodePubs;

    public Collection<PhylotreePub> getPhylotreePubs() {
        return this.phylotreePubs;
    }

    public void setPhylotreePubs(Collection<PhylotreePub> phylotreePubs) {
        this.phylotreePubs = phylotreePubs;
    }


    public Collection<PhylonodePub> getPhylonodePubs() {
        return this.phylonodePubs;
    }

    public void setPhylonodePubs(Collection<PhylonodePub> phylonodePubs) {
        this.phylonodePubs = phylonodePubs;
    }

    // Fields
    @SequenceGenerator(name="generator", sequenceName="pub_pub_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int pubId;

     @ManyToOne(cascade={}, fetch=FetchType.LAZY)

     @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;

     @Column(name="title", unique=false, nullable=true, insertable=true, updatable=true)
     private String title;

     @Column(name="volumetitle", unique=false, nullable=true, insertable=true, updatable=true)
     private String volumeTitle;

     @Column(name="volume", unique=false, nullable=true, insertable=true, updatable=true)
     private String volume;

     @Column(name="series_name", unique=false, nullable=true, insertable=true, updatable=true)
     private String seriesName;

     @Column(name="issue", unique=false, nullable=true, insertable=true, updatable=true)
     private String issue;

     @Column(name="pyear", unique=false, nullable=true, insertable=true, updatable=true)
     private String pyear;

     @Column(name="pages", unique=false, nullable=true, insertable=true, updatable=true)
     private String pages;

     @Column(name="miniref", unique=false, nullable=true, insertable=true, updatable=true)
     private String miniRef;

     @Column(name="uniquename", unique=true, nullable=false, insertable=true, updatable=true)
     private String uniqueName;

     @Column(name="is_obsolete", unique=false, nullable=true, insertable=true, updatable=true)
     private Boolean obsolete;

     @Column(name="publisher", unique=false, nullable=true, insertable=true, updatable=true)
     private String publisher;

     @Column(name="pubplace", unique=false, nullable=true, insertable=true, updatable=true)
     private String pubPlace;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<PubAuthor> pubAuthors;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pubByObjectId")
     private Collection<PubRelationship> pubRelationshipsForObjectId;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<PubDbXRef> pubDbXRefs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeatureCvTerm> featureCvTerms;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeatureRelationshipPub> featureRelationshipPubs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeaturePub> featurePubs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeaturePropPub> featurePropPubs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeatureSynonym> featureSynonyms;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeatureCvTermPub> featureCvTermPubs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeatureRelationshipPropPub> featureRelationshipPropPubs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<PubProp> pubProps;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pubBySubjectId")
     private Collection<PubRelationship> pubRelationshipsForSubjectId;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<FeatureLocPub> featureLocPubs;

     // Constructors

    /** default constructor */
    public Pub() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public Pub(String uniqueName, CvTerm cvTerm) {
        this.uniqueName = uniqueName;
        this.cvTerm = cvTerm;
    }

    public Pub(String uniqueName) {
        this.uniqueName = uniqueName;
    }


    // Property accessors

    public int getPubId() {
        return this.pubId;
    }

    public void setPubId(int pubId) {
        this.pubId = pubId;
    }

    public CvTerm getCvTerm() {
        return this.cvTerm;
    }

    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVolumeTitle() {
        return this.volumeTitle;
    }

    public void setVolumeTitle(String volumeTitle) {
        this.volumeTitle = volumeTitle;
    }


    public String getVolume() {
        return this.volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }


    public String getSeriesName() {
        return this.seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getIssue() {
        return this.issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getPyear() {
        return this.pyear;
    }

    public void setPyear(String pyear) {
        this.pyear = pyear;
    }


    public String getPages() {
        return this.pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }


    public String getMiniRef() {
        return this.miniRef;
    }

    public void setMiniRef(String miniRef) {
        this.miniRef = miniRef;
    }


    public String getUniqueName() {
        return this.uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public Boolean getObsolete() {
        return this.obsolete;
    }

    public void setObsolete(Boolean obsolete) {
        this.obsolete = obsolete;
    }


    public String getPublisher() {
        return this.publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }


    public String getPubPlace() {
        return this.pubPlace;
    }

    public void setPubPlace(String pubPlace) {
        this.pubPlace = pubPlace;
    }

    public Collection<PubAuthor> getPubAuthors() {
        return this.pubAuthors;
    }

    public void setPubAuthors(Collection<PubAuthor> pubAuthors) {
        this.pubAuthors = pubAuthors;
    }

    public Collection<PubRelationship> getPubRelationshipsForObjectId() {
        return this.pubRelationshipsForObjectId;
    }

    public void setPubRelationshipsForObjectId(Collection<PubRelationship> pubRelationshipsForObjectId) {
        this.pubRelationshipsForObjectId = pubRelationshipsForObjectId;
    }

    private Collection<PubDbXRef> getPubDbXRefs() {
        return this.pubDbXRefs;
    }

    private void setPubDbXRefs(Collection<PubDbXRef> pubDbXRefs) {
        this.pubDbXRefs = pubDbXRefs;
    }

    private Collection<FeatureCvTerm> getFeatureCvTerms() {
        return this.featureCvTerms;
    }

    private void setFeatureCvTerms(Collection<FeatureCvTerm> featureCvTerms) {
        this.featureCvTerms = featureCvTerms;
    }

    private Collection<FeatureRelationshipPub> getFeatureRelationshipPubs() {
        return this.featureRelationshipPubs;
    }

    private void setFeatureRelationshipPubs(Collection<FeatureRelationshipPub> featureRelationshipPubs) {
        this.featureRelationshipPubs = featureRelationshipPubs;
    }

    private Collection<FeaturePub> getFeaturePubs() {
        return this.featurePubs;
    }

    private void setFeaturePubs(Collection<FeaturePub> featurePubs) {
        this.featurePubs = featurePubs;
    }

    private Collection<FeaturePropPub> getFeaturePropPubs() {
        return this.featurePropPubs;
    }
//
    private void setFeaturePropPubs(Collection<FeaturePropPub> featurePropPubs) {
        this.featurePropPubs = featurePropPubs;
    }

    private Collection<FeatureSynonym> getFeatureSynonyms() {
        return this.featureSynonyms;
    }

    private void setFeatureSynonyms(Collection<FeatureSynonym> featureSynonyms) {
        this.featureSynonyms = featureSynonyms;
    }

    private Collection<FeatureCvTermPub> getFeatureCvTermPubs() {
        return this.featureCvTermPubs;
    }

    private void setFeatureCvTermPubs(Collection<FeatureCvTermPub> featureCvTermPubs) {
        this.featureCvTermPubs = featureCvTermPubs;
    }

    private Collection<FeatureRelationshipPropPub> getFeatureRelationshipPropPubs() {
        return this.featureRelationshipPropPubs;
    }

    private void setFeatureRelationshipPropPubs(Set<FeatureRelationshipPropPub> featureRelationshipPropPubs) {
        this.featureRelationshipPropPubs = featureRelationshipPropPubs;
    }

    private Collection<PubProp> getPubProps() {
        return this.pubProps;
    }

    private void setPubProps(Set<PubProp> pubProps) {
        this.pubProps = pubProps;
    }

    private Collection<PubRelationship> getPubRelationshipsForSubjectId() {
        return this.pubRelationshipsForSubjectId;
    }

    private void setPubRelationshipsForSubjectId(Collection<PubRelationship> pubRelationshipsForSubjectId) {
        this.pubRelationshipsForSubjectId = pubRelationshipsForSubjectId;
    }

    private Collection<FeatureLocPub> getFeatureLocPubs() {
        return this.featureLocPubs;
    }

    private void setFeatureLocPubs(Collection<FeatureLocPub> featureLocPubs) {
        this.featureLocPubs = featureLocPubs;
    }
}


