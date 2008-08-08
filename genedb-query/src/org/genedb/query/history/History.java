package org.genedb.query.history;

import org.genedb.query.Result;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface History {

    /**
     * Get a list of all the datatypes that have results associated with them
     *
     * @return a set of all the datatypes contained in the history
     */
    public Set<String> getTypes();


    /**
     * Get a list of datasets that contain a particular type
     *
     * @param type The MIME-type type of the dataset
     * @return a list of Results that contain a particular type
     */
    public List<Result> getResults(String type);


    /**
     * Add a new rds to the history
     *
     * @param rds
     */
    public void addResult(Result rds);


    /**
     * Empty the history
     */
    public void clear();


    /**
     * Check whether any results have been stored
     *
     * @return whether there are any entries in the history
     */
    public boolean isFilled();


    /**
     * Iterate over the types available
     *
     * @return
     */
    public Iterator<?> keyIterator();


    /**
     * The number of datatypes in this history
     *
     * @return the number of data types
     */
    public int size();

}