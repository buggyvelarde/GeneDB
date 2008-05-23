package org.gmod.schema.organism;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.gmod.schema.phylogeny.PhylonodeOrganism;
import org.gmod.schema.sequence.Feature;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Entity
@Table(name = "organism")
@Indexed
public class Organism implements Serializable {

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "organism")
    private Collection<PhylonodeOrganism> phylonodeOrganisms;

    public Collection<PhylonodeOrganism> getPhylonodeOrganisms() {
        return this.phylonodeOrganisms;
    }

    public void setPhylonodeOrganisms(Collection<PhylonodeOrganism> phylonodeOrganisms) {
        this.phylonodeOrganisms = phylonodeOrganisms;
    }

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "organism_organism_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "organism_id", unique = false, nullable = false, insertable = true, updatable = true)
    @DocumentId
    private int organismId;

    @Column(name = "abbreviation", unique = false, nullable = true, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED)
    private String abbreviation;

    @Column(name = "genus", unique = false, nullable = false, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED)
    private String genus;

    @Column(name = "species", unique = false, nullable = false, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED)
    private String species;

    @Column(name = "common_name", unique = false, nullable = true, insertable = true, updatable = true)
    @Field(index = Index.UN_TOKENIZED, store=Store.YES)
    private String commonName;

    @Column(name = "comment", unique = false, nullable = true, insertable = true, updatable = true)
    private String comment;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "organism")
    private Collection<OrganismProp> organismProps = new HashSet<OrganismProp>(0);

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "organism")
    private Collection<Feature> features = new HashSet<Feature>(0);

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "organism")
    private Collection<OrganismDbXRef> organismDbXRefs = new HashSet<OrganismDbXRef>(0);

    // Property accessors

    public int getOrganismId() {
        return this.organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    public String getFullName() {
        return getGenus() + ' ' + getSpecies();
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getGenus() {
        return this.genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return this.species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getCommonName() {
        return this.commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Collection<OrganismProp> getOrganismProps() {
        return this.organismProps;
    }

    @SuppressWarnings("unused")
    private void setOrganismProps(Collection<OrganismProp> organismProps) {
        this.organismProps = organismProps;
    }

    @SuppressWarnings("unused")
    private Collection<Feature> getFeatures() {
        return this.features;
    }

    @SuppressWarnings("unused")
    private void setFeatures(Collection<Feature> features) {
        this.features = features;
    }

    @SuppressWarnings("unused")
    private Collection<OrganismDbXRef> getOrganismDbXRefs() {
        return this.organismDbXRefs;
    }

    @SuppressWarnings("unused")
    private void setOrganismDbXRefs(Collection<OrganismDbXRef> organismDbXRefs) {
        this.organismDbXRefs = organismDbXRefs;
    }
}
