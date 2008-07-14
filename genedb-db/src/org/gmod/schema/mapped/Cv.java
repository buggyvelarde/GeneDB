package org.gmod.schema.mapped;



import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

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
     private Collection<CvTerm> cvTerms;


    // Constructors

    Cv() {
        // Deliberately empty default constructor
    }

    public Cv(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    // Property accessors

    public int getCvId() {
        return this.cvId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Collection<CvTerm> getCvTerms() {
        return Collections.unmodifiableCollection(this.cvTerms);
    }
    public void addCvTerm(CvTerm cvTerm) {
        this.cvTerms.add(cvTerm);
        cvTerm.setCv(this);
    }

}
