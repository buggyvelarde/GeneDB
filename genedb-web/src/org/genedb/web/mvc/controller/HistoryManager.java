package org.genedb.web.mvc.controller;

import java.util.List;

public interface HistoryManager {

	public abstract List<HistoryItem> getHistoryItems();

	public abstract void addHistoryItems(String name, List<String> ids);
	
	public abstract void addHistoryItem(String name, String id);
	
	public abstract String getCartName();
	
	public abstract String getNextName();
	
	public abstract int getVersion();

	public abstract void removeItem(int item, int version);

	public abstract int getNumHistoryItems();

}