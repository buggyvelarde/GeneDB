package org.genedb.query.history;

import org.genedb.query.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleHistory implements History {

    private Map<String, List<Result>> entries = new HashMap<String, List<Result>>();

    public Set<String> getTypes() {
        return entries.keySet();
    }

    public List<Result> getResults(String type) {
        return entries.get(type);
    }

    public void addResult(Result rds) {
        if (!entries.containsKey(rds.getType())) {
            entries.put(rds.getType(), new ArrayList<Result>());
        }
        entries.get(rds.getType()).add(rds);
    }

    public void clear() {
        entries.clear();
    }

    public boolean isFilled() {
        return !entries.isEmpty();
    }

    public Iterator<String> keyIterator() {
        return entries.keySet().iterator();
    }

    public int size() {
        return entries.size();
    }
}
