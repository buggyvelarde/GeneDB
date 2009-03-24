package org.genedb.db.audit;

import java.sql.SQLException;
/**
 * 
 * @author LO
 *
 */
public class MockChangeTrackerImpl implements ChangeTracker {
    private MockChangeSetImpl changeSet = new MockChangeSetImpl();
    @Override
    public ChangeSet changes() throws SQLException {
        return changeSet;
    }

}
