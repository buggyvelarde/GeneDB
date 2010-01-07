package org.genedb.querying.tmpquery;

import java.util.Date;
import org.genedb.query.sql.SqlQuery;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * 
 * Returns all the genes, their children and their grand-children, which have got timelastmodified stamps greater than a certain date, for a particular organism.
 * 
 * @author gv1
 *
 */
public class ChangedGeneFeaturesQuery extends SqlQuery {
	
	private Date date;
	private int organismId;
	
	
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	public void setOrganismId(int organismId)
	{
		this.organismId = organismId;
	}
	
	public ChangedGeneFeaturesQuery()
	{
		/*
		 	SELECT feature_id as id, uniquename as uniquename, c1.name as type, timelastmodified as time, feature_id as rootID, uniquename as rootName, c1.name as rootType
			FROM feature, cvterm c1
			WHERE timelastmodified >= DATE '2009-06-01' 
			AND organism_id = 14 
			AND feature.type_id IN ('792', '423')
			AND c1.cvterm_id = feature.type_id
			UNION 
			SELECT f1.feature_id as id, f1.uniquename as uniquename, c1.name as type, f1.timelastmodified as time, f2.feature_id as rootID, f2.uniquename as rootName, c2.name as rootType
			FROM feature f1, feature f2, feature_relationship fr, cvterm c1, cvterm c2
			WHERE f1.timelastmodified >= DATE '2009-06-01' 
			AND f1.organism_id = 14 
			AND f2.type_id in ('792', '423')
			AND fr.subject_id = f1.feature_id
			AND fr.object_id = f2.feature_id
			AND c1.cvterm_id = f1.type_id
			AND c2.cvterm_id = f2.type_id
			UNION
			SELECT f1.feature_id as id, f1.uniquename as uniquename, c1.name as type, f1.timelastmodified as time, f3.feature_id as rootID, f3.uniquename as rootName, c2.name as rootType
			FROM feature f1, feature f2, feature f3, feature_relationship fr, feature_relationship fr2, cvterm c1, cvterm c2
			WHERE f1.timelastmodified >= DATE '2009-06-01' 
			AND f1.organism_id = 14 
			AND f3.type_id in ('792', '423')
			AND fr.subject_id = f1.feature_id
			AND fr.object_id = f2.feature_id
			AND fr2.subject_id = f2.feature_id
			AND fr2.object_id = f3.feature_id
			AND c1.cvterm_id = f1.type_id
			AND c2.cvterm_id = f3.type_id
		 */
		
		StringBuffer q = new StringBuffer();
		
		q.append(" SELECT feature_id as id, uniquename as uniquename, c1.name as type, timelastmodified as time, feature_id as rootID, uniquename as rootName, c1.name as rootType ");
		q.append(" FROM feature, cvterm c1 ");
		q.append(" WHERE timelastmodified >= ? ");
		q.append(" AND organism_id = ? ");
		q.append(" AND feature.type_id IN ('792', '423') ");
		q.append(" AND c1.cvterm_id = feature.type_id ");
		q.append(" UNION ");
		q.append(" SELECT f1.feature_id as id, f1.uniquename as uniquename, c1.name as type, f1.timelastmodified as time, f2.feature_id as rootID, f2.uniquename as rootName, c2.name as rootType ");
		q.append(" FROM feature f1, feature f2, feature_relationship fr, cvterm c1, cvterm c2 ");
		q.append(" WHERE f1.timelastmodified >= ? ");
		q.append(" AND f1.organism_id = ? ");
		q.append(" AND f2.type_id in ('792', '423') ");
		q.append(" AND fr.subject_id = f1.feature_id ");
		q.append(" AND fr.object_id = f2.feature_id ");
		q.append(" AND c1.cvterm_id = f1.type_id ");
		q.append(" AND c2.cvterm_id = f2.type_id ");
		q.append(" UNION ");
		q.append(" SELECT f1.feature_id as id, f1.uniquename as uniquename, c1.name as type, f1.timelastmodified as time, f3.feature_id as rootID, f3.uniquename as rootName, c2.name as rootType ");
		q.append(" FROM feature f1, feature f2, feature f3, feature_relationship fr, feature_relationship fr2, cvterm c1, cvterm c2 ");
		q.append(" WHERE f1.timelastmodified >= ? ");
		q.append(" AND f1.organism_id = ? ");
		q.append(" AND f3.type_id in ('792', '423') ");
		q.append(" AND fr.subject_id = f1.feature_id ");
		q.append(" AND fr.object_id = f2.feature_id ");
		q.append(" AND fr2.subject_id = f2.feature_id ");
		q.append(" AND fr2.object_id = f3.feature_id ");
		q.append(" AND c1.cvterm_id = f1.type_id ");
		q.append(" AND c2.cvterm_id = f3.type_id ");
		
		this.setSql(q.toString());
	}
	
	@Override
	public void processCallBack(RowCallbackHandler callBack) 
	{
		// must setup the args object before calling the query...
		args = new Object [] { date, organismId, date, organismId, date, organismId };
		super.processCallBack(callBack);
	}
	
	
	

}




