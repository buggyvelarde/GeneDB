package org.genedb.querying.tmpquery;


public class DateWithAncestorsQuery extends DateQuery {
	
	public enum QueryType {
		NO_PARENT,
		PARENT,
		GRANDPARENT
	}
	
	private final String[] queryStrings;
	private QueryType queryType;
	
	public DateWithAncestorsQuery()
	{
		String operator = after ? ">" : "<";
        String typeOfDate = created ? "timeAccessioned" : "timeLastModified";
        
		String q1 = String.format("SELECT f.uniqueName, f.featureId, f.type FROM Feature f where f.%s %s :date @ORGANISM@ order by f.organism  ", typeOfDate, operator);
		
		StringBuffer q2 = new StringBuffer();
		
		q2.append("SELECT f1.uniqueName, f1.featureId, f1.type, fr1.type, f2.uniqueName, f2.featureId, f2.type ");
		q2.append("FROM Feature f1, Feature f2, FeatureRelationship fr1 ");
		q2.append(String.format
		         ("WHERE f1.featureId in ( SELECT f.featureId FROM Feature f where f.%s %s :date @ORGANISM@ order by f.organism   ) ", typeOfDate, operator));
		q2.append("AND fr1.subjectFeature = f1.featureId ");
		q2.append("AND fr1.objectFeature = f2.featureId ");
		q2.append("AND fr1.type.name != 'orthologous_to' ");
		
		StringBuffer q3 = new StringBuffer();
		
		q3.append("SELECT f1.uniqueName, f1.featureId, f1.type, fr1.type, f2.uniqueName, f2.featureId, f2.type, fr2.type, f3.uniqueName, f3.featureId, f3.type ");
		q3.append("FROM Feature f1, Feature f2, Feature f3, FeatureRelationship fr1, FeatureRelationship fr2 ");
		q3.append(String.format
		         ("WHERE f1.featureId in ( SELECT f.featureId FROM Feature f where f.%s %s :date @ORGANISM@ order by f.organism   ) ", typeOfDate, operator));
		q3.append("AND fr1.subjectFeature = f1.featureId ");
		q3.append("AND fr1.objectFeature = f2.featureId ");
		q3.append("AND fr1.type.name != 'orthologous_to' ");
		q3.append("AND fr2.subjectFeature = f2.featureId ");
		q3.append("AND fr2.objectFeature = f3.featureId ");
		q3.append("AND fr2.type.name != 'orthologous_to' ");
		
		queryStrings = new String[] {
			q1,
			q2.toString(),
			q3.toString()
		};
		
	}
	
	public void setQueryType(QueryType queryType)
	{
		this.queryType = queryType;
	}
	
	public QueryType getQueryType()
	{
		return this.queryType;
	}
	
	@Override
	protected String getHql() {
		if (queryType.equals(QueryType.GRANDPARENT))
		{
			return queryStrings[2];
		}
		else if (queryType.equals(QueryType.PARENT))
		{
			return queryStrings[1];
		}
		return queryStrings[0];
	}

	
	
}
