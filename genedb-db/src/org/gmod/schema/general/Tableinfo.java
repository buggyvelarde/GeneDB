package org.gmod.schema.general;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name="tableinfo")
public class Tableinfo implements Serializable {

    // Fields    
    @Column(name="tableinfo_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int tableinfoId;
     
    @Column(name="name", unique=true, nullable=false, insertable=true, updatable=true, length=30)
     private String name;
     
    @Column(name="primary_key_column", unique=false, nullable=true, insertable=true, updatable=true, length=30)
     private String primaryKeyColumn;
     
    @Column(name="is_view", unique=false, nullable=false, insertable=true, updatable=true)
     private int isView;
     
    @Column(name="view_on_table_id", unique=false, nullable=true, insertable=true, updatable=true)
     private Integer viewOnTableId;
     
    @Column(name="superclass_table_id", unique=false, nullable=true, insertable=true, updatable=true)
     private Integer superclassTableId;
     
    @Column(name="is_updateable", unique=false, nullable=false, insertable=true, updatable=true)
     private int isUpdateable;
     
    @Temporal(TemporalType.DATE)
    @Column(name="modification_date", unique=false, nullable=false, insertable=true, updatable=true, length=13)
     private Date modificationDate;

     // Constructors

    /** default constructor */
    private Tableinfo() {
    }

	/** minimal constructor */
    private Tableinfo(String name, int isView, int isUpdateable, Date modificationDate) {
        this.name = name;
        this.isView = isView;
        this.isUpdateable = isUpdateable;
        this.modificationDate = modificationDate;
    }
    
    /** full constructor */
    private Tableinfo(String name, String primaryKeyColumn, int isView, Integer viewOnTableId, Integer superclassTableId, int isUpdateable, Date modificationDate) {
       this.name = name;
       this.primaryKeyColumn = primaryKeyColumn;
       this.isView = isView;
       this.viewOnTableId = viewOnTableId;
       this.superclassTableId = superclassTableId;
       this.isUpdateable = isUpdateable;
       this.modificationDate = modificationDate;
    }
    
   
    // Property accessors
     /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getTableinfoId()
     */
    @Id
    

    private int getTableinfoId() {
        return this.tableinfoId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setTableinfoId(int)
     */
    private void setTableinfoId(int tableinfoId) {
        this.tableinfoId = tableinfoId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getName()
     */
    private String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setName(java.lang.String)
     */
    private void setName(String name) {
        this.name = name;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getPrimaryKeyColumn()
     */
    private String getPrimaryKeyColumn() {
        return this.primaryKeyColumn;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setPrimaryKeyColumn(java.lang.String)
     */
    private void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getIsView()
     */
    private int getIsView() {
        return this.isView;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setIsView(int)
     */
    private void setIsView(int isView) {
        this.isView = isView;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getViewOnTableId()
     */
    private Integer getViewOnTableId() {
        return this.viewOnTableId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setViewOnTableId(java.lang.Integer)
     */
    private void setViewOnTableId(Integer viewOnTableId) {
        this.viewOnTableId = viewOnTableId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getSuperclassTableId()
     */
    private Integer getSuperclassTableId() {
        return this.superclassTableId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setSuperclassTableId(java.lang.Integer)
     */
    private void setSuperclassTableId(Integer superclassTableId) {
        this.superclassTableId = superclassTableId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getIsUpdateable()
     */
    private int getIsUpdateable() {
        return this.isUpdateable;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setIsUpdateable(int)
     */
    private void setIsUpdateable(int isUpdateable) {
        this.isUpdateable = isUpdateable;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#getModificationDate()
     */
    private Date getModificationDate() {
        return this.modificationDate;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.TableInfoI#setModificationDate(java.util.Date)
     */
    private void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }




}


