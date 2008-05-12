package org.gmod.schema.organism;



import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.phylogeny.PhylonodeOrganism;
import org.gmod.schema.sequence.Feature;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="organism")
public class Organism implements Serializable {
    
	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="organism")
	private Collection<PhylonodeOrganism> phylonodeOrganisms;
    
    
    public Collection<PhylonodeOrganism> getPhylonodeOrganisms() {
        return this.phylonodeOrganisms;
    }
    
    public void setPhylonodeOrganisms(Collection<PhylonodeOrganism> phylonodeOrganisms) {
        this.phylonodeOrganisms = phylonodeOrganisms;
    }

    // Fields    
    @SequenceGenerator(name="generator", sequenceName="organism_organism_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="organism_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId 
    private int organismId;
     
     @Column(name="abbreviation", unique=false, nullable=true, insertable=true, updatable=true)
     @Field(index = Index.TOKENIZED)
     private String abbreviation;
     
     @Column(name="genus", unique=false, nullable=false, insertable=true, updatable=true)
     @Field(index = Index.TOKENIZED)
     private String genus;
     
     @Column(name="species", unique=false, nullable=false, insertable=true, updatable=true)
     @Field(index = Index.TOKENIZED)
     private String species;
     
     @Column(name="common_name", unique=false, nullable=true, insertable=true, updatable=true)
     @Field(index = Index.TOKENIZED)
     private String commonName;
     
     @Column(name="comment", unique=false, nullable=true, insertable=true, updatable=true)
     private String comment;
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="organism")
     private Collection<OrganismProp> organismProps = new HashSet<OrganismProp>(0);
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="organism")
     private Collection<Feature> features = new HashSet<Feature>(0);
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="organism")
     private Collection<OrganismDbXRef> organismDbXRefs = new HashSet<OrganismDbXRef>(0);
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getOrganismId()
     */
    public int getOrganismId() {
        return this.organismId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setOrganismId(int)
     */
    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }
    
    public String getFullName() {
        return getGenus()+' '+getSpecies();
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getAbbreviation()
     */
    public String getAbbreviation() {
        return this.abbreviation;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setAbbreviation(java.lang.String)
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getGenus()
     */
    public String getGenus() {
        return this.genus;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setGenus(java.lang.String)
     */
    public void setGenus(String genus) {
        this.genus = genus;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getSpecies()
     */
    public String getSpecies() {
        return this.species;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setSpecies(java.lang.String)
     */
    public void setSpecies(String species) {
        this.species = species;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getCommonName()
     */
    public String getCommonName() {
        return this.commonName;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setCommonName(java.lang.String)
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getComment()
     */
    public String getComment() {
        return this.comment;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setComment(java.lang.String)
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getOrganismProps()
     */
    public Collection<OrganismProp> getOrganismProps() {
        return this.organismProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setOrganismProps(java.util.Set)
     */
    private void setOrganismProps(Collection<OrganismProp> organismProps) {
        this.organismProps = organismProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getFeatures()
     */
    private Collection<Feature> getFeatures() {
        return this.features;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setFeatures(java.util.Set)
     */
    private void setFeatures(Collection<Feature> features) {
        this.features = features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#getOrganismDbXRefs()
     */
    private Collection<OrganismDbXRef> getOrganismDbXRefs() {
        return this.organismDbXRefs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.OrganismI#setOrganismDbXRefs(java.util.Set)
     */
    private void setOrganismDbXRefs(Collection<OrganismDbXRef> organismDbXRefs) {
        this.organismDbXRefs = organismDbXRefs;
    }




}


