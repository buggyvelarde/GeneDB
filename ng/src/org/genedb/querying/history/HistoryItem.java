package org.genedb.querying.history;

import org.apache.log4j.Logger;
import org.genedb.querying.core.PagedQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryItem implements Serializable {
	
	private Logger logger = Logger.getLogger(HistoryItem.class);
	
    private String name;

    /*
     * the internal representation of the query
     * in the form 'Organisms:pfalciparum;;Category:biological_process;;
     * Term:cell-cell adhesion'
     */
    private String internalName;

    protected PagedQuery query;

    public void setQuery(PagedQuery query) {
        this.query = query;
    }
    
    // we only want unique values here...
    protected Set<String> ids = new HashSet<String>();

    private HistoryType historyType = HistoryType.QUERY;
    
    public void cleanup() {
    	query = null;
    	ids = null;
    }

    public HistoryItem(String name) {
        this.name = name;
        this.internalName = name;
        this.ids = new HashSet<String>(1);
    }

    public HistoryItem(String name, List<String> ids) {
        this.name = name;
        this.internalName = name;
        this.ids = new HashSet<String>(ids);
    }

    public HistoryItem(String name, String id) {
        this.name = name;
        this.internalName = name;
        this.ids = new HashSet<String>();
        this.ids.add(id);
    }

    public HistoryItem(HistoryItem historyItem) {
        this.name = historyItem.name;
        this.ids = new HashSet<String>(historyItem.getIds());
    }
    
    public List<String> getIds() {
        return new ArrayList<String>(ids);
    }

    public void setIds(List<String> ids) {
        this.ids = new HashSet<String>(ids);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberItems() {
        return getIds().size();
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

    public PagedQuery getQuery() {
        return query;
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
