package org.gmod.schema.mapped;




import org.hibernate.annotations.Cascade;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="cvterm")
public class CvTerm implements Serializable {

    // Fields
    @SequenceGenerator(name="generator",sequenceName="cvterm_cvterm_id_seq" )
    @Id @GeneratedValue(generator="generator")
    @Column(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
    private int cvTermId;

    @ManyToOne(cascade={}, fetch=FetchType.EAGER)
    @JoinColumn(name="dbxref_id", unique=true, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

    @ManyToOne(cascade={}, fetch=FetchType.EAGER)
    @JoinColumn(name="cv_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Cv cv;

    @Column(name="name", unique=false, nullable=false, insertable=true, updatable=true, length=1024)
    @Field(index = Index.UN_TOKENIZED,store=Store.YES)
    private String name;

    @Column(name="definition", unique=false, nullable=true, insertable=true, updatable=true)
    private String definition;

    @Column(name="is_obsolete", unique=false, nullable=false, insertable=true, updatable=true)
    private int obsolete;

    @Column(name="is_relationshiptype", unique=false, nullable=false, insertable=true, updatable=true)
    private int isRelationshipType;

    @OneToMany(cascade={CascadeType.PERSIST}, fetch=FetchType.LAZY, mappedBy="cvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Collection<CvTermDbXRef> cvTermDbXRefs;

     // Constructors

    /** default constructor */
    public CvTerm() {
        // Deliberately empty default constructor
    }

    /** useful constructor! */
    public CvTerm(final Cv cv, final DbXRef dbXRef, final String name, final String definition) {
       this.dbXRef = dbXRef;
       this.cv = cv;
       this.name = name;
       this.definition = definition;
    }

    // Property accessors
    public int getCvTermId() {
        return this.cvTermId;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    public Cv getCv() {
        return this.cv;
    }
    void setCv(Cv cv) {
        this.cv = cv;
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

    public boolean isObsolete() {
        return this.obsolete == 1;
    }

    public void setObsolete(boolean isObsolete) {
        this.obsolete = isObsolete ? 1 : 0;
    }

    public boolean isRelationshipType() {
        return this.isRelationshipType == 1;
    }

    public void setRelationshipType(boolean isRelationshipType) {
        this.isRelationshipType = isRelationshipType ? 1 : 0;
    }

    public Collection<CvTermDbXRef> getCvTermDbXRefs() {
        return Collections.unmodifiableCollection(this.cvTermDbXRefs);
    }

    public void addCvTermDbXRef(CvTermDbXRef cvTermDbXRef) {
        this.cvTermDbXRefs.add(cvTermDbXRef);
        cvTermDbXRef.setCvTerm(this);
    }

    public void removeCvTermDbXRef(CvTermDbXRef cvTermDbXRef) {
        this.cvTermDbXRefs.remove(cvTermDbXRef);
        cvTermDbXRef.setCvTerm(null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cv == null) ? 0 : cv.hashCode());
        result = prime * result
                + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((obsolete == 1) ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        CvTerm other = (CvTerm) obj;
        if (cv == null) {
            if (other.getCv() != null) {
                return false;
            }
        } else if (!cv.equals(other.getCv())) {
            return false;
        }
        if (definition == null) {
            if (other.getDefinition() != null) {
                return false;
            }
        } else if (!definition.equals(other.getDefinition())) {
            return false;
        }
        if (name == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!name.equals(other.getName())) {
            return false;
        }
        if (this.isObsolete() != other.isObsolete()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getCv().getName(), getName());
    }
}
