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
@Table(name = "pubprop")
public class PubProp implements Serializable, PropertyI {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "pubprop_pubprop_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "pubprop_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int pubPropId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @Column(name = "value", unique = false, nullable = false, insertable = true, updatable = true)
    private String value;

    @Column(name = "rank", unique = false, nullable = true, insertable = true, updatable = true)
    private Integer rank;

    // Constructors

    PubProp() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public PubProp(Pub pub, CvTerm type, String value) {
        this.pub = pub;
        this.cvTerm = type;
        this.value = value;
    }

    /** full constructor */
    public PubProp(CvTerm type, Pub pub, String value, Integer rank) {
        this(pub, type, value);
        this.rank = rank;
    }

    // Property accessors

    public int getPubPropId() {
        return this.pubPropId;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    void setType(CvTerm type) {
        this.cvTerm = type;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public String getValue() {
        return this.value;
    }

    void setValue(String value) {
        this.value = value;
    }

    public Integer getRank() {
        return this.rank;
    }

}
