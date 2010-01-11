package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;


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
@Table(name="cvterm_dbxref")
public class CvTermDbXRef implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="cvterm_dbxref_cvterm_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="cvterm_dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
    private int cvTermDbXRefId;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
    private CvTerm cvTerm;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

    @Column(name="is_for_definition", unique=false, nullable=false, insertable=true, updatable=true)
    private int isForDefinition;


    // Constructors
    CvTermDbXRef() {
        // Deliberately empty default constructor
    }

    public CvTermDbXRef(CvTerm cvTerm, DbXRef dbXRef, int isForDefinition) {
        this.cvTerm = cvTerm;
        this.dbXRef = dbXRef;
        this.isForDefinition = isForDefinition;
    }


    // Property accessors

    public int getCvTermDbXRefId() {
        return this.cvTermDbXRefId;
    }

    public CvTerm getCvTerm() {
        return this.cvTerm;
    }

    void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    void setDbXRef (DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public int isForDefinition() {
        return this.isForDefinition;
    }

    public void setForDefinition(int isForDefinition) {
        this.isForDefinition = isForDefinition;
    }
}


