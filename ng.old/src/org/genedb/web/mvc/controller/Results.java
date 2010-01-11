package org.genedb.web.mvc.controller;

import java.util.List;

public interface Results {
    
    public List<String> getIds();

    public void setIds(List<String> ids);
    
    public int getNumberItems();

    public void addResult(String id);
    
  public void union(Results i);

    public void intersect(Results i);
}
