package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.spring.web.servlet.view.JsonView;

import org.genedb.db.dao.SequenceDao;
import org.genedb.query.Result;
import org.genedb.query.SimpleListResult;
import org.genedb.query.history.History;
import org.genedb.query.history.SimpleHistory;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;
import org.genedb.web.mvc.history.commandline.HistoryParser;
import org.genedb.web.mvc.history.commandline.ParseException;

import org.gmod.schema.mapped.Feature;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 * 
 * @author Adrian Tivey
 */
@Controller
public class HistoryController extends MultiActionController implements InitializingBean {

    private SequenceDao sequenceDao;
    private FileCheckingInternalResourceViewResolver viewChecker;
    private HistoryManagerFactory historyManagerFactory;
    private String historyView;
    private JsonView jsonView;
    private String downloadView;
    private String editView;
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
    private static final int SESSION_FAILED_ERROR_CODE = 500;
    private static final String GENEDB_HISTORY = "_GeneDB_History_List";
    String compile = "Organism:([\\w\\W]+?);;Category:([\\w\\W]+?);;Term:([\\w\\W]*)";
    Pattern pattern = Pattern.compile(compile);
    
    public void setViewChecker(FileCheckingInternalResourceViewResolver viewChecker) {
        this.viewChecker = viewChecker;
    }

    public void afterPropertiesSet() throws Exception {
        // Deliberately empty
    }

    /**
     * Simple redirection to a JSP that does an AJAX-y request to viewData
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    public ModelAndView View(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ModelAndView("history/noSession");
        }
        
        return new ModelAndView(historyView);
    }
    
    
    public ModelAndView viewData(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            // No session
            response.setStatus(SESSION_FAILED_ERROR_CODE);
            return null;
        }
        
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        JSONArray items = JSONArray.fromObject(historyManager.getHistoryItems());
        model.put("items",items);

        return new ModelAndView(jsonView, model);
    }
    
    
    public ModelAndView Download(HttpServletRequest request,HttpServletResponse response) {
        String history = null;
        try {
            history = ServletRequestUtils.getRequiredStringParameter(request, "history");
        } catch (ServletRequestBindingException exp) {
            // No history item chosen - redirect to view history
            // TODO Flash message
        }
        
        HttpSession session = request.getSession(false);
        // TODO Session may be null

        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
        String formalHistoryName = historyManager.getFormalName(history);
        
        if (formalHistoryName == null) {
            // No history item chosen - redirect to view history
            // TODO Flash message
        }

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("history", formalHistoryName);
        return new ModelAndView(downloadView,model);
    }
    
    
    public ModelAndView EditHistory(HttpServletRequest request,HttpServletResponse response) {
        String history = ServletRequestUtils.getStringParameter(request, "history","0");
        String remove = ServletRequestUtils.getStringParameter(request, "remove","false");
        
        if(history == "0") {
            //be.reject("no.download.history");
            return new ModelAndView(editView);
        }
        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
        if(remove.equals("true")) {
            logger.info("history is " + remove);
            historyManager.removeItem(Integer.parseInt(history)-1, historyManager.getVersion());
            return new ModelAndView("redirect:/History/View");
        }
        
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("history", history);
        int item = Integer.parseInt(history);
        HistoryItem historyItem = historyManager.getHistoryItems().get(item-1);
        String internalName = historyItem.getInternalName();
        

        Matcher matcher = pattern.matcher(internalName);
        
        String organism = null;
        String category = null;
        String term = null;
        
        while(matcher.find()) {
            organism = matcher.group(1);
            category = matcher.group(2);
            term = matcher.group(3);
        }
        
        model.put("historyName", historyItem.getName());
        model.put("organism", organism);
        model.put("category", category);
        model.put("term", term);
        
        return new ModelAndView(editView,model);
    }
    
    

    
    
    public ModelAndView EditName(HttpServletRequest request,HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
        String history = ServletRequestUtils.getStringParameter(request, "history","0");
        String newName = ServletRequestUtils.getStringParameter(request, "value","");
        
        int count = Integer.parseInt(history) - 1;
        
        List<HistoryItem> historyItems = historyManager.getHistoryItems();
        HistoryItem changedItem = historyItems.get(count);
        boolean exists = false;
        
        for (HistoryItem historyItem : historyItems) {
            if(historyItem.getName().equals(newName) && 
                    historyItem.getHistoryType().equals(changedItem.getHistoryType())) {
                exists = true;
            }
        }
        
        if(!exists) {
            changedItem.setName(newName);
        } else {
            try {
                response.sendError(511);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public ModelAndView AddItem(HttpServletRequest request,HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            // No session
            String secondTry = ServletRequestUtils.getStringParameter(request, "sessionTest",
                "false");
            if ("true".equals(secondTry)) {
                // TODO Maybe use built in error handling
                return new ModelAndView("history/noSession");
            }
            // Try and create session and return here
            session = request.getSession(true);
            return new ModelAndView("redirect:" + "/History/View?sessionTest=true");
        }
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
        
        String history = ServletRequestUtils.getStringParameter(request, "history","0");
        String id = ServletRequestUtils.getStringParameter(request, "ids","");
        String type = ServletRequestUtils.getStringParameter(request, "type","");
        
        ArrayList<String> ids = new ArrayList<String>();
        String list[] = id.split(",");
        for (String s : list) {
            ids.add(s);
        }
        HistoryItem item = historyManager.getHistoryItems().get(Integer.parseInt(history)-1);
        
        if(type.equals("MODIFY")) {
            item.setIds(ids);
            item.setHistoryType(HistoryType.MANUAL);
        } else {
            String name = item.getName();
            String time = sdf.format(Calendar.getInstance().getTime());
            name = String.format("%s Modified %s", name,time);
            historyManager.addHistoryItem(name,HistoryType.MANUAL,ids);
        }
        return new ModelAndView("redirect:/History/View");
    }

    @SuppressWarnings("unchecked")
    public ModelAndView Test(HttpServletRequest request, HttpServletResponse response) {
        JSONArray array = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("index", 1);
        // obj.put("name", "Query 1");
        // obj.put("type", "query 1");
        obj.put("noresults", 378);
        // obj.put("tools", "orthologs");
        // obj.put("download",
        // "http://localhost:8080/genedb-web/DownloadFeatures?historyItem=1");
        array.add(obj);
        JSONObject obj1 = new JSONObject();
        obj1.put("total", 1);
        obj1.put("queries", array);
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        out.print(obj1);
        out.close();
        return new ModelAndView("history/historyIndex", obj1);
    }

    // public ModelAndView RenameItem(HttpServletRequest request,
    // HttpServletResponse response) {
    // // String name = ServletRequestUtils.getStringParameter(request, "name",
    // NO_VALUE_SUPPLIED);
    // // if (name.equals(NO_VALUE_SUPPLIED)) {
    //          
    // // }
    // // Feature feat = featureDao.findByUniqueName(name);
    // Map model = new HashMap(3);
    // // model.put("feature", feat);
    // String viewName = "features/generic";
    // // String type = feat.getCvTerm().getName();
    // // TODO
    // // Check if features/type is known about
    // // otherwise go to features/generic
    // if (type != null && type.equals("gene")) {
    // viewName = "features/gene";
    // Feature mRNA = null;
    // Set<FeatureRelationship> frs = feat.getFeatureRelationshipsForObjectId();
    // for (FeatureRelationship fr : frs) {
    // mRNA = fr.getFeatureBySubjectId();
    // break;
    // }
    // Feature polypeptide = null;
    // Set<FeatureRelationship> frs2 =
    // mRNA.getFeatureRelationshipsForObjectId();
    // for (FeatureRelationship fr : frs2) {
    // Feature f = fr.getFeatureBySubjectId();
    // if ("polypeptide".equals(f.getCvTerm().getName())) {
    // polypeptide = f;
    // }
    // }
    // model.put("polypeptide", polypeptide);
    // //System.err.println("The value of pp is '"+polypeptide+"'");
    // }
    // return new ModelAndView(viewName, model);
    // }
    //
    // public ModelAndView FindCvByName(HttpServletRequest request,
    // HttpServletResponse response) {
    // String name = ServletRequestUtils.getStringParameter(request, "name",
    // "%");
    // List cvs = cvDao.findByName(name);
    // Map model = new HashMap();
    // String viewName = "db/listCv";
    // if (cvs.size()==1) {
    // viewName = "db/cv";
    // model.put("cv", cvs.get(0));
    // } else {
    // model.put("cvs", cvs);
    // }
    // return new ModelAndView(viewName, model);
    // }
    //  
    //
    // public ModelAndView CvTermByCvName(HttpServletRequest request,
    // HttpServletResponse response) {
    // String cvName = ServletRequestUtils.getStringParameter(request, "cvName",
    // NO_VALUE_SUPPLIED);
    // String cvTermName = ServletRequestUtils.getStringParameter(request,
    // "cvTermName", "*");
    // System.err.println("cvName="+cvName+" : cvTermName="+cvTermName);
    // cvTermName = cvTermName.replace('*', '%');
    // List cvs = cvDao.findByName(cvName);
    // Cv cv = (Cv) cvs.get(0);
    // System.err.println("cv="+cv);
    // List cvTerms = cvTermDao.findByNameInCv(cvTermName, cv);
    // String viewName = "db/listCvTerms";
    // Map model = new HashMap();
    //      
    // if (cvTerms.size()==1) {
    // viewName = "db/cvTerm";
    // model.put("cvTerm", cvTerms.get(0));
    // } else {
    // model.put("cvTerms", cvTerms);
    // }
    // System.err.println("viewName is '"+viewName+"' and cvTerms length is
    // "+cvTerms.size());
    // return new ModelAndView(viewName, model);
    // }

    // public ModelAndView PublicationById(HttpServletRequest request,
    // HttpServletResponse response) {
    // int id = ServletRequestUtils.getIntParameter(request, "id", -1);
    // if (id == -1) {
    //          
    // }
    // Pub pub = pubHome.findById(id);
    // Map model = new HashMap(3);
    // model.put("pub", pub);
    // return new ModelAndView("db/pub", model);
    // }

    /**
     * Custom handler for examples
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    /*
     * public ModelAndView BooleanQuery(HttpServletRequest request,
     * HttpServletResponse response) { List<String> answers = new ArrayList<String>();
     * if (!WebUtils.extractTaxonNodesFromRequest(request, answers, true,
     * false)) { return new ModelAndView("chooseTaxonContextView"); }
     * 
     * QueryForm qf = parseQueryForm(0, request); NumberedQueryI q =
     * qf.getNumberedQueryI(); if (q == null) { q = new QueryPlaceHolder();
     * qf.setNumberedQuery(q); }
     * 
     * boolean submitPressed = false; if
     * ("Run".equals(request.getParameter("runquery"))) { if (q.isComplete()) {
     * return new ModelAndView("resultListView"); } // query not complete - fall
     * back to query page } else {
     *  // Check for set expansion... for (Object o :
     * request.getParameterMap().keySet()) { String key = (String) o; if
     * (key.startsWith("bop.")) { String[] parts = key.split("\\."); BooleanOp
     * op = BooleanOp.valueOf(parts[1]); int q1 = Integer.parseInt(parts[2]); q =
     * replaceNode(q, q1, op); QueryTreeWalker qtw = new
     * QueryTreeWalker((NumberedQueryI)q, 0); qtw.go(); break; } } // Now let's
     * see if an expansion was requested }
     * 
     * Map model = new HashMap(); model.put(WebConstants.QUERY_FORM, qf);
     * model.put(WebConstants.TAX_ID, answers.get(0)); return new
     * ModelAndView("queryWorking", WebConstants.MODEL_MAP, model ); }
     */
    /*
     * private NumberedQueryI replaceNode(NumberedQueryI q, int q1, BooleanOp
     * op) { if (q1 == 0) { return new BooleanQuery(op, q, new
     * QueryPlaceHolder()); } recurseTree(q, q1, op); return q; }
     */
    /*
     * private boolean recurseTree(BasicQueryI q, int q1, BooleanOp op) { //
     * Looking for parent... if (!(q instanceof BooleanQuery)) { return false; }
     * BooleanQuery bool = (BooleanQuery) q;
     * 
     * if (bool.getFirstQuery().getIndex()==q1) { BasicQueryI node =
     * bool.getFirstQuery(); BooleanQuery newNode = new BooleanQuery(op, node,
     * new QueryPlaceHolder()); bool.setFirstQuery(newNode); return true; }
     * 
     * if (bool.getSecondQuery().getIndex()==q1) { BasicQueryI node =
     * bool.getSecondQuery(); BooleanQuery newNode = new BooleanQuery(op, node,
     * new QueryPlaceHolder()); bool.setSecondQuery(newNode); return true; }
     * 
     * if (recurseTree(bool.getFirstQuery(), q1, op)) { return true; } return
     * recurseTree(bool.getSecondQuery(), q1, op);
     *  }
     */
    /**
     * @param index
     * @param request
     * @return
     */
    /*
     * private QueryForm parseQueryForm(int index, HttpServletRequest request) {
     * QueryForm qf = new QueryForm(); qf.setNumberedQuery(parseQuery(index,
     * request)); return qf;
     *  }
     */
    /**
     * @param index
     * @param request
     * @return
     */
    /*
     * private NumberedQueryI parseQuery(int index, HttpServletRequest request) {
     * String value = request.getParameter("node."+index); if (value == null) {
     * return null; } if (value.startsWith("bool.")) { String[] parts =
     * value.split("\\."); BooleanOp op = BooleanOp.valueOf(parts[1]); int q1 =
     * Integer.parseInt(parts[2]); BasicQueryI query1 = parseQuery(q1, request);
     * int q2 = Integer.parseInt(parts[3]); BasicQueryI query2 = parseQuery(q2,
     * request); NumberedQueryI q = new BooleanQuery(op, query1, query2);
     * q.setIndex(index); return q; } //String name =
     * request.getParameter("question."+index); QueryPlaceHolder qph = new
     * QueryPlaceHolder(); if (!"none".equals(value)) { qph.setName(value); }
     * qph.setIndex(index);
     * 
     * return qph;
     *  }
     */
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }

    // public void setPubHome(PubHome pubHome) {
    // this.pubHome = pubHome;
    // }

    public ModelAndView ParseCommand(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            // No session
            String secondTry = ServletRequestUtils.getStringParameter(request, "sessionTest",
                "false");
            if ("true".equals(secondTry)) {
                // TODO Maybe use built in error handling
                return new ModelAndView("history/noSession");
            }
            // Try and create session and return here
            session = request.getSession(true);
            return new ModelAndView("redirect:" + "/History/View?sessionTest=true");
        }
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        String command = null;
        HistoryParser historyParser = new HistoryParser(new StringReader(command));
        historyParser.setHistoryManager(historyManager);

        HistoryItem historyItem = null;
        try {
            historyItem = historyParser.Start();
        } catch (NumberFormatException exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        } catch (ParseException exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }

        //historyManager.addHistoryItems("merged", historyItem.getIds());

        return new ModelAndView("redirect:/History/View");
    }

    public String getHistoryView() {
        return historyView;
    }

    public void setHistoryView(String historyView) {
        this.historyView = historyView;
    }

    public JsonView getJsonView() {
        return jsonView;
    }

    public void setJsonView(JsonView jsonView) {
        this.jsonView = jsonView;
    }

    public String getDownloadView() {
        return downloadView;
    }

    public void setDownloadView(String downloadView) {
        this.downloadView = downloadView;
    }

    public void setEditView(String editView) {
        this.editView = editView;
    }

}