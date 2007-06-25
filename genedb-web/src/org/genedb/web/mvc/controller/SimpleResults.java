package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.List;

public class SimpleResults implements Results {
	
	private List<String> ids;
	
	private HistoryType historyType = HistoryType.QUERY;
	

	
	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	
	public int getNumberItems() {
		return ids.size();
	}

	public void addResult(String id) {
		ids.add(id);
	}
	
  public void union(SimpleResults i) {
        this.ids.addAll(i.getIds());
    }

    public void intersect(SimpleResults i) {
        this.ids.retainAll(i.getIds());
    }

	public void intersect(Results i) {
		// TODO Auto-generated method stub
		
	}

	public void union(Results i) {
		// TODO Auto-generated method stub
		
	}
}
