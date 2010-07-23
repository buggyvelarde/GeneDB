package org.genedb.querying.tmpquery;

import java.util.Date;

import org.apache.log4j.Logger;
import org.genedb.query.sql.SqlQuery;
import org.springframework.jdbc.core.JdbcTemplate;
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
	private String type;
	private static final Logger logger = Logger.getLogger(ChangedGeneFeaturesQuery.class);
	
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	public void setOrganismId(int organismId)
	{
		this.organismId = organismId;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public ChangedGeneFeaturesQuery()
	{
		
		String queryString = " SELECT " +
			" f.uniquename as transcriptuniquename, " +
			" fctype.name as type, " +
			" mrna.uniquename as mrnauniquename, " +
			" gene.uniquename as geneuniquename, " +
			" fcp_detail.value as changedetail, " +
			" to_date (fcp_date.value, 'YYYYMMDD' ) as changedate, " +
			" fcp_user.value as changeuser " +
			
			" FROM feature f " +
			" JOIN feature_cvterm fc ON f.feature_id = fc.feature_id " + 
			" JOIN cvterm ctype ON f.type_id = ctype.cvterm_id AND ctype.name = 'polypeptide' " +
			
			" JOIN cvterm fctype ON fc.cvterm_id = fctype.cvterm_id  " +
			" JOIN cv fctypecv ON fctypecv.cv_id = fctype.cv_id AND fctypecv.name = 'annotation_change' " +
			
			" JOIN feature_cvtermprop fcp_date ON fc.feature_cvterm_id = fcp_date.feature_cvterm_id AND fcp_date.type_id = (select cvterm.cvterm_id from cvterm join cv on cv.cv_id = cvterm.cv_id and cv.name = 'feature_property' where cvterm.name = 'date' )  " +
			" JOIN feature_cvtermprop fcp_detail ON fc.feature_cvterm_id = fcp_detail.feature_cvterm_id AND fcp_detail.type_id = (select cvterm.cvterm_id from cvterm join cv on cv.cv_id = cvterm.cv_id and cv.name = 'genedb_misc' where cvterm.name = 'qualifier' )  " +
			" JOIN feature_cvtermprop fcp_user ON fc.feature_cvterm_id = fcp_user.feature_cvterm_id AND fcp_user.type_id = (select cvterm.cvterm_id from cvterm join cv on cv.cv_id = cvterm.cv_id and cv.name = 'genedb_misc' where cvterm.name = 'curatorName' ) " +
			
			" LEFT JOIN feature_relationship fr ON fr.subject_id = f.feature_id and fr.type_id IN (42, 69) " +
			" LEFT JOIN feature mrna ON fr.object_id = mrna.feature_id " +
			
			" LEFT JOIN feature_relationship fr2 ON fr2.subject_id = fr.object_id and fr2.type_id IN (42, 69) " +
			" LEFT JOIN feature gene ON fr2.object_id = gene.feature_id AND gene.type_id IN ('792', '423') " +
			
			" WHERE f.organism_id = ?  " +
			" AND to_date (fcp_date.value, 'YYYYMMDD' ) >= ? " ;
		
		this.setSql(queryString);
		
	
	}
	
	@Override
	public void processCallBack(RowCallbackHandler callBack) 
	{
		String the_sql = sql;
		
		if (type == null) {
			args = new Object [] { organismId, date };
		} else {
			the_sql += " AND fctype.name = ? ";
			args = new Object [] { organismId, date, type };
		}
		
		super.processCallBack(the_sql, args, callBack);
		
	}
	
	
	

}




