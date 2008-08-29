package org.gmod.schema.mapped;


import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="feature_synonym")
public class FeatureSynonym implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="feature_synonym_feature_synonym_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")

    @Column(name="feature_synonym_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureSynonymId;

    @ManyToOne(cascade={CascadeType.PERSIST}, fetch=FetchType.LAZY)
    @JoinColumn(name="synonym_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Synonym synonym;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature feature;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Pub pub;

    @Column(name="is_current", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean current;

    @Column(name="is_internal", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean internal;

    // Constructors

    /** default constructor */
    FeatureSynonym() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public FeatureSynonym(Synonym synonym, Feature feature, Pub pub, boolean current, boolean internal) {
       this.synonym = synonym;
       this.feature = feature;
       this.pub = pub;
       this.current = current;
       this.internal = internal;
    }


    // Property accessors

    public int getFeatureSynonymId() {
        return this.featureSynonymId;
    }

    public Synonym getSynonym() {
        return this.synonym;
    }

    public Feature getFeature() {
        return this.feature;
    }

    void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isInternal() {
        return this.internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}


