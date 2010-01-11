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
@Table(name = "organism_dbxref")
public class OrganismDbXRef implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "organism_dbxref_organism_dbxref_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "organism_dbxref_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int organismDbXRefId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "organism_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Organism organism;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "dbxref_id", unique = false, nullable = false, insertable = true, updatable = true)
    private DbXRef dbXRef;

    // Constructors

    OrganismDbXRef() {
        // Deliberately empty default constructor
    }

    public OrganismDbXRef(Organism organism, DbXRef dbXRef) {
        this.organism = organism;
        this.dbXRef = dbXRef;
    }

    // Property accessors

    public int getOrganismDbXRefId() {
        return this.organismDbXRefId;
    }

    public Organism getOrganism() {
        return this.organism;
    }

    void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }
}
