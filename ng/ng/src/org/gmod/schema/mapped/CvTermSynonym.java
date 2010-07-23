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
@Table(name = "cvtermsynonym")
public class CvTermSynonym implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "cvtermsynonym_cvtermsynonym_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "cvtermsynonym_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int cvTermSynonymId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cvterm_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTermByCvTermId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = true, insertable = true, updatable = true)
    private CvTerm cvTermByTypeId;

    @Column(name = "synonym", unique = false, nullable = false, insertable = true, updatable = true, length = 1024)
    private String synonym;


    // Constructors

    CvTermSynonym() {
        // Deliberately empty default constructor
    }

    public CvTermSynonym(CvTerm cvTerm, String synonym, CvTerm type) {
        this.cvTermByCvTermId = cvTerm;
        this.cvTermByTypeId = type;
        this.synonym = synonym;
    }

    // Property accessors

    public int getCvTermSynonymId() {
        return this.cvTermSynonymId;
    }

    public CvTerm getCvTerm() {
        return this.cvTermByCvTermId;
    }

    public CvTerm getType() {
        return this.cvTermByTypeId;
    }

    public String getSynonym() {
        return this.synonym;
    }
}
