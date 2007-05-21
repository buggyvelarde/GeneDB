package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.List;

public class HistoryItem {
	
	private String name;
	
	private List<String> ids;
	
	public HistoryItem(String name, List<String> ids) {
		this.name = name;
		this.ids = ids;
	}
	
	public HistoryItem(String name, String id) {
		this.name = name;
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

    public void union(HistoryItem i) {
        this.ids.addAll(i.getIds());
    }

    public void intersect(HistoryItem i) {
        this.ids.retainAll(i.getIds());
    }

}
