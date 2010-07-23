package org.gmod.schema.mapped;


import org.gmod.schema.utils.Rankable;
import org.gmod.schema.utils.propinterface.PropertyI;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="featureprop")
public class FeatureProp implements Serializable, PropertyI, Rankable {

    // Fields
    @GenericGenerator(name="generator", strategy="seqhilo", parameters = {  @Parameter(name="max_lo", value="100"), @Parameter(name="sequence", value="featureprop_featureprop_id_seq") } )
    @Id
    @GeneratedValue(generator="generator")
    @Column(name="featureprop_id", unique=false, nullable=false, insertable=true, updatable=true)
    private int featurePropId;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
    private CvTerm cvTerm;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature feature;

    @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
    private String value;

    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
    private int rank;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureProp")
    private Collection<FeaturePropPub> featurePropPubs;

     // Constructors
    /** default constructor */
    FeatureProp() {
        // Deliberately empty default constructor
    }

    /** useful constructor ! */
    public FeatureProp(Feature feature, CvTerm type, String value, int rank) {
       this.cvTerm = type;
       this.feature = feature;
       this.value = value;
       this.rank = rank;
    }


    // Property accessors

    public int getFeaturePropId() {
        return this.featurePropId;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getRank() {
        return this.rank;
    }

    public Collection<FeaturePropPub> getFeaturePropPubs() {
        return Collections.unmodifiableCollection(this.featurePropPubs);
    }

    public void setFeaturePropPubs(Collection<FeaturePropPub> featurePropPubs) {
        this.featurePropPubs = featurePropPubs;
    }



}


