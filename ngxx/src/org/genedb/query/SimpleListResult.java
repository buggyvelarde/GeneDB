package org.genedb.query;

import org.genedb.query.compatability.NullResultCompatibility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple implementation of Result, using an ArrayList internally.
 *
 * @author art
 */
public class SimpleListResult implements Result {

    private String name;
    private boolean edited;
    private String type;
    private List<Object> list = new ArrayList<Object>();
    private String query;
    private ResultCompatibility resultCompatability;
    
    /**
     * Get the <code>ResultCompatability</code> which this result is 
     * using for comparing
     * 
     * @return the compatability check, or NullResultCompatability 
     */
    public ResultCompatibility getResultCompatability() {
        if (this.resultCompatability == null) {
            return NullResultCompatibility.INSTANCE;
        }
        return this.resultCompatability;
    }

    /**
     * Set the ResultCompatability to use for this Result
     * 
     * @param resultCompatability
     */
    public void setResultCompatability(ResultCompatibility resultCompatability) {
        this.resultCompatability = resultCompatability;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEdited() {
        return edited;
    }

    public String getQueryAsString() {
        return query;
    }

    public String getType() {
        return type;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }
    
    public void add(Object o) {
        list.add(o);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }
    
    public Iterator iterator() {
        return list.iterator();
    }
    
    public Result union(Result result) {
        Result ret = new SimpleListResult();
        for (Object o : list) {
            ret.add(o);
        }
        for (Object o : result) {
            ret.add(o);
        }
        return ret;
    }
    
    /**
     * @see org.genedb.zoe.query.Result#except(org.genedb.zoe.query.Result)
     */
    public Result except(Result except) {
        Result ret = new SimpleListResult();
        for (Object o : list) {
            ret.add(o);
        }
        for (Object o : except) {
            ret.remove(o);
        }
        return ret;
    }

    /**
     * @see org.genedb.zoe.query.Result#intersect(org.genedb.zoe.query.Result)
     */
    public Result intersect(Result intersect) {
        Result ret = new SimpleListResult();
        for (Object o : intersect) {
            if (list.contains(o)) {
                ret.add(o);
            }
        }
        return ret;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("SimpleListResult name='");
        ret.append(name);
        ret.append("' size=");
        ret.append(size());
        return ret.toString();
    }




}
