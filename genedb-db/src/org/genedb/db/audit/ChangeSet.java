package org.genedb.db.audit;

import java.sql.SQLException;
import java.util.List;

public interface ChangeSet {

    public List<Integer> newTranscripts();
    public List<Integer> changedTranscripts();
    public List<Integer> deletedTranscripts();

    public List<Integer> newTopLevelFeatures();
    public List<Integer> changedTopLevelFeatures();
    public List<Integer> deletedTopLevelFeatures();

    public void commit() throws SQLException;
}
