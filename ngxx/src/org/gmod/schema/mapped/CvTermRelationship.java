package org.gmod.schema.mapped;



import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="cvterm_relationship")
public class CvTermRelationship implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="cvterm_relationship_cvterm_relationship_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="cvterm_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int cvTermRelationshipId;

    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="subject_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTermBySubjectId;

    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="object_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTermByObjectId;

    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTermByTypeId;

     // Constructors

    /** default constructor */
    public CvTermRelationship() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public CvTermRelationship(CvTerm subject, CvTerm object, CvTerm type) {
       this.cvTermBySubjectId = subject;
       this.cvTermByObjectId = object;
       this.cvTermByTypeId = type;
    }


    // Property accessors
    public int getCvTermRelationshipId() {
        return this.cvTermRelationshipId;
    }

    public CvTerm getSubject() {
        return this.cvTermBySubjectId;
    }

    public CvTerm getObject() {
        return this.cvTermByObjectId;
    }

    public CvTerm getType() {
        return this.cvTermByTypeId;
    }
}


