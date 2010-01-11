package org.genedb.querying.tmpquery;


/**
 * Similar to DateQuery, but returns a count.
 * @author gv1
 *
 */
public class DateCountQuery extends DateQuery {
	
	@Override
    public String getQueryDescription() {
    	return "Returns the number of features changed in an oranism since a certain date.";
    }
	
	@Override
    protected String getHql() {
        System.err.println("date in hql is '"+date+"'");
        String operator = after ? ">" : "<";
        String typeOfDate = created ? "timeAccessioned" : "timeLastModified";
        return String.format("select count(*) from Feature f where f.%s %s :date @ORGANISM@ ", typeOfDate, operator);
    }
	
	
}
