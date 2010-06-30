package org.genedb.querying.tmpquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.genedb.querying.core.QueryParam;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * 
 * Searches the dbxrefs, and links them back to feature. 
 * 
 * @author gv1
 * 
 */
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
	
	private List<String> queries = new ArrayList<String>();
	
	public DbxrefQuery() {
		
		/*
		 * HQL doesn't have a union, so we have to merge the results of several queries in this case. If this is a common use case then
		 * this sort of approach could be abstracted to a base class of its own. 
		 * */
		
		queries.add(
				" select f.uniqueName from FeatureDbXRef fd  " +
					" inner join fd.feature f " + 
					" inner join fd.dbXRef dx " +
					" inner join dx.db d " +
					" where dx.accession = :dbxref :appendage " +
					" @ORGANISM@ " +
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
					" order by f.organism, f.uniqueName ");
		
		queries.add(
				" select f.uniqueName from FeatureCvTermDbXRef fcd " +
					" inner join fcd.featureCvTerm fc  " +
					" inner join fc.feature f " + 
					" inner join fcd.dbXRef dx " +
					" inner join dx.db d " +
					" where dx.accession = :dbxref :appendage  " +
					" @ORGANISM@ " +
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
					" order by f.organism, f.uniqueName ");
		
		queries.add(
				" select f.uniqueName from Pub p, PubDbXRef pd, FeaturePub fp " +
					" inner join pd.dbXRef dx " +
					" inner join fp.feature f " +
					" inner join dx.db d " +
					" where dx.accession = :dbxref  and pd.pub = p  and fp.pub = p :appendage " +
					" @ORGANISM@ " +
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
					" order by f.organism, f.uniqueName " );
		
		queries.add(
				" select f.uniqueName from PubDbXRef pd, FeatureCvTermPub fcp " + 
					" inner join fcp.featureCvTerm fc " + 
					" inner join fc.feature f " +
					" inner join fcp.pub p " + 
					" inner join pd.dbXRef dx " + 
					" inner join dx.db d " + 
					" where dx.accession = :dbxref  and pd.pub = p  :appendage " + 
					" @ORGANISM@ " + 
					RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES + 
					" order by f.organism, f.uniqueName " );
		
	}
	
	/**
	 * 
	 * Overrides the standard runQuery to run several HQL queries, one after the other. Also sanitises the 
	 * user-supplied dbxref, because this may be in the form 'DBNAME:ACCESSION' or just 'ACCESSION'.  
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List runQuery() {
		
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        Set<String> resultsSet = new TreeSet<String>();
        
        /*
         * Here we check to see if the user supplied a dbxref that has the db in it (e.g. PMID:14747138) or not (e.g. IPR003439). 
         * If the db has been supplied, then we split this and restrict the above HQLs to only those dbxrefs that belong to that
         * DB. If it's not been supplied, we use the user supplied dbxref as is. 
         */
        String tmp_dbxref = new String(dbxref);
        String hql_appendage = "";
        
        try {
        
	    	if (tmp_dbxref.contains(":")) {
	    		String[] dbxref_split = tmp_dbxref.split(":");
	    		String db_name = dbxref_split[0];
	    		tmp_dbxref = dbxref_split[1];
	    		hql_appendage = " and d.name = '" + db_name + "' ";
	    	}
	    	
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.error(e.getMessage());
        }
        
        logger.debug(tmp_dbxref);
		logger.debug(hql_appendage);
		
        
        /* 
         * Runs several queries, they're only returning lists of uniqueNames and are exact match searches,
         * so this should not be too expensive. Appends the resulting feature uniqueNames into the resultSet. 
         */
        for (String hql : queries) {
            try {
            	
            	hql = hql.replaceAll(":appendage", hql_appendage);
            	hql = restrictQueryByOrganism(hql, getOrganismHql());
            	
	            org.hibernate.Query query = session.createQuery(hql);
	            populateQueryWithParams(query, tmp_dbxref);
	            
	            logger.debug(query.getQueryString());
	
	            //Set max result to prevent max memory error
	            query.setMaxResults(getMaxResults());
	            
            	//Run query
                List<String> ret = query.list();
                
                if (ret.size() > 0) {
                	resultsSet.addAll(ret);
                }
                
            } catch (Exception e) {
            	e.printStackTrace();
            	logger.error(e.getMessage());
            }
            
        }
        
        List<String> results = new ArrayList<String>(resultsSet);
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

    /**
     * 
     * The use supplied dbxref may need trimming, and this method allows that trimmed string 
     * to be passed into the query, instead of what the user supplied.  
     * 
     * @param query
     * @param dbxref_param
     */
    protected void populateQueryWithParams(org.hibernate.Query query, String dbxref_param) {
    	super.populateQueryWithParams(query);
        query.setString("dbxref", dbxref_param);
    }

	@Override
	protected String getHql() {
		return null;
	}
	

}
