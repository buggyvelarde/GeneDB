package org.genedb.querying.history;

import org.genedb.querying.core.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HistoryItem implements Serializable {

    private String name;

    /*
     * the internal representation of the query
     * in the form 'Organisms:pfalciparum;;Category:biological_process;;
     * Term:cell-cell adhesion'
     */
    private String internalName;

    private Query query;

    public void setQuery(Query query) {
        this.query = query;
    }

    private List<String> ids;

    private HistoryType historyType = HistoryType.QUERY;


    public HistoryItem(String name) {
        this.name = name;
        this.internalName = name;
        this.ids = new ArrayList<String>();
    }

    public HistoryItem(String name, List<String> ids) {
        this.name = name;
        this.internalName = name;
        this.ids = ids;
    }

    public HistoryItem(String name, String id) {
        this.name = name;
        this.internalName = name;
        this.ids = new ArrayList<String>();
        this.ids.add(id);
    }

    public HistoryItem(HistoryItem historyItem) {
        this.name = historyItem.name;
        this.ids = new ArrayList<String>(historyItem.getIds());
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberItems() {
        return ids.size();
    }

    public void addResult(String id) {
        ids.add(id);
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public void union(HistoryItem i) {
        this.ids.addAll(i.getIds());
    }

    public void intersect(HistoryItem i) {
        this.ids.retainAll(i.getIds());
    }

    public Query getQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void addUniqueResult(String id) {
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    public boolean containsEntry(String uniqueName) {
        return ids.contains(uniqueName);
    }

    public void removeNum(int index) {
        ids.remove(index);
    }
}
