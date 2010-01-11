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
@Table(name = "pubauthor")
public class PubAuthor implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "pubauthor_pubauthor_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "pubauthor_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int pubAuthorId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @Column(name = "editor", unique = false, nullable = true, insertable = true, updatable = true)
    private Boolean editor;

    @Column(name = "surname", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
    private String surname;

    @Column(name = "givennames", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    private String givenNames;

    @Column(name = "suffix", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
    private String suffix;


    // Constructors

    PubAuthor() {
        // Deliberately empty default constructor
    }

    public PubAuthor(Pub pub, int rank, Boolean editor, String givenNames, String surname,
            String suffix) {
        this.pub = pub;
        this.rank = rank;
        this.editor = editor;
        this.givenNames = givenNames;
        this.surname = surname;
        this.suffix = suffix;
    }


    // Property accessors

    public int getPubAuthorId() {
        return this.pubAuthorId;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public int getRank() {
        return this.rank;
    }

    public Boolean isEditor() {
        return this.editor;
    }

    void setEditor(Boolean editor) {
        this.editor = editor;
    }

    public String getSurname() {
        return this.surname;
    }

    void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGivenNames() {
        return this.givenNames;
    }

    public void setGivenNames(String givenNames) {
        this.givenNames = givenNames;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
