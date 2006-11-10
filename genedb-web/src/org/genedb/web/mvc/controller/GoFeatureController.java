package org.genedb.web.mvc.controller;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureRelationship;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class GoFeatureController extends SimpleFormController{
	
	private String listResultsView;
    private String formInputView;
	private SequenceDao sequenceDao;
    private OrganismDao organismDao;

  	
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
        List<List> data;
        List<Feature> results;
        List<Feature> features;
        @SuppressWarnings("unused")
		String goName = new String();
        @SuppressWarnings("unused")
		List<FeatureLoc> featlocs = new ArrayList<FeatureLoc>();
        data = sequenceDao.getFeatureByGO(gl.getLookup());
        results = data.get(0);
        features = data.get(1);
        goName = ((CvTerm)data.get(2).get(0)).getName();
        
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
        // Go to list results page
        model.put("results", results);
        viewName = listResultsView;
        model.put("termName", goName);
        model.put("goNumber", gl.getLookup());
        File tmpDir = new File(getServletContext().getRealPath("/GViewer/data"));
        String length = WebUtils.buildGViewerXMLFiles(results, tmpDir);
        model.put("length", length);
        
        
        return new ModelAndView(viewName,model);
    }


    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
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
}
