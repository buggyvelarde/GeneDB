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
@Table(name = "cvtermpath")
public class CvTermPath implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "cvtermpath_cvtermpath_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "cvtermpath_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int cvTermPathId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTermBySubjectId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTermByObjectId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = true, insertable = true, updatable = true)
    private CvTerm cvTermByTypeId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Cv cv;

    @Column(name = "pathdistance", unique = false, nullable = true, insertable = true, updatable = true)
    private Integer pathDistance;

    // Constructor
    CvTermPath() {
        // Deliberately empty default constructor
    }

    public CvTermPath(Cv cv, CvTerm cvTermByTypeId, CvTerm cvTermBySubjectId,
            CvTerm cvTermByObjectId, Integer pathDistance) {
        this.cv = cv;
        this.cvTermByTypeId = cvTermByTypeId;
        this.cvTermBySubjectId = cvTermBySubjectId;
        this.cvTermByObjectId = cvTermByObjectId;
        this.pathDistance = pathDistance;
    }

    // Property accessors
    public int getCvTermPathId() {
        return this.cvTermPathId;
    }

    public CvTerm getCvTermBySubjectId() {
        return this.cvTermBySubjectId;
    }

    public CvTerm getCvTermByObjectId() {
        return this.cvTermByObjectId;
    }

    public CvTerm getCvTermByTypeId() {
        return this.cvTermByTypeId;
    }

    public Cv getCv() {
        return this.cv;
    }

    public Integer getPathDistance() {
        return this.pathDistance;
    }
}
