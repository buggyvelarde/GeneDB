package org.genedb.web.mvc.controller;

import org.genedb.query.BasicQueryI;
import org.genedb.query.NumberedQueryI;
import org.genedb.query.QueryPlaceHolder;
import org.genedb.query.bool.BooleanOp;
import org.genedb.query.bool.BooleanQuery;
import org.genedb.web.tags.bool.QueryTreeWalker;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
public class ComplexQueryController extends MultiActionController implements InitializingBean {

    public void afterPropertiesSet() throws Exception {
        // Deliberately empty
    }

    // handlers

    /**
     * Custom handler for examples
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render the response
     */
    public ModelAndView BooleanQuery(HttpServletRequest request, HttpServletResponse response) {
        List<String> answers = new ArrayList<String>();
        if (!webUtils.extractTaxonNodesFromRequest(request, answers, true, false)) {
            return new ModelAndView("chooseTaxon");
        }

        QueryForm qf = parseQueryForm(0, request);
        NumberedQueryI q = qf.getNumberedQueryI();
        if (q == null) {
            q = new QueryPlaceHolder();
            qf.setNumberedQuery(q);
        }

        // boolean submitPressed = false;
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
                    QueryTreeWalker qtw = new QueryTreeWalker(q, 0);
                    qtw.go();
                    break;
                }
            }
            // Now let's see if an expansion was requested
        }

        Map<String,Object> model = new HashMap<String,Object>();
        model.put(WebConstants.QUERY_FORM, qf);
        model.put(WebConstants.TAX_ID, answers.get(0));
        return new ModelAndView("queryWorking", WebConstants.MODEL_MAP, model);
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

        if (bool.getFirstQuery().getIndex() == q1) {
            BasicQueryI node = bool.getFirstQuery();
            BooleanQuery newNode = new BooleanQuery(op, node, new QueryPlaceHolder());
            bool.setFirstQuery(newNode);
            return true;
        }

        if (bool.getSecondQuery().getIndex() == q1) {
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
        String value = request.getParameter("node." + index);
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
        // String name = request.getParameter("question."+index);
        QueryPlaceHolder qph = new QueryPlaceHolder();
        if (!"none".equals(value)) {
            qph.setName(value);
        }
        qph.setIndex(index);

        return qph;
    }

    private GeneDBWebUtils webUtils;
    public void setWebUtils(GeneDBWebUtils webutils) {
        this.webUtils = webutils;
    }
}