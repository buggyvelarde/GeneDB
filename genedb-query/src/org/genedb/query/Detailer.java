package org.genedb.query;

/**
 * An implementation of this knows how to fetch the items from a Result, and
 * get more details on them. This is typically unnecessary if the items are 
 * already objects (or proxies) but very useful if the results contains a list 
 * of eg systematic ids 
 * 
 * @author art
 */
public interface Detailer {
    
    /**
     * Fetch more details on the given item eg a gene summary from a systematic id 
     * 
     * @param o the item 
     * @return A more detailed object, typically a domain object or a map
     */
    public Object fetchDetailsFor(Object o);
    

    /**
     * Return what type of object the detailer returns ie domain object, map etc
     * 
     * @return
     */
    public String getDetailedObjectType();  // TODO Maybe should be enum returned
    
    
    /**
     * Get a name which can be used to choose a suitable view
     * 
     * @return the name, may be null if a domain object is returned
     */
    public String getDetailedObjectName();

}
