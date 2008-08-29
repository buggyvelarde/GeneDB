package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;


import org.hibernate.annotations.AccessType;

import java.io.Serializable;
import java.util.Collection;

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
@Table(name = "synonym")
@AccessType("field")
public class Synonym implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "synonym_synonym_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "synonym_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int synonymId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm type;

    @Column(name = "name", unique = false, nullable = false, insertable = true, updatable = true)
    private String name;

    @Column(name = "synonym_sgml", unique = false, nullable = false, insertable = true, updatable = true)
    private String synonymSGML;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "synonym")
    private Collection<FeatureSynonym> featureSynonyms;

    // Constructors

    /** default constructor */
    public Synonym() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public Synonym(CvTerm cvTerm, String name, String synonymSGML) {
        this.type = cvTerm;
        this.name = name;
        this.synonymSGML = synonymSGML;
    }

    // Property accessors

    public int getSynonymId() {
        return this.synonymId;
    }

    public CvTerm getType() {
        return this.type;
    }

    public void setType(CvTerm type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSynonymSGML() {
        return this.synonymSGML;
    }

    public void setSynonymSGML(String synonymSgml) {
        this.synonymSGML = synonymSgml;
    }

    public Collection<FeatureSynonym> getFeatureSynonyms() {
        return this.featureSynonyms;
    }

    @SuppressWarnings("unused") // Called by Hibernate only
    private void setFeatureSynonyms(Collection<FeatureSynonym> featureSynonyms) {
        this.featureSynonyms = featureSynonyms;
    }
}
