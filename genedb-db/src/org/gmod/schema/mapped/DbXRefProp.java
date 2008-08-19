package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.utils.propinterface.PropertyI;

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

@Entity
@Table(name="dbxrefprop")
public class DbXRefProp implements Serializable, PropertyI {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="dbxrefprop_dbxrefprop_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="dbxrefprop_id", unique=false, nullable=false, insertable=true, updatable=true)
    private int dbXRefPropId;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;

    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

    @Column(name="value", unique=false, nullable=false, insertable=true, updatable=true)
     private String value;

    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
     private int rank;


    // Constructors

    DbXRefProp() {
        // Deliberately empty default constructor
    }

    public DbXRefProp(CvTerm cvTerm, DbXRef dbXRef, String value, int rank) {
        this.cvTerm = cvTerm;
        this.dbXRef = dbXRef;
        this.value = value;
        this.rank = rank;
    }

    // Property accessors
    public int getDbXRefPropId() {
        return this.dbXRefPropId;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }
    void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public String getValue() {
        return this.value;
    }

    public int getRank() {
        return this.rank;
    }
}


