package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.utils.propinterface.PropertyI;
import org.gmod.schema.utils.Rankable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * This represents a key/value pair attached to a FeatureCvTerm. The key is itself a CvTerm, with
 * a free-text value
 *
 * Database constraints: feature_cvtermprop_c1 unique (feature_cvterm_id, type_id, rank)
 *
 * @author art
 */
/**
 * @author art
 *
 */
@Entity
@Table(name="feature_cvtermprop")
public class FeatureCvTermProp implements Serializable, PropertyI, Rankable {

/*
create table feature_cvtermprop (
    feature_cvtermprop_id serial not null,
    primary key (feature_cvtermprop_id),
    feature_cvterm_id int not null,
    foreign key (feature_cvterm_id) references feature_cvterm (feature_cvterm_id) on delete cascade,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text null,
    rank int not null default 0,
    constraint feature_cvtermprop_c1 unique (feature_cvterm_id,type_id,rank)
);

COMMENT ON TABLE feature_cvtermprop IS 'Extensible properties for feature to cvterm associations. Examples: GO evidence codes; qualifiers; metadata such as the date on which the entry was curated and the source of the association';

create index feature_cvtermprop_idx1 on feature_cvtermprop (feature_cvterm_id);
create index feature_cvtermprop_idx2 on feature_cvtermprop (type_id);
*/




    // Fields


     /**
     * Database unique primary key
     */
    @SequenceGenerator(name="generator", sequenceName="feature_cvtermprop_feature_cvtermprop_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_cvtermprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermPropId;


    /**
     * The CvTerm that acts as the key in this map of properties
     */
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;

     /**
     * The FeatureCvTerm to which this property is attached
     */
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
         @JoinColumn(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureCvTerm featureCvTerm;

     /**
     * The value of this property
     */
    @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
     private String value;

     /**
     * The rank is used to distinguish multiple
     * values for the same key eg /foo="value1", /foo="value2" in an EMBL file could be stored as two FeatureCvTerm with
     * different ranks. The default is 0;
     */
    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
     private int rank;

     // Constructors

    /** default constructor */
    public FeatureCvTermProp() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public FeatureCvTermProp(CvTerm cvTerm, FeatureCvTerm featureCvTerm, int rank) {
        this.cvTerm = cvTerm;
        this.featureCvTerm = featureCvTerm;
        this.rank = rank;
    }
    /** full constructor */
    public FeatureCvTermProp(CvTerm cvTerm, FeatureCvTerm featureCvTerm, String value, int rank) {
       this.cvTerm = cvTerm;
       this.featureCvTerm = featureCvTerm;
       this.value = value;
       this.rank = rank;
    }


    // Property accessors

    private int getFeatureCvTermPropId() {
        return this.featureCvTermPropId;
    }

    private void setFeatureCvTermPropId(int featureCvTermPropId) {
        this.featureCvTermPropId = featureCvTermPropId;
    }

    /**
     * Accessor for featureCvTerm
     *
     * @see featureCvTerm
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }

    /**
     * Accessor for cvTerm
     *
     * @see cvTerm
     */
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /**
     * Accessor for featureCvTerm
     *
     * @see featureCvTerm
     */
    public FeatureCvTerm getFeatureCvTerm() {
        return this.featureCvTerm;
    }

    /**
     * Accessor for featureCvTerm
     *
     * @see featureCvTerm
     */
    public void setFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        this.featureCvTerm = featureCvTerm;
    }


    /**
     * Accessor for value
     *
     * @see value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Accessor for value
     *
     * @see value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Accessor for rank
     *
     * @see rank
     */
    public int getRank() {
        return this.rank;
    }

    /**
     * Accessor for rank
     *
     * @see rank
     */
    public void setRank(int rank) {
        this.rank = rank;
    }
}


