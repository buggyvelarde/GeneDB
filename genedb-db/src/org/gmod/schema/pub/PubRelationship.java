package org.gmod.schema.pub;

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
@Table(name="pub_relationship")
public class PubRelationship implements Serializable {

    // Fields    
     @Id
    
    @Column(name="pub_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int pubRelationshipId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="subject_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pubBySubjectId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="object_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pubByObjectId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;

     // Constructors

    /** default constructor */
    private PubRelationship() {
    }

    /** full constructor */
    private PubRelationship(Pub pubBySubjectId, Pub pubByObjectId, CvTerm cvTerm) {
       this.pubBySubjectId = pubBySubjectId;
       this.pubByObjectId = pubByObjectId;
       this.cvTerm = cvTerm;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#getPubRelationshipId()
     */
    private int getPubRelationshipId() {
        return this.pubRelationshipId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#setPubRelationshipId(int)
     */
    private void setPubRelationshipId(int pubRelationshipId) {
        this.pubRelationshipId = pubRelationshipId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#getPubBySubjectId()
     */
    private Pub getPubBySubjectId() {
        return this.pubBySubjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#setPubBySubjectId(org.gmod.schema.pub.PubI)
     */
    private void setPubBySubjectId(Pub pubBySubjectId) {
        this.pubBySubjectId = pubBySubjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#getPubByObjectId()
     */
    private Pub getPubByObjectId() {
        return this.pubByObjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#setPubByObjectId(org.gmod.schema.pub.PubI)
     */
    private void setPubByObjectId(Pub pubByObjectId) {
        this.pubByObjectId = pubByObjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#getCvTerm()
     */
    private CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubRelationshipI#setCvTerm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }




}


