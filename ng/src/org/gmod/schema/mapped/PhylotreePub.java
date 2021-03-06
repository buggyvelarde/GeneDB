package org.gmod.schema.mapped;

// Generated Aug 31, 2006 4:02:18 PM by Hibernate Tools 3.2.0.beta7

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * PhylotreePub generated by hbm2java
 */
@Entity
@Table(name = "phylotree_pub", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "phylotree_id", "pub_id" }) })
public class PhylotreePub implements java.io.Serializable {

    // Fields

    @Id
    @Column(name = "phylotree_pub_id", unique = true, nullable = false, insertable = true, updatable = true)
    private int phylotreePubId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "phylotree_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Phylotree phylotree;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    // Constructors

    PhylotreePub() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public PhylotreePub(Phylotree phylotree, Pub pub) {
        this.phylotree = phylotree;
        this.pub = pub;
    }

    // Property accessors
    public int getPhylotreePubId() {
        return this.phylotreePubId;
    }

    public Phylotree getPhylotree() {
        return this.phylotree;
    }

    void setPhylotree(Phylotree phylotree) {
        this.phylotree = phylotree;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

}
