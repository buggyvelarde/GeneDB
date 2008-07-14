package org.gmod.schema.mapped;




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
@Table(name="analysisprop")
public class AnalysisProp implements Serializable, PropertyI {

    // Fields

    @SequenceGenerator(name="generator",sequenceName="analysisprop_analysisprop_id_seq" )
    @Id @GeneratedValue(generator="generator")
    @Column(name="analysisprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int analysisPropId;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="analysis_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Analysis analysis;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;

    @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
     private String value;

    // Constructors
    AnalysisProp() {
        // Deliberately empty default constructor
    }

    // Property accessors

    public AnalysisProp(Analysis analysis, CvTerm cvTerm, String value) {
        this.analysis = analysis;
        this.cvTerm = cvTerm;
        this.value = value;
    }

    public int getAnalysisPropId() {
        return this.analysisPropId;
    }

    public Analysis getAnalysis() {
        return this.analysis;
    }

    void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


