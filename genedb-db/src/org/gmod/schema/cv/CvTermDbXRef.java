package org.gmod.schema.cv;

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

import org.gmod.schema.general.DbXRef;


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
    
   
    // Property accessors

    private int getCvTermDbXRefId() {
        return this.cvTermDbXRefId;
    }
    
    private void setCvTermDbXRefId(int cvTermDbXRefId) {
        this.cvTermDbXRefId = cvTermDbXRefId;
    }

    private CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    private DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    private void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }
    
    private int getIsForDefinition() {
        return this.isForDefinition;
    }
    
    private void setIsForDefinition(int isForDefinition) {
        this.isForDefinition = isForDefinition;
    }
}


