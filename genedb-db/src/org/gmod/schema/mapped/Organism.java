package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;


import org.apache.log4j.Logger;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
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
import javax.persistence.Transient;

@Entity
@Table(name = "organism")
@Indexed
public class Organism implements Serializable {
    private static final Logger logger = Logger.getLogger(Organism.class);

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "organism")
    private Collection<PhylonodeOrganism> phylonodeOrganisms;

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


    // Constructors
    Organism() {
        // Deliberately empty default constructor
    }

    public Organism(String genus, String species, String commonName, String abbreviation,
            String comment) {
        this.genus = genus;
        this.species = species;
        this.commonName = commonName;
        this.abbreviation = abbreviation;
        this.comment = comment;
    }

    // Property accessors

    public int getOrganismId() {
        return this.organismId;
    }

    public String getFullName() {
        return getGenus() + ' ' + getSpecies();
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    void setAbbreviation(final String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getGenus() {
        return this.genus;
    }

    void setGenus(final String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return this.species;
    }

    void setSpecies(final String species) {
        this.species = species;
    }

    public String getCommonName() {
        return this.commonName;
    }

    void setCommonName(final String commonName) {
        this.commonName = commonName;
    }

    public String getComment() {
        return this.comment;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    public Collection<OrganismProp> getOrganismProps() {
        return Collections.unmodifiableCollection(this.organismProps);
    }

    public Collection<Feature> getFeatures() {
        return Collections.unmodifiableCollection(this.features);
    }

    /**
     * Get all the OrganismDbXRef objects associated with this organism.
     * Usually it is easier to use {@link #getDbXRefs()}.
     *
     * @return an unmodifiable collection of <code>OrganismDbXRef</code> objects.
     */
    public Collection<OrganismDbXRef> getOrganismDbXRefs() {
        return Collections.unmodifiableCollection(this.organismDbXRefs);
    }

    /**
     * Get all the external database references associated with this
     * organism.
     *
     * @return an unmodifiable collection of <code>DbXRef</code> objects
     */
    @Transient
    public Collection<DbXRef> getDbXRefs() {
        Collection<DbXRef> dbXRefs = new HashSet<DbXRef>();
        for (OrganismDbXRef organismDbXRef: this.organismDbXRefs) {
            dbXRefs.add(organismDbXRef.getDbXRef());
        }
        return Collections.unmodifiableCollection(dbXRefs);
    }

    /**
     * Get the value of a property of this organism.
     *
     * @param cvName the name of the controlled vocabulary from which the property name is drawn
     * @param cvTermName the property name
     * @return the value of the property, or null if there is no such property.
     */
    @Transient
    private String getPropertyValue(String cvName, String cvTermName) {
        for (OrganismProp organismProp: getOrganismProps()) {
            CvTerm propType = organismProp.getType();
            if (propType.getName().equals(cvTermName) && propType.getCv().getName().equals(cvName)) {
                logger.debug(String.format("Organism property '%s/%s' is '%s'", cvName, cvTermName, organismProp.getValue()));
                return organismProp.getValue();
            }
        }
        logger.error(String.format("Organism property '%s/%s' not found for organism ID=%d", cvName, cvTermName, getOrganismId()));
        return null;
    }

    @Transient
    public String getHtmlShortName() {
        return getPropertyValue("genedb_misc", "htmlShortName");
    }

    public Collection<PhylonodeOrganism> getPhylonodeOrganisms() {
        return Collections.unmodifiableCollection(this.phylonodeOrganisms);
    }

}
