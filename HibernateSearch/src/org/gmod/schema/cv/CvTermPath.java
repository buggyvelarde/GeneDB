package org.gmod.schema.cv;



import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Entity
@Table(name="cvtermpath")
//@Indexed
public class CvTermPath implements Serializable {

    // Fields    
    @Id
    @Column(name="cvtermpath_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int cvTermPathId;
    
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="subject_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermBySubjectId;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        
        @JoinColumn(name="object_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermByObjectId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="type_id", unique=false, nullable=true, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermByTypeId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="cv_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private Cv cv;
     
    @Column(name="pathdistance", unique=false, nullable=true, insertable=true, updatable=true)
     private Integer pathDistance;

     // Constructors

    /** default constructor */
    public CvTermPath() {
    }

	/** minimal constructor */
    private CvTermPath(CvTerm cvTermBySubjectId, CvTerm cvTermByObjectId, Cv cv) {
        this.cvTermBySubjectId = cvTermBySubjectId;
        this.cvTermByObjectId = cvTermByObjectId;
        this.cv = cv;
    }
    /** full constructor */
    private CvTermPath(CvTerm cvTermBySubjectId, CvTerm cvTermByObjectId, CvTerm cvTermByTypeId, Cv cv, Integer pathDistance) {
       this.cvTermBySubjectId = cvTermBySubjectId;
       this.cvTermByObjectId = cvTermByObjectId;
       this.cvTermByTypeId = cvTermByTypeId;
       this.cv = cv;
       this.pathDistance = pathDistance;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#getCvTermPathId()
     */
    private int getCvTermPathId() {
        return this.cvTermPathId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#setCvTermPathId(int)
     */
    private void setCvTermPathId(int cvTermPathId) {
        this.cvTermPathId = cvTermPathId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#getCvTermBySubjectId()
     */
    private CvTerm getCvTermBySubjectId() {
        return this.cvTermBySubjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#setCvTermBySubjectId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermBySubjectId(CvTerm cvTermBySubjectId) {
        this.cvTermBySubjectId = cvTermBySubjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#getCvTermByObjectId()
     */
    private CvTerm getCvTermByObjectId() {
        return this.cvTermByObjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#setCvTermByObjectId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByObjectId(CvTerm cvTermByObjectId) {
        this.cvTermByObjectId = cvTermByObjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#getCvTermByTypeId()
     */
    private CvTerm getCvTermByTypeId() {
        return this.cvTermByTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#setCvTermByTypeId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByTypeId(CvTerm cvTermByTypeId) {
        this.cvTermByTypeId = cvTermByTypeId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#getCv()
     */
    private Cv getCv() {
        return this.cv;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#setCv(org.gmod.schema.cv.CvI)
     */
    private void setCv(Cv cv) {
        this.cv = cv;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#getPathDistance()
     */
    private Integer getPathDistance() {
        return this.pathDistance;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPathI#setPathDistance(java.lang.Integer)
     */
    private void setPathDistance(Integer pathDistance) {
        this.pathDistance = pathDistance;
    }




}


