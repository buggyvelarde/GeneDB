package org.genedb.querying.tmpquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.genedb.querying.core.QueryParam;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;


public class DbxrefQuery extends OrganismHqlQuery {
	
	private static final Logger logger = Logger.getLogger(DbxrefQuery.class);
	private static final long serialVersionUID = 5845342305225726744L;
	
	@QueryParam(
            order=1,
            title="The dbxref"
    )
    protected String dbxref;
	
	public void setDbxref(String dbxref) {
		this.dbxref = dbxref;
	}
	
	public String getDbxref() {
		return dbxref;
	}
	
	List<String> queries = new ArrayList<String>();
	
	public DbxrefQuery() {
		
		/*
		 * HQL doesn't have a union, so we have to merge the results of several queries in this case. If this is a common use case then
		 * this sort of approach could be abstracted to a base class of its own. 
		 * */
		
		queries.add(
				" select f.uniqueName from FeatureDbXRef fd  " +
					" inner join fd.feature f " + 
					" inner join fd.dbXRef d " +
					" where d.accession = :dbxref " +
					" @ORGANISM@ " +
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
					" order by f.organism, f.uniqueName ");
		
		queries.add(
				" select f.uniqueName from FeatureCvTermDbXRef fcd " +
					" inner join fcd.featureCvTerm fc  " +
					" inner join fc.feature f " + 
					" inner join fcd.dbXRef d " +
					" where d.accession = :dbxref  " +
					" @ORGANISM@ " +
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
					" order by f.organism, f.uniqueName ");
		
		queries.add(
				" select f.uniqueName from Pub p, PubDbXRef pd, FeaturePub fp " +
					" inner join pd.dbXRef d " +
					" inner join fp.feature f " +
					" where d.accession = :dbxref  and pd.pub = p  and fp.pub = p " +
					" @ORGANISM@ " +
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
					" order by f.organism, f.uniqueName " );
		
		queries.add(
				" select f.uniqueName from PubDbXRef pd, FeatureCvTermPub fcp " + 
					" inner join fcp.featureCvTerm fc " + 
					" inner join fc.feature f " +
					" inner join fcp.pub p " + 
					" inner join pd.dbXRef d " + 
					" where d.accession = :dbxref  and pd.pub = p  " + 
					" @ORGANISM@ " + 
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES + 
					" order by f.organism, f.uniqueName " );
				
	}
	
	
	@Override
	protected List runQuery() {
		
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        Set resultsSet = new TreeSet();
        
        /* 
         * run an merge several queries, they're only returning lists of uniqueNames and are exact match searches,
         * so this should not be too expensive
         */
        for (String hql : queries) {
        	
        	String concat_hql = restrictQueryByOrganism(hql, getOrganismHql());
            
            try {
            
	            org.hibernate.Query query = session.createQuery(concat_hql);
	            populateQueryWithParams(query);
	            logger.info(query.getQueryString());
	
	            //Set max result to prevent max memory error
	            query.setMaxResults(getMaxResults());
	            
            	//Run query
                List ret = query.list();
                if (ret.size() > 0) {
                	resultsSet.addAll(ret);
                }
                
            } catch (Exception e) {
            	e.printStackTrace();
            	logger.error(e.getMessage());
            }
            
        }
        
        List results = new ArrayList(resultsSet);
        logger.info(results);
        
        //Get the result size
        if (results!= null && getMaxResults()==results.size()){
            isActualResultSizeSameAsMax = true;
        }
        
        return results;
    }

	@Override
	public String getQueryDescription() {
		return "Returns a list of genes that have dbxrefs matching the search term";
	}

	@Override
	public String getQueryName() {
		return "Dbxrefs";
	}
	
	@Override
    protected String[] getParamNames() {
        return new String[] {"dbxref"};
    }

    @Override
    protected void populateQueryWithParams(org.hibernate.Query query) {
    	super.populateQueryWithParams(query);
        query.setString("dbxref", dbxref);
    }

	@Override
	protected String getHql() {
		return null;
	}
	

}
