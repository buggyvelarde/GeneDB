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
@Table(name = "cvtermprop")
public class CvTermProp implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "cvtermprop_cvtermprop_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "cvtermprop_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int cvTermPropId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cvterm_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTermByCvTermId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTermByTypeId;

    @Column(name = "value", unique = false, nullable = false, insertable = true, updatable = true)
    private String value;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;


    // Constructors
    CvTermProp() {
        // Deliberately empty default constructor
    }

    public CvTermProp(CvTerm cvTerm, CvTerm type, int rank, String value) {
        this.cvTermByCvTermId = cvTerm;
        this.cvTermByTypeId = type;
        this.rank = rank;
        this.value = value;
    }

    // Property accessors

    public int getCvTermPropId() {
        return this.cvTermPropId;
    }

    public CvTerm getCvTerm() {
        return this.cvTermByCvTermId;
    }

    public CvTerm getType() {
        return this.cvTermByTypeId;
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

    public void setRank(int rank) {
        this.rank = rank;
    }
}
