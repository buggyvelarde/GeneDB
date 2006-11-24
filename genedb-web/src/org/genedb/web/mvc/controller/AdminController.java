package org.genedb.web.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Ken Krebs
 */
public class AdminController extends MultiActionController implements InitializingBean {
    
 //   	private FeatureDao featureDao;
    	private DataSource dataSource;

	public void afterPropertiesSet() throws Exception {
//		if (clinic == null) {
//			throw new ApplicationContextException("Must set clinic bean property on " + getClass());
//		}
	}

	private static final String LOCK_QUERY_STRING = 
	    "select a.procpid, a.usename, a.current_query, a.backend_start, "+
	    " l.relation, c.relname, l.transactionid, l.mode, l.granted " +
	    "from pg_stat_activity a, pg_locks l, pg_class c " +
	    "where a.procpid = l.pid " +
	    " and l.relation = c.oid " +
	    "order by a.backend_start";
	
	// handlers
	@SuppressWarnings("unchecked")
	public ModelAndView LockExaminer(HttpServletRequest request, HttpServletResponse response) {
	    SimpleJdbcTemplate sjt = new SimpleJdbcTemplate(dataSource);
	    
	    List<Map<String, Object>> rows = sjt.queryForList(LOCK_QUERY_STRING);
	    Map model = new HashMap(3);
	    model.put("rows", rows);
	    return new ModelAndView("db/locks", model);
	}

	public void setDataSource(DataSource dataSource) {
	    this.dataSource = dataSource;
	}

//	public void setFeatureDao(FeatureDao featureDao) {
//	    this.featureDao = featureDao;
//	}

}