package org.genedb.db.audit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MockChangeSetImpl implements ChangeSet {

    List<String> changedTopLevelNames = new ArrayList<String>();
    List<String> deletedTopLevelNames = new ArrayList<String>();
    List<String> newTopLevelNames = new ArrayList<String>();
    List<String> changedTranscriptNames = new ArrayList<String>();
    List<String> deletedTranscriptNames = new ArrayList<String>();
    List<String> newTranscriptNames = new ArrayList<String>();
    
    @Override
    public List<String> changedTopLevelFeatures() {
        return changedTopLevelNames;
    }

    @Override
    public List<String> changedTranscripts() {
        return changedTranscriptNames;
    }

    @Override
    public void commit() throws SQLException {
    }

    @Override
    public List<String> deletedTopLevelFeatures() {
        return deletedTopLevelNames;
    }

    @Override
    public List<String> deletedTranscripts() {
        return deletedTranscriptNames;
    }

    @Override
    public List<String> newTopLevelFeatures() {
        return newTopLevelNames;
    }

    @Override
    public List<String> newTranscripts() {
        return newTranscriptNames;
    }

    public List<String> getChangedTopLevelNames() {
        return changedTopLevelNames;
    }

    public void setChangedTopLevelNames(List<String> changedTopLevelNames) {
        this.changedTopLevelNames = changedTopLevelNames;
    }

    public List<String> getDeletedTopLevelNames() {
        return deletedTopLevelNames;
    }

    public void setDeletedTopLevelNames(List<String> deletedTopLevelNames) {
        this.deletedTopLevelNames = deletedTopLevelNames;
    }

    public List<String> getNewTopLevelNames() {
        return newTopLevelNames;
    }

    public void setNewTopLevelNames(List<String> newTopLevelNames) {
        this.newTopLevelNames = newTopLevelNames;
    }

    public List<String> getChangedTranscriptNames() {
        return changedTranscriptNames;
    }

    public void setChangedTranscriptNames(List<String> changedTranscriptNames) {
        this.changedTranscriptNames = changedTranscriptNames;
    }

    public List<String> getDeletedTranscriptNames() {
        return deletedTranscriptNames;
    }

    public void setDeletedTranscriptNames(List<String> deletedTranscriptNames) {
        this.deletedTranscriptNames = deletedTranscriptNames;
    }

    public List<String> getNewTranscriptNames() {
        return newTranscriptNames;
    }

    public void setNewTranscriptNames(List<String> newTranscriptNames) {
        this.newTranscriptNames = newTranscriptNames;
    }
    
    

}
