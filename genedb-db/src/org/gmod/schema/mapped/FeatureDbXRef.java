package org.gmod.schema.mapped;


import static javax.persistence.GenerationType.SEQUENCE;

import org.apache.log4j.Logger;

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
@Table(name="feature_dbxref")
public class FeatureDbXRef implements Serializable {
    private static final Logger logger = Logger.getLogger(FeatureDbXRef.class);

    // Fields
    @SequenceGenerator(name="generator", sequenceName="feature_dbxref_feature_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
    private int featureDbXRefId;

    @ManyToOne(cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
    private DbXRef dbXRef;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature feature;

    @Column(name="is_current", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean current = true;

    // Constructors

    /** default constructor */
    FeatureDbXRef() {
        logger.trace("Constructing FeatureDbXRef: " + this);
    }

    /** full constructor */
    public FeatureDbXRef(DbXRef dbXRef, Feature feature, boolean current) {
        if (dbXRef == null) {
            throw new NullPointerException("dbxref is null in FeatureDbXRef constructor");
        }
        this.dbXRef = dbXRef;
        this.feature = feature;
        this.current = current;
        logger.trace(String.format("Constructing FeatureDbXRef(%s, %s, %s): %s",
           dbXRef, feature, current, this));
    }

    // Property accessors
    public int getFeatureDbXRefId() {
        return this.featureDbXRefId;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public Feature getFeature() {
        return this.feature;
    }

    void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }


    // hashCode and equals were defined by Eclipse using the fields feature and dbXRef

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbXRef == null) ? 0 : dbXRef.hashCode());
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
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
        FeatureDbXRef other = (FeatureDbXRef) obj;
        if (dbXRef == null) {
            if (other.dbXRef != null)
                return false;
        } else if (!dbXRef.equals(other.dbXRef))
            return false;
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
            return false;
        return true;
    }
}
