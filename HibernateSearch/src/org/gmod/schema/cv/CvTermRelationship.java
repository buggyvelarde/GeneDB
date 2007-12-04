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
@Table(name="cvterm_relationship")
@Indexed
public class CvTermRelationship implements Serializable {

    // Fields    
    @Id
    @Column(name="cvterm_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int cvTermRelationshipId;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="subject_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermBySubjectId;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="object_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermByObjectId;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermByTypeId;

     // Constructors

    /** default constructor */
    public CvTermRelationship() {
    }

    /** full constructor */
    public CvTermRelationship(CvTerm cvTermBySubjectId, CvTerm cvTermByObjectId, CvTerm cvTermByTypeId) {
       this.cvTermBySubjectId = cvTermBySubjectId;
       this.cvTermByObjectId = cvTermByObjectId;
       this.cvTermByTypeId = cvTermByTypeId;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#getCvTermRelationshipId()
     */
    private int getCvTermRelationshipId() {
        return this.cvTermRelationshipId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#setCvTermRelationshipId(int)
     */
    private void setCvTermRelationshipId(int cvTermRelationshipId) {
        this.cvTermRelationshipId = cvTermRelationshipId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#getCvTermBySubjectId()
     */
    private CvTerm getCvTermBySubjectId() {
        return this.cvTermBySubjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#setCvTermBySubjectId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermBySubjectId(CvTerm cvTermBySubjectId) {
        this.cvTermBySubjectId = cvTermBySubjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#getCvTermByObjectId()
     */
    private CvTerm getCvTermByObjectId() {
        return this.cvTermByObjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#setCvTermByObjectId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByObjectId(CvTerm cvTermByObjectId) {
        this.cvTermByObjectId = cvTermByObjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#getCvTermByTypeId()
     */
    private CvTerm getCvTermByTypeId() {
        return this.cvTermByTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermRelationshipI#setCvTermByTypeId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByTypeId(CvTerm cvTermByTypeId) {
        this.cvTermByTypeId = cvTermByTypeId;
    }




}


