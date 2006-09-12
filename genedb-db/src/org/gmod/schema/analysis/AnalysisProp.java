package org.gmod.schema.analysis;



import org.genedb.db.propinterface.PropertyI;
import org.gmod.schema.cv.CvTerm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="analysisprop")
public class AnalysisProp implements Serializable,PropertyI {

    // Fields    

    @Id
    @Column(name="analysisprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int analysisPropId;
    
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="analysis_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Analysis analysis;
    
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
    
    @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
     private String value;

     // Constructors

    /** default constructor */
    public AnalysisProp() {
    }

	/** minimal constructor */
    private AnalysisProp(Analysis analysis, CvTerm cvTerm) {
        this.analysis = analysis;
        this.cvTerm = cvTerm;
    }
    /** full constructor */
    private AnalysisProp(Analysis analysis, CvTerm cvTerm, String value) {
       this.analysis = analysis;
       this.cvTerm = cvTerm;
       this.value = value;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#getAnalysispropId()
     */
    private int getAnalysisPropId() {
        return this.analysisPropId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#setAnalysispropId(int)
     */
    private void setAnalysisPropId(int analysisPropId) {
        this.analysisPropId = analysisPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#getAnalysis()
     */
    private Analysis getAnalysis() {
        return this.analysis;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#setAnalysis(org.genedb.db.jpa.Analysis)
     */
    private void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#getCvterm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#setCvterm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#getValue()
     */
    private String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisPropI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }




}


