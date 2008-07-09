package org.gmod.schema.cv;



import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import java.io.Serializable;
import java.util.Collection;

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
    @DocumentId
    private int cvId;
    
    @Column(name="name", unique=true, nullable=false, insertable=true, updatable=true)
    @Field(index = Index.TOKENIZED) 
    private String name;
    
    @Column(name="definition", unique=false, nullable=true, insertable=true, updatable=true)
     private String definition;
    
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cv")
     private Collection<CvTermPath> cvTermPaths;
    
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="cv")
     private Collection<CvTerm> cvTerms;
    
   
    // Property accessors

    public int getCvId() {
        return this.cvId;
    }
    
    public void setCvId(int cvId) {
        this.cvId = cvId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getName()
     */
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setName(java.lang.String)
     */
    public void setName(String name) {
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
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getCvTermPaths()
     */
    public Collection<CvTermPath> getCvTermPaths() {
        return this.cvTermPaths;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setCvTermPaths(java.util.Set)
     */
    public void setCvTermPaths(Collection<CvTermPath> cvTermPaths) {
        this.cvTermPaths = cvTermPaths;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#getCvTerms()
     */
    public Collection<CvTerm> getCvTerms() {
        return this.cvTerms;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvI#setCvTerms(java.util.Set)
     */
    public void setCvTerms(Collection<CvTerm> cvTerms) {
        this.cvTerms = cvTerms;
    }

}


