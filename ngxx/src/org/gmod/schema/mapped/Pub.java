package org.gmod.schema.mapped;


import static javax.persistence.GenerationType.SEQUENCE;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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
import javax.persistence.Transient;

@Entity
@Table(name="pub")
public class Pub implements Serializable {
    private static final Logger logger = Logger.getLogger(Pub.class);

    // Fields
    @SequenceGenerator(name="generator", sequenceName="pub_pub_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int pubId;

     @ManyToOne(cascade={}, fetch=FetchType.LAZY)

     @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm type;

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

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pubBySubjectId")
     private Collection<PubRelationship> pubRelationshipsForSubjectId;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pubByObjectId")
     private Collection<PubRelationship> pubRelationshipsForObjectId;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<PubDbXRef> pubDbXRefs;

     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="pub")
     private Collection<PubProp> pubProps;

     // Constructors

     public Pub() {
         // Deliberately empty default constructor
     }

    /** minimal constructor */
    public Pub(String uniqueName, CvTerm type) {
        this.uniqueName = uniqueName;
        this.type = type;
    }

    // Property accessors

    public int getPubId() {
        return this.pubId;
    }

    public CvTerm getType() {
        return this.type;
    }

    void setType(CvTerm cvTerm) {
        this.type = cvTerm;
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

    public Boolean isObsolete() {
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
        return Collections.unmodifiableCollection(this.pubAuthors);
    }

    public Collection<PubRelationship> getPubRelationshipsForObjectId() {
        return Collections.unmodifiableCollection(this.pubRelationshipsForObjectId);
    }

    @Transient
    private Object pubDbXRefsLock = new Object();

    public Collection<PubDbXRef> getPubDbXRefs() {
        synchronized(pubDbXRefsLock) {
            if (pubDbXRefs == null) {
                pubDbXRefs = new HashSet<PubDbXRef>();
            }
            return this.pubDbXRefs;
        }
    }

    public PubDbXRef addDbXRef(DbXRef dbXRef, boolean current) {
        synchronized(pubDbXRefsLock) {
            if (pubDbXRefs == null) {
                pubDbXRefs = new HashSet<PubDbXRef>();
            }
            for (PubDbXRef pubDbXRef: pubDbXRefs) {
                DbXRef existingDbXRef = pubDbXRef.getDbXRef();
                if (existingDbXRef.equals(dbXRef)) {
                    if (pubDbXRef.isCurrent() && !current) {
                        throw new RuntimeException(String.format(
                            "The pub '%s' (ID=%d) already has a current DbXRef '%s'; trying to add a non-current one",
                            getUniqueName(), getPubId(), existingDbXRef));
                    }
                    if (!pubDbXRef.isCurrent() && current) {
                        throw new RuntimeException(String.format(
                            "The pub '%s' (ID=%d) already has a non-current DbXRef '%s'; trying to add a current one",
                            getUniqueName(), getPubId(), existingDbXRef));
                    }
                    logger.debug(String.format("The pub '%s' (ID=%d) already has DbXRef '%s'",
                        getUniqueName(), getPubId(), dbXRef));
                    return pubDbXRef;
                }
            }
                PubDbXRef pubDbXRef = new PubDbXRef(this, dbXRef, current);
                pubDbXRefs.add(pubDbXRef);
            return pubDbXRef;
        }
    }

    public Collection<DbXRef> getCurrentDbXRefs() {
        synchronized(pubDbXRefsLock) {
            if (pubDbXRefs == null) {
                pubDbXRefs = new HashSet<PubDbXRef>();
            }
            Collection<DbXRef> dbXRefs = new HashSet<DbXRef>();
            for (PubDbXRef pubDbXRef: this.pubDbXRefs) {
                if (pubDbXRef.isCurrent()) {
                    dbXRefs.add(pubDbXRef.getDbXRef());
                }
            }
            return Collections.unmodifiableCollection(dbXRefs);
        }
    }

    public Collection<PubProp> getPubProps() {
        return Collections.unmodifiableCollection(this.pubProps);
    }

    public void addPubProp(PubProp pubProp) {
        this.pubProps.add(pubProp);
        pubProp.setPub(this);
    }

    public Collection<PubRelationship> getPubRelationshipsForSubjectId() {
        return Collections.unmodifiableCollection(this.pubRelationshipsForSubjectId);
    }
}
