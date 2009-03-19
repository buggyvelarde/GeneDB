package org.genedb.db.audit;

import java.sql.SQLException;

public interface ChangeTracker {
    public ChangeSet changes() throws SQLException;
}
