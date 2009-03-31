package org.genedb.db.audit;

import java.sql.SQLException;
/**
 *
 * @author LO
 *
 */
public class MockChangeTrackerImpl implements ChangeTracker {

    private MockChangeSetImpl changeSet = new MockChangeSetImpl();

    public void setChangeSet(MockChangeSetImpl changeSet) {
        this.changeSet = changeSet;
    }

    @Override
    public ChangeSet changes(String key) throws SQLException {
        return changeSet;
    }

}
