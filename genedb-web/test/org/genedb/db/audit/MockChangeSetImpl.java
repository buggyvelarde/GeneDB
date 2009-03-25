package org.genedb.db.audit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MockChangeSetImpl implements ChangeSet {

    List<Integer> changedTopLevelIds = new ArrayList<Integer>();
    List<Integer> deletedTopLevelIds = new ArrayList<Integer>();
    List<Integer> newTopLevelIds = new ArrayList<Integer>();
    List<Integer> changedTranscriptIds = new ArrayList<Integer>();
    List<Integer> deletedTranscriptIds = new ArrayList<Integer>();
    List<Integer> newTranscriptIds = new ArrayList<Integer>();
    
    @Override
    public List<Integer> changedTopLevelFeatures() {
        return changedTopLevelIds;
    }

    @Override
    public List<Integer> changedTranscripts() {
        return changedTranscriptIds;
    }

    @Override
    public void commit() throws SQLException {
    }

    @Override
    public List<Integer> deletedTopLevelFeatures() {
        return deletedTopLevelIds;
    }

    @Override
    public List<Integer> deletedTranscripts() {
        return deletedTranscriptIds;
    }

    @Override
    public List<Integer> newTopLevelFeatures() {
        return newTopLevelIds;
    }

    @Override
    public List<Integer> newTranscripts() {
        return newTranscriptIds;
    }

    public List<Integer> getChangedTopLevelIds() {
        return changedTopLevelIds;
    }

    public void setChangedTopLevelIds(List<Integer> changedTopLevelIds) {
        this.changedTopLevelIds = changedTopLevelIds;
    }

    public List<Integer> getDeletedTopLevelIds() {
        return deletedTopLevelIds;
    }

    public void setDeletedTopLevelIds(List<Integer> deletedTopLevelIds) {
        this.deletedTopLevelIds = deletedTopLevelIds;
    }

    public List<Integer> getNewTopLevelIds() {
        return newTopLevelIds;
    }

    public void setNewTopLevelIds(List<Integer> newTopLevelIds) {
        this.newTopLevelIds = newTopLevelIds;
    }

    public List<Integer> getChangedTranscriptIds() {
        return changedTranscriptIds;
    }

    public void setChangedTranscriptIds(List<Integer> changedTranscriptIds) {
        this.changedTranscriptIds = changedTranscriptIds;
    }

    public List<Integer> getDeletedTranscriptIds() {
        return deletedTranscriptIds;
    }

    public void setDeletedTranscriptIds(List<Integer> deletedTranscriptIds) {
        this.deletedTranscriptIds = deletedTranscriptIds;
    }

    public List<Integer> getNewTranscriptIds() {
        return newTranscriptIds;
    }

    public void setNewTranscriptIds(List<Integer> newTranscriptIds) {
        this.newTranscriptIds = newTranscriptIds;
    }
    
    

}
