package org.genedb.web.mvc.controller;


import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.web.mvc.history.commandline.HistoryParser;
import org.genedb.web.mvc.history.commandline.ParseException;
import org.genedb.web.mvc.view.FileCheckingInternalResourceViewResolver;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/History")
public class HistoryController {

    Logger logger = Logger.getLogger(HistoryController.class);

    private SequenceDao sequenceDao;
    private FileCheckingInternalResourceViewResolver viewChecker;
    private HistoryManagerFactory historyManagerFactory;
    private String historyView;
    private String downloadView;
    private String editView;
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
    private static final int SESSION_FAILED_ERROR_CODE = 500;
    private static final String GENEDB_HISTORY = "_GeneDB_History_List";


    public void setViewChecker(FileCheckingInternalResourceViewResolver viewChecker) {
        this.viewChecker = viewChecker;
    }

    @RequestMapping(method=RequestMethod.GET, value="/{historyItem}")
    public ModelAndView editHistoryItem(HttpServletRequest request,HttpServletResponse response,
            @PathVariable("historyItem") int historyItem) {

        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("history", historyItem);
        HistoryItem item = historyManager.getHistoryItems().get(historyItem-1);
        //String internalName = item.getInternalName();

        model.put("items", item.getIds());
        model.put("historyName", item.getName());

        return new ModelAndView("history/editHistoryItem",model);
    }

    @RequestMapping(method=RequestMethod.POST, value="/{historyItem}")
    public ModelAndView deleteHistoryItems(HttpServletRequest request,HttpServletResponse response,
            @PathVariable("historyItem") int historyItem,
            @RequestParam("historyVersion") int historyVersion) {

        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("history", historyItem);
        HistoryItem item = historyManager.getHistoryItems().get(historyItem-1);
        //String internalName = item.getInternalName();

        List<Integer> hits = Lists.newArrayList();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("item")) {
                name = name.substring(4);
                logger.error("About to add '"+name+"' to hits");
                hits.add(Integer.parseInt(name)-1);
            }
        }

        for (Integer i : hits) {
            item.removeNum(i);
        }

        if (item.getNumberItems() < 1) {
            historyManager.removeItem(historyItem, historyVersion);
            return new ModelAndView("/History");
        }

        model.put("items", item.getIds());
        model.put("historyName", item.getName());

        return new ModelAndView("history/editHistoryItem",model);
    }


    @RequestMapping(method=RequestMethod.DELETE, params="historyItem")
    public ModelAndView deleteHistoryItem(HttpServletRequest request,HttpServletResponse response,
            @RequestParam("historyItem") int historyItem) {

        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
        logger.info("Removing item from history");
        historyManager.removeItem(historyItem, historyManager.getVersion());
        return new ModelAndView("redirect:/History/View");
    }


    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView listHistory(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession(true);

        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("items", historyManager.getHistoryItems());

        return new ModelAndView("history/list", model);
    }


//    public ModelAndView EditName(HttpServletRequest request,HttpServletResponse response) {
//        HttpSession session = request.getSession(false);
//        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
//        String history = ServletRequestUtils.getStringParameter(request, "history","0");
//        String newName = ServletRequestUtils.getStringParameter(request, "value","");
//
//        int count = Integer.parseInt(history) - 1;
//
//        List<HistoryItem> historyItems = historyManager.getHistoryItems();
//        HistoryItem changedItem = historyItems.get(count);
//        boolean exists = false;
//
//        for (HistoryItem historyItem : historyItems) {
//            if(historyItem.getName().equals(newName) &&
//                    historyItem.getHistoryType().equals(changedItem.getHistoryType())) {
//                exists = true;
//            }
//        }
//
//        if(!exists) {
//            changedItem.setName(newName);
//        } else {
//            try {
//                response.sendError(511);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

//    public ModelAndView AddItem(HttpServletRequest request,HttpServletResponse response) {
//        HttpSession session = request.getSession(false);
//        if (session == null) {
//            // No session
//            String secondTry = ServletRequestUtils.getStringParameter(request, "sessionTest",
//                "false");
//            if ("true".equals(secondTry)) {
//                // TODO Maybe use built in error handling
//                return new ModelAndView("history/noSession");
//            }
//            // Try and create session and return here
//            session = request.getSession(true);
//            return new ModelAndView("redirect:" + "/History/View?sessionTest=true");
//        }
//        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
//
//        String history = ServletRequestUtils.getStringParameter(request, "history","0");
//        String id = ServletRequestUtils.getStringParameter(request, "ids","");
//        String type = ServletRequestUtils.getStringParameter(request, "type","");
//
//        ArrayList<String> ids = new ArrayList<String>();
//        String list[] = id.split(",");
//        for (String s : list) {
//            ids.add(s);
//        }
//        HistoryItem item = historyManager.getHistoryItems().get(Integer.parseInt(history)-1);
//
//        if(type.equals("MODIFY")) {
//            item.setIds(ids);
//            item.setHistoryType(HistoryType.MANUAL);
//        } else {
//            String name = item.getName();
//            String time = sdf.format(Calendar.getInstance().getTime());
//            name = String.format("%s Modified %s", name,time);
//            historyManager.addHistoryItem(name,HistoryType.MANUAL,ids);
//        }
//        return new ModelAndView("redirect:/History/View");
//    }

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
