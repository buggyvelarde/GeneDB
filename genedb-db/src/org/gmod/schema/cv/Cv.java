package org.gmod.schema.cv;



import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="cv")
public class Cv implements Serializable {

    // Fields    
    @Id
    @Column(name="cv_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int cvId;
    
    @Column(name="name", unique=true, nullable=false, insertable=true, updatable=true)
     private String name;
    
    @Column(name="definition", unique=false, nullable=true, insertable=true, updatable=true)
     private String definition;
    
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cv")
     private Set<CvTermPath> cvTermPaths = new HashSet<CvTermPath>(0);
    
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cv")
     private Set<CvTerm> cvTerms = new HashSet<CvTerm>(0);

     // Constructors

    /** default constructor */
    public Cv() {
    }

	/** minimal constructor */
    private Cv(String name) {
        this.name = name;
    }
    /** full constructor */
    private Cv(String name, String definition, Set<CvTermPath> cvTermPaths, Set<CvTerm> cvTerms) {
       this.name = name;
       this.definition = definition;
       this.cvTermPaths = cvTermPaths;
       this.cvTerms = cvTerms;
    }
    
   
    // Property accessors

    private int getCvId() {
        return this.cvId;
    }
    
    private void setCvId(int cvId) {
        this.cvId = cvId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getName()
     */
    private String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setName(java.lang.String)
     */
    private void setName(String name) {
        this.name = name;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getDefinition()
     */
    public String getDefinition() {
        return this.definition;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setDefinition(java.lang.String)
     */
    private void setDefinition(String definition) {
        this.definition = definition;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getCvTermPaths()
     */
    private Collection<CvTermPath> getCvTermPaths() {
        return this.cvTermPaths;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setCvTermPaths(java.util.Set)
     */
    private void setCvTermPaths(Set<CvTermPath> cvTermPaths) {
        this.cvTermPaths = cvTermPaths;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getCvTerms()
     */
    private Collection<CvTerm> getCvTerms() {
        return this.cvTerms;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setCvTerms(java.util.Set)
     */
    private void setCvTerms(Set<CvTerm> cvTerms) {
        this.cvTerms = cvTerms;
    }

}


