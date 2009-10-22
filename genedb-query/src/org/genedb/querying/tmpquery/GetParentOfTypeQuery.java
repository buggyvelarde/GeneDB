package org.genedb.querying.tmpquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genedb.querying.core.HqlQuery;
import org.genedb.querying.core.QueryException;
import org.hibernate.Query;
import org.springframework.validation.Errors;

public class GetParentOfTypeQuery extends HqlQuery {
	
	private static final long serialVersionUID = -1784633580608160063L;

	private static final Logger logger = Logger.getLogger(GetParentOfTypeQuery.class);
	
	private String type;
	private int id;
	
	private final Map<String, String> depths = new HashMap<String, String>();
	
	private final String[] queryStrings;
	
	public GetParentOfTypeQuery()
	{
		depths.put("repeat_region", "0");
		depths.put("region", "0");
		depths.put("gene", "0");
		depths.put("pseudogene", "0");
		
		depths.put("tRNA", "1");
		depths.put("mRNA", "1");
		
		depths.put("exon", "2");
		depths.put("pseudogenic_exon", "2");
		depths.put("pseudogenic_transcript", "2");
		depths.put("transcript", "2");
		depths.put("polypeptide", "2");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT f2.featureId, f2.uniqueName, f2.type ");
		sb.append("FROM Feature f1, FeatureRelationship fr, Feature f2 ");
		sb.append("WHERE f1.featureId = :id ");
		sb.append("AND fr.subjectFeature = f1.featureId ");
		sb.append("AND fr.objectFeature = f2.featureId ");
		sb.append("AND fr.type.name != 'orthologous_to' ");
		
		
		StringBuffer sb2 = new StringBuffer();
		
		/*
		 * SELECT f3.feature_id, f3.uniquename, f3.type_id
			FROM feature f3, feature_relationship fr2
			WHERE
			fr2.subject_id = (
			
				SELECT f2.feature_id
				FROM feature f1, feature f2, feature_relationship fr1
				WHERE f1.feature_id = 287
				AND fr1.subject_id = f1.feature_id
				AND fr1.object_id = f2.feature_id
			
			)
			AND fr2.object_id = f3.feature_id
		 * */
		
		sb2.append("SELECT f3.featureId, f3.uniqueName, f3.type ");
		sb2.append("FROM Feature f3, FeatureRelationship fr2 ");
		sb2.append("WHERE ");
		sb2.append(" fr2.subjectFeature in (   ");
		sb2.append("       SELECT f2.featureId ");
		sb2.append("       FROM Feature f1, FeatureRelationship fr1, Feature f2 ");
		sb2.append("       WHERE f1.featureId = :id ");
		sb2.append("       AND fr1.subjectFeature = f1.featureId ");
		sb2.append("       AND fr1.objectFeature = f2.featureId ");
		sb2.append("       AND fr1.type.name != 'orthologous_to' ");
		sb2.append(" ) ");
		sb2.append("AND fr2.objectFeature = f3.featureId ");
		sb2.append("AND fr2.type.name != 'orthologous_to' ");
		
		queryStrings = new String[] {sb.toString(), sb2.toString()}; 
		
	}
	
	
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List getResults() throws QueryException {
		if(isRoot(type))
			return null;
		return super.getResults();
	}
	
	private String getQueryString()
	{
		if (! depths.containsKey(type))
		{
			return "";
		}
		else if (depths.get(type).equals("1"))
		{
			return queryStrings[0];
		}
		return queryStrings[1];
	}
	
	
	@Override
	protected void extraValidation(Errors errors) {
		// TODO Auto-generated method stub
	}

	@Override
	protected String getHql() {
		String queryString = getQueryString(); 
        logger.debug(queryString);
        return queryString;
	}

	@Override
	protected String getOrganismHql() {
		return null;
	}

	@Override
	protected String[] getParamNames() {
		return new String[] { "type", "id" };
	}

	@Override
	protected void populateQueryWithParams(Query query) {
		query.setParameter("id", id);
		// TODO Auto-generated method stub
	}

	private boolean isRoot(String type)
	{
		if ((! depths.containsKey(type)) || (depths.get(type).equals("0")))
		{
			return true;
		}
		return false;
	}
}
