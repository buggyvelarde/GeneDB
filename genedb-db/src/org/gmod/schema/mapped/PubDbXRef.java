package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.CascadeType;
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
@Table(name = "pub_dbxref")
public class PubDbXRef implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "pub_dbxref_pub_dbxref_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "pub_dbxref_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int pubDbXRefId;

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "dbxref_id", unique = false, nullable = false, insertable = true, updatable = true)
    private DbXRef dbXRef;

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @Column(name = "is_current", unique = false, nullable = false, insertable = true, updatable = true)
    private boolean current;

    // Constructors

    PubDbXRef() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public PubDbXRef(Pub pub, DbXRef dbXRef, boolean current) {
        this.dbXRef = dbXRef;
        this.pub = pub;
        this.current = current;
    }

    // Property accessors

    public int getPubDbXRefId() {
        return this.pubDbXRefId;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

}
