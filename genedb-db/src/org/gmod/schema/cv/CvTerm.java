package org.gmod.schema.cv;



import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.Synonym;

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

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=true, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="cv_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Cv cv;

    @Column(name="name", unique=false, nullable=false, insertable=true, updatable=true, length=1024)
    @Field(index = Index.UN_TOKENIZED,store=Store.YES)
     private String name;

    @Column(name="definition", unique=false, nullable=true, insertable=true, updatable=true)
     private String definition;

    @Column(name="is_obsolete", unique=false, nullable=false, insertable=true, updatable=true)
     private boolean obsolete;

    @Column(name="is_relationshiptype", unique=false, nullable=false, insertable=true, updatable=true)
     private boolean isRelationshipType;

    @OneToMany(cascade={CascadeType.PERSIST}, fetch=FetchType.LAZY, mappedBy="cvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
     private Collection<Synonym> synonyms;

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
    private int getCvTermId() {
        return this.cvTermId;
    }

    private void setCvTermId(int cvTermId) {
        this.cvTermId = cvTermId;
    }

    private DbXRef getDbXRef() {
        return this.dbXRef;
    }

    private void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public Cv getCv() {
        return this.cv;
    }

    private void setCv(Cv cv) {
        this.cv = cv;
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    private String getDefinition() {
        return this.definition;
    }

    private void setDefinition(String definition) {
        this.definition = definition;
    }

    private boolean isObsolete() {
        return this.obsolete;
    }

    private void setObsolete(boolean isObsolete) {
        this.obsolete = isObsolete;
    }


    private boolean getIsRelationshipType() {
        return this.isRelationshipType;
    }

    private void setIsRelationshipType(boolean isRelationshipType) {
        this.isRelationshipType = isRelationshipType;
    }

    private Collection<Synonym> getSynonyms() {
        return Collections.unmodifiableCollection(this.synonyms);
    }

    private void addSynonym(Synonym synonym) {
        this.synonyms.add(synonym);
        synonym.setCvTerm(this);
    }

    private void removeSynonym(Synonym synonym) {
        this.synonyms.remove(synonym);
        synonym.setCvTerm(null);
    }

    private Collection<CvTermDbXRef> getCvTermDbXRefs() {
        return Collections.unmodifiableCollection(this.cvTermDbXRefs);
    }

    private void addCvTermDbXRef(CvTermDbXRef cvTermDbXRef) {
        this.cvTermDbXRefs.add(cvTermDbXRef);
        cvTermDbXRef.setCvTerm(this);
    }

    private void removeCvTermDbXRef(CvTermDbXRef cvTermDbXRef) {
        this.cvTermDbXRefs.remove(cvTermDbXRef);
        cvTermDbXRef.setCvTerm(null);
    }
}
