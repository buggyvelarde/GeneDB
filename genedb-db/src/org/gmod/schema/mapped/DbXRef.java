package org.gmod.schema.mapped;


import static javax.persistence.GenerationType.SEQUENCE;


import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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
import javax.persistence.Transient;

@Entity
@Table(name="dbxref")
public class DbXRef implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="dbxref_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int dbXRefId;

    @Column(name="version", unique=false, nullable=false, insertable=true, updatable=true)
     private String version;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="db_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Db db;

    @Column(name="accession", unique=false, nullable=false, insertable=true, updatable=true)
     private String accession;

    @Column(name="description", unique=false, nullable=true, insertable=true, updatable=true)
     private String description;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="dbXRef")
     private Collection<DbXRefProp> dbXRefProps = new HashSet<DbXRefProp>(0);

     // Constructors

    DbXRef() {
        // Deliberately empty default constructor
    }

    public DbXRef(Db db, String accession) {
        this(db, accession, null);
    }

    public DbXRef(Db db, String accession, String description) {
        this(db, accession, description, "1");
    }

    public DbXRef(Db db, String accession, String description, String version) {
        this.version = version;
        this.db = db;
        this.accession = accession;
        this.description = description;
    }

    // Property accessors
    public int getDbXRefId() {
        return this.dbXRefId;
    }

    public String getVersion() {
        return this.version;
    }

    public Db getDb() {
        return this.db;
    }
    void setDb(Db db) {
        this.db = db;
    }

    /**
     * Get the URL of this reference.
     * @return the URL, or null if the associated database is not accessible through the web
     */
    @Transient
    public String getUrl() {
        String urlPrefix = getDb().getUrlPrefix();
        if (urlPrefix == null) {
            return null;
        }
        return urlPrefix + getAccession();
    }

    public String getAccession() {
        return this.accession;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<DbXRefProp> getDbXRefProps() {
        return Collections.unmodifiableCollection(this.dbXRefProps);
    }

    public void addDbXRefProp(DbXRefProp dbXRefProp) {
        this.dbXRefProps.add(dbXRefProp);
        dbXRefProp.setDbXRef(this);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", db == null ? "null" : db.getName(), accession);
    }
}


