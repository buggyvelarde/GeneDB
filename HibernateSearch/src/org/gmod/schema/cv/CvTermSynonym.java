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
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

@Entity
@Table(name="cvtermsynonym")
@Indexed
public class CvTermSynonym implements Serializable {

    // Fields    
     @Id
    @Column(name="cvtermsynonym_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int cvTermSynonymId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         @JoinColumn(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermByCvTermId;
     
     @ManyToOne(cascade={},fetch=FetchType.LAZY)
         @JoinColumn(name="type_id", unique=false, nullable=true, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTermByTypeId;
     
     @Column(name="synonym", unique=false, nullable=false, insertable=true, updatable=true, length=1024)
     @Field(index = Index.TOKENIZED)
     private String synonym;

     // Constructors

    /** default constructor */
    public CvTermSynonym() {
    }

	/** minimal constructor */
    private CvTermSynonym(CvTerm cvTermByCvTermId, String synonym) {
        this.cvTermByCvTermId = cvTermByCvTermId;
        this.synonym = synonym;
    }
    
    /** full constructor */
    private CvTermSynonym(CvTerm cvTermByCvTermId, CvTerm cvTermByTypeId, String synonym) {
       this.cvTermByCvTermId = cvTermByCvTermId;
       this.cvTermByTypeId = cvTermByTypeId;
       this.synonym = synonym;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#getCvTermSynonymId()
     */
    private int getCvTermSynonymId() {
        return this.cvTermSynonymId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#setCvTermSynonymId(int)
     */
    private void setCvTermSynonymId(int cvTermSynonymId) {
        this.cvTermSynonymId = cvTermSynonymId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#getCvTermByCvTermId()
     */
    private CvTerm getCvTermByCvTermId() {
        return this.cvTermByCvTermId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#setCvTermByCvTermId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByCvTermId(CvTerm cvTermByCvTermId) {
        this.cvTermByCvTermId = cvTermByCvTermId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#getCvTermByTypeId()
     */
    private CvTerm getCvTermByTypeId() {
        return this.cvTermByTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#setCvTermByTypeId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByTypeId(CvTerm cvTermByTypeId) {
        this.cvTermByTypeId = cvTermByTypeId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#getSynonym()
     */
    private String getSynonym() {
        return this.synonym;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermSynonymI#setSynonym(java.lang.String)
     */
    private void setSynonym(String synonym) {
        this.synonym = synonym;
    }




}


