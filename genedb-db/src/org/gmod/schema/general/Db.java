package org.gmod.schema.general;


import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="db")
public class Db implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="db_db_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="db_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int dbId;

    @Column(name="name", unique=true, nullable=false, insertable=true, updatable=true)
     private String name;

    @Column(name="description", unique=false, nullable=true, insertable=true, updatable=true)
     private String description;

    @Column(name="urlprefix", unique=false, nullable=true, insertable=true, updatable=true)
     private String urlPrefix;

    @Column(name="url", unique=false, nullable=true, insertable=true, updatable=true)
     private String url;

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="db")
     private Collection<DbXRef> dbXRefs;


    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#getDbId()
     */
    public int getDbId() {
        return this.dbId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#setDbId(int)
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#getName()
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#getDescription()
     */
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#getUrlPrefix()
     */
    public String getUrlPrefix() {
        return this.urlPrefix;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#setUrlPrefix(java.lang.String)
     */
    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#getUrl()
     */
    public String getUrl() {
        return this.url;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#setUrl(java.lang.String)
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#getDbXRefs()
     */
    public Collection<DbXRef> getDbXRefs() {
        return this.dbXRefs;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbI#setDbXRefs(java.util.Set)
     */
    public void setDbXRefs(Collection<DbXRef> dbXRefs) {
        this.dbXRefs = dbXRefs;
    }




}


