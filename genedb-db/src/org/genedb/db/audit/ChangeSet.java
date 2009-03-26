package org.genedb.db.audit;

import org.gmod.schema.mapped.Feature;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Represents a set of changes mined from the audit logs.
 *
 * @author rh11
 *
 */
public interface ChangeSet {

    /**
     * Find out which features have been added within this change set.
     * Bear in mind that the change set is frozen at the time it's created
     * (i.e. when {@link ChangeTracker#changeSet(String)} is called), so there
     * is no guarantee that the features listed by this method still exist
     * in the database. They may have been deleted since the change set was
     * frozen.
     *
     * @param featureClass the class of feature you're interested in
     * @return a collection of the IDs of the newly-created features
     */
    public abstract Collection<Integer> newFeatureIds(Class<? extends Feature> featureClass);

    /**
     * Find out which features have been modified within this change set.
     * This includes only features that existed prior to the change set being
     * frozen, so there is no overlap with the results of <code>newFeatureIds</code>.
     * <p>
     * Bear in mind that the change set is frozen at the time it's created
     * (i.e. when {@link ChangeTracker#changeSet(String)} is called), so there
     * is no guarantee that the features listed by this method still exist
     * in the database. They may have been deleted since the change set was
     * frozen.
     *
     * @param featureClass the class of feature you're interested in
     * @return a collection of the IDs of the modified features
     */
    public abstract Collection<Integer> changedFeatureIds(Class<? extends Feature> featureClass);

    /**
     *
     * Since feature IDs are not reused, the features returned by this method are
     * guaranteed not to exist in the database (except in extraordinary circumstances,
     * as for example if the change set was created in a transaction that has subsequently
     * been rolled back).
     *
     * @param featureClass the class of feature you're interested in
     * @return a collection of the IDs of the deleted features
     */
    public abstract Collection<Integer> deletedFeatureIds(Class<? extends Feature> featureClass);

    /**
     * Commit the change set, indicating that it has been successfully processed.
     * The next time a change set is requested with the same key, it will not include
     * any of the changes represented in this change set.
     *
     * @throws SQLException
     */
    public abstract void commit() throws SQLException;
}
