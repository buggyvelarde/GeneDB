package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "project")
public class Project implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "project_project_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "project_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int projectId;

    @Column(name = "name", unique = true, nullable = false, insertable = true, updatable = true)
    private String name;

    @Column(name = "description", unique = false, nullable = false, insertable = true, updatable = true)
    private String description;


    // Constructors

    Project() {
        // Deliberately empty default constructor
    }

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Property accessors

    public int getProjectId() {
        return this.projectId;
    }

    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    void setDescription(String description) {
        this.description = description;
    }

}
