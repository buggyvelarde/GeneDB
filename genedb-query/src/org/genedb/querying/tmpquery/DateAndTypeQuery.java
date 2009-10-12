package org.genedb.querying.tmpquery;

public class DateAndTypeQuery extends DateQuery {
	
	@Override
    protected String getHql() {
        System.err.println("date in hql is '"+date+"'");
        String operator = after ? ">" : "<";
        String typeOfDate = created ? "timeAccessioned" : "timeLastModified";
        return String.format("select f.uniqueName, c.name, f.featureId from Feature f, CvTerm c where f.type = c.cvTermId and f.%s %s :date @ORGANISM@ ", typeOfDate, operator);
    }
}
