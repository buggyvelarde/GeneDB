package org.gmod.schema.pub;



import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="pubauthor")
public class PubAuthor implements Serializable {

    // Fields    
     @Id
    
    @Column(name="pubauthor_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int pubAuthorId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;
     
     @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)     
     private int rank;
     
     @Column(name="editor", unique=false, nullable=true, insertable=true, updatable=true)    
     private Boolean editor;
     
     @Column(name="surname", unique=false, nullable=false, insertable=true, updatable=true, length=100)    
     private String surname;
     
     @Column(name="givennames", unique=false, nullable=true, insertable=true, updatable=true, length=100)     
     private String givenNames;
     
     @Column(name="suffix", unique=false, nullable=true, insertable=true, updatable=true, length=100)     
     private String suffix;

     // Constructors

    /** default constructor */
    public PubAuthor() {
    }

	/** minimal constructor */
    private PubAuthor(Pub pub, int rank, String surname) {
        this.pub = pub;
        this.rank = rank;
        this.surname = surname;
    }
    /** full constructor */
    private PubAuthor(Pub pub, int rank, Boolean editor, String surname, String givenNames, String suffix) {
       this.pub = pub;
       this.rank = rank;
       this.editor = editor;
       this.surname = surname;
       this.givenNames = givenNames;
       this.suffix = suffix;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getPubAuthorId()
     */
    private int getPubAuthorId() {
        return this.pubAuthorId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setPubAuthorId(int)
     */
    private void setPubAuthorId(int pubAuthorId) {
        this.pubAuthorId = pubAuthorId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getRank()
     */
    private int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setRank(int)
     */
    private void setRank(int rank) {
        this.rank = rank;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getEditor()
     */
    private Boolean getEditor() {
        return this.editor;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setEditor(java.lang.Boolean)
     */
    private void setEditor(Boolean editor) {
        this.editor = editor;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getSurname()
     */
    private String getSurname() {
        return this.surname;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setSurname(java.lang.String)
     */
    private void setSurname(String surname) {
        this.surname = surname;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getGivenNames()
     */
    private String getGivenNames() {
        return this.givenNames;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setGivenNames(java.lang.String)
     */
    private void setGivenNames(String givenNames) {
        this.givenNames = givenNames;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#getSuffix()
     */
    private String getSuffix() {
        return this.suffix;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubAuthorI#setSuffix(java.lang.String)
     */
    private void setSuffix(String suffix) {
        this.suffix = suffix;
    }




}


