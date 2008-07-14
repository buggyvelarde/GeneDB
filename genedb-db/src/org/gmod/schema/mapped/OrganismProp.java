package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.utils.propinterface.PropertyI;

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
@Table(name = "organismprop")
public class OrganismProp implements Serializable, PropertyI {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "organismprop_organismprop_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "organismprop_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int organismPropId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "organism_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Organism organism;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @Column(name = "value", unique = false, nullable = true, insertable = true, updatable = true)
    private String value;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;


    // Constructors

    OrganismProp() {
        // Deliberately empty default constructor
    }

    public OrganismProp(Organism organism, CvTerm type, String value, int rank) {
        this.organism = organism;
        this.cvTerm = type;
        this.value = value;
        this.rank = rank;
    }


    // Property accessors

    public int getOrganismPropId() {
        return this.organismPropId;
    }

    public Organism getOrganism() {
        return this.organism;
    }

    void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    void setType(CvTerm type) {
        this.cvTerm = type;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getRank() {
        return this.rank;
    }
}
