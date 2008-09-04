package org.genedb.medusa.changelog.messages;

import java.util.Date;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.medusa.changelog.ReviewStatus;

/**
 * A class to represent a requested change in the db. It is permanently kept as
 * a reporting aid.
 *
 * @author art
 */
public class ChangeLogMessage {

    private int id;

    // Housekeeping
    private String submittedBy;
    private Date submissionDate;

    private String reviewedBy;
    private Date reviewDate;

    private ReviewStatus status;

    // Real content
    private TaxonNode taxonNode;

    private String featureSystematicId;

}

