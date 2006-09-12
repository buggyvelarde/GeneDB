package org.gmod.schema.organism;


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
@Table(name="organismprop")
public class OrganismProp implements Serializable,PropertyI {

    // Fields    
     @Id
    
    @Column(name="organismprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int organismPropId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="organism_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Organism organism;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
     @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
     private String value;
     
     @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
     private int rank;

     // Constructors

    /** default constructor */
    public OrganismProp() {
    }

	/** minimal constructor */
    private OrganismProp(Organism organism, CvTerm cvTerm, int rank) {
        this.organism = organism;
        this.cvTerm = cvTerm;
        this.rank = rank;
    }
    
    /** full constructor */
    private OrganismProp(Organism organism, CvTerm cvTerm, String value, int rank) {
       this.organism = organism;
       this.cvTerm = cvTerm;
       this.value = value;
       this.rank = rank;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#getOrganismPropId()
     */
    private int getOrganismPropId() {
        return this.organismPropId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#setOrganismPropId(int)
     */
    private void setOrganismPropId(int organismPropId) {
        this.organismPropId = organismPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#getOrganism()
     */
    private Organism getOrganism() {
        return this.organism;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#setOrganism(org.gmod.schema.organism.OrganismI)
     */
    private void setOrganism(Organism organism) {
        this.organism = organism;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#getCvTerm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#setCvTerm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#getValue()
     */
    private String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#getRank()
     */
    private int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismPropI#setRank(int)
     */
    private void setRank(int rank) {
        this.rank = rank;
    }




}


