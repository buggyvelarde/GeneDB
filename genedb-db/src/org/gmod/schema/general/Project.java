package org.gmod.schema.general;


import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="project")
public class Project implements Serializable {

    // Fields    
	@SequenceGenerator(name="generator", sequenceName="project_project_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="project_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int projectId;
     
     @Column(name="name", unique=true, nullable=false, insertable=true, updatable=true)
     private String name;
     
     @Column(name="description", unique=false, nullable=false, insertable=true, updatable=true)
     private String description;
    
   
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


