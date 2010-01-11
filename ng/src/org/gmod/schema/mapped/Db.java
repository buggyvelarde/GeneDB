package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "db")
public class Db implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "db_db_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "db_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int dbId;

    @Column(name = "name", unique = true, nullable = false, insertable = true, updatable = true)
    private String name;

    @Column(name = "description", unique = false, nullable = true, insertable = true, updatable = true)
    private String description;

    @Column(name = "urlprefix", unique = false, nullable = true, insertable = true, updatable = true)
    private String urlPrefix;

    @Column(name = "url", unique = false, nullable = true, insertable = true, updatable = true)
    private String url;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "db")
    private Collection<DbXRef> dbXRefs;


    // Constructors

    Db() {
        // Deliberately empty default constructor
    }

    public Db(String name, String description, String urlPrefix, String url) {
        this.name = name;
        this.description = description;
        this.urlPrefix = urlPrefix;
        this.url = url;
    }


    // Property accessors
    public int getDbId() {
        return this.dbId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlPrefix() {
        return this.urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Collection<DbXRef> getDbXRefs() {
        return Collections.unmodifiableCollection(this.dbXRefs);
    }

    public void addDbXRef(DbXRef dbXRef) {
        this.dbXRefs.add(dbXRef);
        dbXRef.setDb(this);
    }

    public boolean removeDbXRef(DbXRef dbXRef) {
        if (!this.dbXRefs.remove(dbXRef)) {
            return false;
        }
        dbXRef.setDb(null);
        return true;
    }

}
