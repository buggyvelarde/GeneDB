package org.genedb.web.mvc.model;

import org.genedb.db.audit.ChangeSet;

/**
 * Marks a program that can accept a changeset, as part of
 * a two-phase commit system
 */
public interface IndexUpdater {

    /**
     * The entry point for processing a given list of C(R)UD changes
     *
     * @param cs The list of changes
     * @return true if the update was successful
     */
    boolean indexFeatures(ChangeSet cs);

    /**
     * Called if all the indexing operations have succeeded, to make it really happen
     */
    void applyChanges();

}
