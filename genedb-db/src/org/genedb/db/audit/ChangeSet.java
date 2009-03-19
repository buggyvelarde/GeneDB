package org.genedb.db.audit;

import java.sql.SQLException;
import java.util.List;

public interface ChangeSet {

    public List<String> newTranscripts();
    public List<String> changedTranscripts();
    public List<String> deletedTranscripts();

    public List<String> newTopLevelFeatures();
    public List<String> changedTopLevelFeatures();
    public List<String> deletedTopLevelFeatures();

    public void commit() throws SQLException;
}
