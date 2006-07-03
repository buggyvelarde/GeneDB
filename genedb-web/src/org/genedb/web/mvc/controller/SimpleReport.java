package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.CvTermDao;
import org.genedb.db.dao.FeatureDao;
import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.Pub;
import org.genedb.query.BasicQueryI;
import org.genedb.query.NumberedQueryI;
import org.genedb.query.QueryPlaceHolder;
import org.genedb.query.bool.BooleanOp;
import org.genedb.query.bool.BooleanQuery;
import org.genedb.web.tags.bool.QueryTreeWalker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import ca.ualberta.stothard.cgview.Feature;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
public class SimpleReport extends MultiActionController implements InitializingBean {
    
    	private FeatureDao featureDao;
    	private PubHome pubHome;
    	private FileCheckingInternalResourceViewResolver viewChecker;
	
	public void setViewChecker(FileCheckingInternalResourceViewResolver viewChecker) {
	    this.viewChecker = viewChecker;
	}

	public void afterPropertiesSet() throws Exception {
//		if (clinic == null) {
//			throw new ApplicationContextException("Must set clinic bean property on " + getClass());
//		}
	}

	// handlers

	/**
	 * Custom handler for gene test
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView simpleQueryHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("featureView");
	}
	
	/**
	 * Custom handler for MOD common URL
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView booleanQueryHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("commonURLView");
	}
	
	
	/**
	 * Custom handler for examples
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView examplesHandler(HttpServletRequest request, HttpServletResponse response) {
	    return new ModelAndView("examplesView");
	}

	
	public ModelAndView FeatureById(HttpServletRequest request, HttpServletResponse response) {
	    int id = ServletRequestUtils.getIntParameter(request, "id", -1);
	    if (id == -1) {
		    
	    }
	    org.genedb.db.jpa.Feature feat = featureDao.findById(id);
	    Map model = new HashMap(3);
	    model.put("feature", feat);		
	    String viewName = "features/gene";
	    String type = feat.getCvTerm().getName();
	    // TODO
	    // Check if features/type is known about
	    // otherwise go to features/generic
	    viewName = "features/generic";
	    return new ModelAndView(viewName, model);
	}
	
	private static final String NO_VALUE_SUPPLIED = "_NO_VALUE_SUPPLIED";
	
	public ModelAndView DummyGeneFeature(HttpServletRequest request, HttpServletResponse response) {
		org.genedb.db.jpa.Feature feat = new org.genedb.db.jpa.Feature();
	    feat.setName("dummy_name");
	    feat.setUniquename("dummy_id");
	    CvTerm cvTerm = new CvTerm();
	    cvTerm.setName("gene");
	    feat.setCvTerm(cvTerm);
	    Map model = new HashMap(3);
	    model.put("feature", feat);		
	    String viewName = "features/gene";
	    return new ModelAndView(viewName, model);
	}
	
	public ModelAndView ByRegion(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    String name = ServletRequestUtils.getStringParameter(request, "name", NO_VALUE_SUPPLIED);
	    if (name.equals(NO_VALUE_SUPPLIED)) {
		    
	    }
        RegionCommand bean = new RegionCommand();
        try {
            bind(request, bean);
        } catch (Exception exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }
        // Check sequence is valid in org
	    org.genedb.db.jpa.Feature feat = featureDao.findByUniqueName(bean.getName());
	    Map model = new HashMap(3);
	    model.put("feature", feat);		
	    String viewName = "features/gene";
	    String type = feat.getCvTerm().getName();
        
        Writer w = null; // TODO
        EmblUtils.exportEmbl(w, feat, bean.getMin(), bean.getMax(), false, true, bean.isTruncateEndFeatures());
	    // TODO
	    // Check if features/type is known about
	    // otherwise go to features/generic
	    viewName = "features/generic";
	    return new ModelAndView(viewName, model);
	}
	
	CvDao cvDao;
	CvTermDao cvTermDao;
	
	public void setCvTermDao(CvTermDao cvTermDao) {
	    this.cvTermDao = cvTermDao;
	}

	public void setCvDao(CvDao cvDao) {
	    this.cvDao = cvDao;
	}

	public ModelAndView FindCvByName(HttpServletRequest request, HttpServletResponse response) {
	    String name = ServletRequestUtils.getStringParameter(request, "name", "%");
	    List cvs = cvDao.findByName(name);
	    Map model = new HashMap();
	    String viewName = "db/listCv";
	    if (cvs.size()==1) {
		viewName = "db/cv";
		model.put("cv", cvs.get(0));
	    } else {
		model.put("cvs", cvs);
	    }
	    return new ModelAndView(viewName, model);
	}
	

	public ModelAndView CvTermByCvName(HttpServletRequest request, HttpServletResponse response) {
	    String cvName = ServletRequestUtils.getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);
	    String cvTermName = ServletRequestUtils.getStringParameter(request, "cvTermName", "*");
	    System.err.println("cvName="+cvName+"   :   cvTermName="+cvTermName);
	    cvTermName = cvTermName.replace('*', '%');
	    List cvs = cvDao.findByName(cvName);
	    Cv cv = (Cv) cvs.get(0);
	    System.err.println("cv="+cv);
	    List cvTerms = cvTermDao.findByNameInCv(cvTermName, cv);
	    String viewName = "db/listCvTerms";
	    Map model = new HashMap();
	    
	    if (cvTerms.size()==1) {
		viewName = "db/cvTerm";
		model.put("cvTerm", cvTerms.get(0));
	    } else {
		model.put("cvTerms", cvTerms);
	    }
	    System.err.println("viewName is '"+viewName+"' and cvTerms length is "+cvTerms.size());
	    return new ModelAndView(viewName, model);
	}
	
	public ModelAndView PublicationById(HttpServletRequest request, HttpServletResponse response) {
	    int id = ServletRequestUtils.getIntParameter(request, "id", -1);
	    if (id == -1) {
		    
	    }
	    Pub pub = pubHome.findById(id);
	    Map model = new HashMap(3);
	    model.put("pub", pub);
	    return new ModelAndView("db/pub", model);
	}
	
	
	/**
	 * Custom handler for examples
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView BooleanQuery(HttpServletRequest request, HttpServletResponse response) {
		List<String> answers = new ArrayList<String>();
		if (!WebUtils.extractTaxonOrOrganism(request, true, false, answers)) {
			return new ModelAndView("chooseTaxonContextView");
		}
		
		QueryForm qf = parseQueryForm(0, request);
		NumberedQueryI q = qf.getNumberedQueryI();
		if (q == null) {
			q = new QueryPlaceHolder();
			qf.setNumberedQuery(q);
		}

		boolean submitPressed = false;
		if ("Run".equals(request.getParameter("runquery"))) {
		    if (q.isComplete()) {
			return new ModelAndView("resultListView");
		    }
		    // query not complete - fall back to query page
		} else {
		
		    // Check for set expansion...
		    for (Object o : request.getParameterMap().keySet()) {
			String key = (String) o;
			if (key.startsWith("bop.")) {
			    String[] parts = key.split("\\.");
			    BooleanOp op = BooleanOp.valueOf(parts[1]);
			    int q1 = Integer.parseInt(parts[2]);
			    q = replaceNode(q, q1, op);
			    QueryTreeWalker qtw = new QueryTreeWalker((NumberedQueryI)q, 0);
			    qtw.go();
			    break;
			}
		    }
		    // Now let's see if an expansion was requested
		}
			
		Map model = new HashMap();
		model.put(WebConstants.QUERY_FORM, qf);
		model.put(WebConstants.TAX_ID, answers.get(0));
		return new ModelAndView("queryWorking", WebConstants.MODEL_MAP, model );
	}

	
	private NumberedQueryI replaceNode(NumberedQueryI q, int q1, BooleanOp op) {	
		if (q1 == 0) {
			return new BooleanQuery(op, q, new QueryPlaceHolder());
		}
		recurseTree(q, q1, op);
		return q;
	}
		
		
	private boolean recurseTree(BasicQueryI q, int q1, BooleanOp op) {
		// Looking for parent...
		if (!(q instanceof BooleanQuery)) {
			return false;
		}
		BooleanQuery bool = (BooleanQuery) q;
		
		if (bool.getFirstQuery().getIndex()==q1) {
			BasicQueryI node = bool.getFirstQuery();
			BooleanQuery newNode = new BooleanQuery(op, node, new QueryPlaceHolder());
			bool.setFirstQuery(newNode);
			return true;
		}
        
		if (bool.getSecondQuery().getIndex()==q1) {
		        BasicQueryI node = bool.getSecondQuery();
		        BooleanQuery newNode = new BooleanQuery(op, node, new QueryPlaceHolder());
		        bool.setSecondQuery(newNode);
		        return true;
		}
		
		if (recurseTree(bool.getFirstQuery(), q1, op)) {
				return true;
		}
		return recurseTree(bool.getSecondQuery(), q1, op);
		
	}


	/**
	 * @param index
	 * @param request
	 * @return
     */
	private QueryForm parseQueryForm(int index, HttpServletRequest request) {
		QueryForm qf = new QueryForm();
		qf.setNumberedQuery(parseQuery(index, request));
		return qf;
		
	}
	
	/**
	 * @param index
	 * @param request
	 * @return
	 */
	private NumberedQueryI parseQuery(int index, HttpServletRequest request) {
		String value = request.getParameter("node."+index);
		if (value == null) {
			return null;
		}
		if (value.startsWith("bool.")) {
			String[] parts = value.split("\\.");
			BooleanOp op = BooleanOp.valueOf(parts[1]);
			int q1 = Integer.parseInt(parts[2]);
			BasicQueryI query1 = parseQuery(q1, request);
			int q2 = Integer.parseInt(parts[3]);
			BasicQueryI query2 = parseQuery(q2, request);
			NumberedQueryI q = new BooleanQuery(op, query1, query2);
			q.setIndex(index);
			return q;
		}
		//String name = request.getParameter("question."+index);
		QueryPlaceHolder qph = new QueryPlaceHolder();
		if (!"none".equals(value)) {
			qph.setName(value);
		}
		qph.setIndex(index);
		
		return qph;
		
	}

	public void setFeatureDao(FeatureDao featureDao) {
	    this.featureDao = featureDao;
	}

	public void setPubHome(PubHome pubHome) {
	    this.pubHome = pubHome;
	}

}