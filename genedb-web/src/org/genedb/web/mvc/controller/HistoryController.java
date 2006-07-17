package org.genedb.web.mvc.controller;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.CvTermDao;
import org.genedb.db.dao.FeatureDao;
import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.FeatureRelationship;
import org.genedb.db.jpa.Feature;
import org.genedb.query.BasicQueryI;
import org.genedb.query.NumberedQueryI;
import org.genedb.query.QueryPlaceHolder;
import org.genedb.query.Result;
import org.genedb.query.SimpleListResult;
import org.genedb.query.bool.BooleanOp;
import org.genedb.query.bool.BooleanQuery;
import org.genedb.query.history.History;
import org.genedb.query.history.SimpleHistory;
import org.genedb.web.tags.bool.QueryTreeWalker;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
public class HistoryController extends MultiActionController implements InitializingBean {
    
    	private FeatureDao featureDao;
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
	 * Custom handler for examples
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@SuppressWarnings("unchecked")
    public ModelAndView View(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            // No session
            String secondTry = ServletRequestUtils.getStringParameter(request, "sessionTest", "false");
            if ("true".equals(secondTry)) {
                // TODO Maybe use built in error handling
                return new ModelAndView("history/noSession");
            }
            // Try and create session and return here
            session = request.getSession(true);
            return new ModelAndView("redirect:"+"/History/View?sessionTest=true");
        }
        History history = (History) session.getAttribute(GENEDB_HISTORY);
        if (history == null) {
            history = new SimpleHistory();
            session.setAttribute(GENEDB_HISTORY, history);
        }
        
        Map model = new HashMap();
        model.put("history", history);
        
	    int id = ServletRequestUtils.getIntParameter(request, "id", -1);
	    if (id == -1) {
		    
	    }

	    return new ModelAndView("history/historyIndex", model);
	}
	
	private static final String GENEDB_HISTORY = "_GeneDB_History_List";
	
	public ModelAndView AddItem(HttpServletRequest request, HttpServletResponse response) {
        Result result = new SimpleListResult();
        result.setName("testing");
        
        HttpSession session = request.getSession(false);
        String sessionTest = "";
        boolean madeSession = false;
        if (session == null) {
            madeSession = true;
            session = request.getSession(true);
        }
        History history = (History) session.getAttribute(GENEDB_HISTORY);
        if (history == null) {
            history = new SimpleHistory();
            session.setAttribute(GENEDB_HISTORY, history);
        }
        history.addResult(result);
        if (madeSession) {
            sessionTest = "?sessionTest=true";
        }
        
        return new ModelAndView("redirect:/History/View"+sessionTest);
	}
	
//	public ModelAndView RenameItem(HttpServletRequest request, HttpServletResponse response) {
////	    String name = ServletRequestUtils.getStringParameter(request, "name", NO_VALUE_SUPPLIED);
////	    if (name.equals(NO_VALUE_SUPPLIED)) {
//		    
////	    }
////	    Feature feat = featureDao.findByUniqueName(name);
//	    Map model = new HashMap(3);
////	    model.put("feature", feat);		
//	    String viewName = "features/generic";
////	    String type = feat.getCvTerm().getName();
//	    // TODO
//	    // Check if features/type is known about
//	    // otherwise go to features/generic
//        if (type != null && type.equals("gene")) {
//            viewName = "features/gene";
//            Feature mRNA = null;
//            Set<FeatureRelationship> frs = feat.getFeatureRelationshipsForObjectId(); 
//            for (FeatureRelationship fr : frs) {
//                mRNA = fr.getFeatureBySubjectId();
//                break;
//            }
//            Feature polypeptide = null;
//            Set<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForObjectId(); 
//            for (FeatureRelationship fr : frs2) {
//                Feature f = fr.getFeatureBySubjectId();
//                if ("polypeptide".equals(f.getCvTerm().getName())) {
//                    polypeptide = f;
//                }
//            }
//            model.put("polypeptide", polypeptide);
//            //System.err.println("The value of pp is '"+polypeptide+"'");
//        }
//	    return new ModelAndView(viewName, model);
//	}
//
//	public ModelAndView FindCvByName(HttpServletRequest request, HttpServletResponse response) {
//	    String name = ServletRequestUtils.getStringParameter(request, "name", "%");
//	    List cvs = cvDao.findByName(name);
//	    Map model = new HashMap();
//	    String viewName = "db/listCv";
//	    if (cvs.size()==1) {
//		viewName = "db/cv";
//		model.put("cv", cvs.get(0));
//	    } else {
//		model.put("cvs", cvs);
//	    }
//	    return new ModelAndView(viewName, model);
//	}
//	
//
//	public ModelAndView CvTermByCvName(HttpServletRequest request, HttpServletResponse response) {
//	    String cvName = ServletRequestUtils.getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);
//	    String cvTermName = ServletRequestUtils.getStringParameter(request, "cvTermName", "*");
//	    System.err.println("cvName="+cvName+"   :   cvTermName="+cvTermName);
//	    cvTermName = cvTermName.replace('*', '%');
//	    List cvs = cvDao.findByName(cvName);
//	    Cv cv = (Cv) cvs.get(0);
//	    System.err.println("cv="+cv);
//	    List cvTerms = cvTermDao.findByNameInCv(cvTermName, cv);
//	    String viewName = "db/listCvTerms";
//	    Map model = new HashMap();
//	    
//	    if (cvTerms.size()==1) {
//		viewName = "db/cvTerm";
//		model.put("cvTerm", cvTerms.get(0));
//	    } else {
//		model.put("cvTerms", cvTerms);
//	    }
//	    System.err.println("viewName is '"+viewName+"' and cvTerms length is "+cvTerms.size());
//	    return new ModelAndView(viewName, model);
//	}
	
//	public ModelAndView PublicationById(HttpServletRequest request, HttpServletResponse response) {
//	    int id = ServletRequestUtils.getIntParameter(request, "id", -1);
//	    if (id == -1) {
//		    
//	    }
//	    Pub pub = pubHome.findById(id);
//	    Map model = new HashMap(3);
//	    model.put("pub", pub);
//	    return new ModelAndView("db/pub", model);
//	}
	
	
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

//	public void setPubHome(PubHome pubHome) {
//	    this.pubHome = pubHome;
//	}

}