package org.genedb.query;

import java.util.Iterator;


public interface Result extends Iterable {
    
    /**
     * Each Result has a non-null name. This is either generated 
     * automatically, or assigned by a user.
     * 
     * @return the name of the Result
     */
    public String getName();

    /**
     * Assigns new name, typically user-friendly, to this Result
     * 
     * @param name the new name
     * @throws NullPointerException if the name submitted is null
     */
    public void setName(String name);

    /**
     * Has this Result been edited eg particular hits 
     * manually removed.
     * 
     * @return false, if the contents exactly match the results 
     * of the original query.
     */
    public boolean isEdited();

    public String getQueryAsString();

    /**
     * The data in a Result will be of a particular type, in the 
     * MIME-type sense. It may represent eg features, alignments, motifs etc. 
     * In the case of a heterogenuous dataset, the type is the most generic
     * 
     * @return the type of data
     */
    public String getType();
    
//  public boolean addAll(Result merge);

    public boolean isEmpty();

    public Iterator iterator();

    public boolean remove(Object arg0);

    /**
     * Create a new Result which has all the elements of the current Result, <b>except</b> 
     * those also in the parameter Result. ie subtract the entries
     * 
     * @param except The members of <code>Result</code> to remove
     * @return a new Result.
     */
    public Result except(Result except);
    
    /**
     * Create a new Result which is the <b>union</b> of both Results. ie merge
     * 
     * @param union The members of Result to merge with this one
     * @return a new Result.
     */
    public Result union(Result union);
    
    /**
     * Create a new Result which is the <b>intersection</b> of both Results. 
     * 
     * @param intersect The members of Result to join with this one
     * @return a new Result
     */
    public Result intersect(Result intersect);

//  public boolean retainAll(Result rds);

    public int size();

    public void add(Object o);
    
}
