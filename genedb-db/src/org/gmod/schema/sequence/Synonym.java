package org.gmod.schema.sequence;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;

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
public class Synonym implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "synonym_synonym_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "synonym_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int synonymId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @Column(name = "name", unique = false, nullable = false, insertable = true, updatable = true)
    private String name;

    @Column(name = "synonym_sgml", unique = false, nullable = false, insertable = true, updatable = true)
    private String synonymSgml;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "synonym")
    private Collection<FeatureSynonym> featureSynonyms;

    // Constructors

    /** default constructor */
    public Synonym() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public Synonym(CvTerm cvTerm, String name, String synonymSgml) {
        this.cvTerm = cvTerm;
        this.name = name;
        this.synonymSgml = synonymSgml;
    }

    // Property accessors

    public int getSynonymId() {
        return this.synonymId;
    }

    public void setSynonymId(int synonymId) {
        this.synonymId = synonymId;
    }

    public CvTerm getCvTerm() {
        return this.cvTerm;
    }

    public void setCvTerm(CvTerm cvterm) {
        this.cvTerm = cvterm;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSynonymSgml() {
        return this.synonymSgml;
    }

    public void setSynonymSgml(String synonymSgml) {
        this.synonymSgml = synonymSgml;
    }

    public Collection<FeatureSynonym> getFeatureSynonyms() {
        return this.featureSynonyms;
    }

    public void setFeatureSynonyms(Collection<FeatureSynonym> featureSynonyms) {
        this.featureSynonyms = featureSynonyms;
    }
}
