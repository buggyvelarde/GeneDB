package org.gmod.schema.cv;




import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.general.DbXRef;
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
@Table(name="dbxrefprop")
public class DbXRefProp implements Serializable, PropertyI {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="dbxrefprop_dbxrefprop_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(unique=false, nullable=false, insertable=true, updatable=true)
     private int dbXRefPropId;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;

    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

    @Column(name="value", unique=false, nullable=false, insertable=true, updatable=true)
     private String value;

    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
     private int rank;

    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#getDbXRefpropId()
     */
    private int getDbXRefPropId() {
        return this.dbXRefPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#setDbXRefpropId(int)
     */
    private void setDbXRefPropId(int dbXRefPropId) {
        this.dbXRefPropId = dbXRefPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#getCvTerm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#setCvTerm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#getDbXRef()
     */
    private DbXRef getDbXRef() {
        return this.dbXRef;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#setDbXRef(org.gmod.schema.general.DbXRefI)
     */
    private void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#getValue()
     */
    private String getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#getRank()
     */
    private int getRank() {
        return this.rank;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.DbXRefPropI#setRank(int)
     */
    private void setRank(int rank) {
        this.rank = rank;
    }




}


