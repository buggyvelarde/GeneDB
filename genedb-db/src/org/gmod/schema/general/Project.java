package org.gmod.schema.general;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="project")
public class Project implements Serializable {

    // Fields    
     @Id
    
    @Column(name="project_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int projectId;
     
     @Column(name="name", unique=true, nullable=false, insertable=true, updatable=true)
     private String name;
     
     @Column(name="description", unique=false, nullable=false, insertable=true, updatable=true)
     private String description;

     // Constructors

    /** default constructor */
    private Project() {
    }

    /** full constructor */
    private Project(String name, String description) {
       this.name = name;
       this.description = description;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.ProjectI#getProjectId()
     */
    private int getProjectId() {
        return this.projectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.ProjectI#setProjectId(int)
     */
    private void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.ProjectI#getName()
     */
    private String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.ProjectI#setName(java.lang.String)
     */
    private void setName(String name) {
        this.name = name;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.ProjectI#getDescription()
     */
    private String getDescription() {
        return this.description;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.ProjectI#setDescription(java.lang.String)
     */
    private void setDescription(String description) {
        this.description = description;
    }




}


