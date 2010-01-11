package org.genedb.querying.history;

import java.util.List;

public interface HistoryManager {

    public abstract List<HistoryItem> getHistoryItems();

    public HistoryItem getHistoryItemByName(String name);

    public HistoryItem getHistoryItemByType(HistoryType historyType);

    public abstract  HistoryItem addHistoryItem(String name, HistoryType type, List<String> ids);

    public abstract HistoryItem addHistoryItem(String name,HistoryType type, String id);

    public abstract HistoryItem addHistoryItem(HistoryType type, String id);

    public abstract String getCartName();

    public abstract String getNextName();

    public abstract int getVersion();

    public abstract void removeItem(int item, int version);

    public abstract int getNumHistoryItems();

    public abstract String getFormalName(String name);

}