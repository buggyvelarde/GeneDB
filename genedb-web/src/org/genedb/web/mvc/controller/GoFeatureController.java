package org.genedb.web.mvc.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class GoFeatureController extends SimpleFormController{

    private static final Logger logger = Logger.getLogger(GoFeatureController.class);
    private String listResultsView;
    private String formInputView;
    private SequenceDao sequenceDao;

    @Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        GoLookup gl = (GoLookup) command;
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        if(gl.getLookup() == "" || gl.getLookup() == null || !(gl.getLookup().contains(":"))) {
            List <String> err = new ArrayList <String> ();
            logger.info("Look up is null");
            err.add("No search String found or the search string was not in the format 'GO:somenumber'");
            err.add("please use the form below to search again");
            model.put("status", err);
            model.put("goLookup", gl);
            viewName = formInputView;
            return new ModelAndView(viewName,model);
        }
        List<List<?>> data;
        List<Feature> results;
        String goName = new String();
        data = sequenceDao.getFeatureByGO(gl.getLookup());
        results = (List<Feature>) data.get(0);
        if(data.get(2).size() != 0) {
            goName = ((CvTerm)data.get(2).get(0)).getName();
        }

        if (results.size()== 0 ) {
            logger.info("result is null");
            List <String> err = new ArrayList <String> ();
            err.add("No Results found");
            err.add("please use the form below to search again");
            model.put("status", err);
            model.put("goLookup", gl);
            viewName = formInputView;
            return new ModelAndView(viewName,model);
            // TODO Fail page
        }
        // Go to list results page features/GO.jsp
        model.put("results", results);
        viewName = listResultsView;
        model.put("termName", goName);
        model.put("goNumber", gl.getLookup());
        File tmpDir = new File(getServletContext().getRealPath("/GViewer/data"));
        String length = webUtils.buildGViewerXMLFiles(results, tmpDir);
        model.put("length", length);


        return new ModelAndView(viewName,model);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public String getFormInputView() {
        return formInputView;
    }

    public void setFormInputView(String formInputView) {
        this.formInputView = formInputView;
    }

    public String getListResultsView() {
        return listResultsView;
    }

    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    private GeneDBWebUtils webUtils;
    public void setWebUtils(GeneDBWebUtils webUtils) {
        this.webUtils = webUtils;
    }

}
