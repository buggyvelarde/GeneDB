package org.genedb.querying.history;

import java.util.LinkedHashMap;
import java.util.List;

import org.genedb.querying.core.PagedQuery;
import org.genedb.querying.core.Query;

public interface HistoryManager {

    public abstract LinkedHashMap<String, HistoryItem> getHistoryItems();

    public HistoryItem getHistoryItemByName(String name);

    public HistoryItem getHistoryItemByType(HistoryType historyType);
    
    public HistoryItem addHistoryItem(String name, HistoryType historyType);
    
    public QueryHistoryItem addQueryHistoryItem(String name, PagedQuery query);
    public QueryHistoryItem getQueryHistoryItem(String name);

    public abstract  HistoryItem addHistoryItem(String name, HistoryType type, List<String> ids);

    public abstract HistoryItem addHistoryItem(String name, HistoryType type, String id);

    public abstract HistoryItem addHistoryItem(HistoryType type, String id);

    public abstract String getCartName();

    //public abstract String getNextName();

    //public abstract int getVersion();
    //public abstract void removeItem(int item, int version);
    
    public abstract void removeItem(String name);
    
    public HistoryItem getHistoryItemByID(int id);
    
    public abstract int getNumHistoryItems();

    public abstract String getFormalName(String name);

}