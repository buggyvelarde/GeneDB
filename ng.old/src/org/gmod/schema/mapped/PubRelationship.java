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
@Table(name = "pub_relationship")
public class PubRelationship implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "pub_relationship_pub_relationship_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "pub_relationship_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int pubRelationshipId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pubBySubjectId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pubByObjectId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    // Constructors
    PubRelationship() {
        // Deliberately empty default constructor
    }



    // Property accessors

    public PubRelationship(Pub subject, Pub object, CvTerm type) {
        this.pubBySubjectId = subject;
        this.pubByObjectId = object;
        this.cvTerm = type;
    }



    public int getPubRelationshipId() {
        return this.pubRelationshipId;
    }

    public Pub getSubjectPub() {
        return this.pubBySubjectId;
    }

    void setSubjectPub(Pub subject) {
        this.pubBySubjectId = subject;
    }

    public Pub getObjectPub() {
        return this.pubByObjectId;
    }

    void setObjectPub(Pub object) {
        this.pubByObjectId = object;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    void setType(CvTerm type) {
        this.cvTerm = type;
    }

}
