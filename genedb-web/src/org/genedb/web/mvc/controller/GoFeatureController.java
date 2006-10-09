package org.genedb.web.mvc.controller;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;

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

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        GoLookup gl = (GoLookup) command;
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        if(gl.getLookup() == "" || gl.getLookup() == null) {
        	List <String> err = new ArrayList <String> ();
        	logger.info("Look up is null");
        	err.add("No search String found");
        	err.add("please use the form below to search again");
        	model.put("status", err);
        	model.put("goLookup", gl);
        	viewName = formInputView;
        	return new ModelAndView(viewName,model);
        }
        List<List> data;
        List<Feature> results;
        List<Feature> features;
        List<FeatureLoc> featlocs = new ArrayList<FeatureLoc>();
        data = sequenceDao.getFeatureByGO(gl.getLookup());
        results = data.get(0);
        features = data.get(1);

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
 
        File tmpDir = new File(getServletContext().getRealPath("/GViewer/data"));
        OutputStream out = new FileOutputStream(tmpDir + "/sbase.xml");

        Element genome = new Element("genome");
        
        int i = 0;
        int length = 0;
        for (Feature feature : features) {
        	String chromosomeName = feature.getUniqueName();
            String chromosomeNumber = chromosomeName.substring(chromosomeName.length()-1);
        	i++;
			Element chromosome = new Element("chromosome");
			Element end = new Element("end");
			chromosome.setAttribute("index", Integer.toString(i));
			chromosome.setAttribute("number", chromosomeNumber);
			chromosome.setAttribute("length", Integer.toString(feature.getSeqLen()));
			if (feature.getSeqLen() > length) {
				length = feature.getSeqLen();
			}
			end.setText(Integer.toString(feature.getSeqLen()));

			Element band = new Element("band");
			band.setAttribute("index", "1");
			band.setAttribute("name", "1");
			Element start = new Element("start");
			start.setText("0");
			Element stain = new Element("stain");
			stain.setText("gneg");
			band.addContent(start);
			band.addContent(end);
			band.addContent(stain);
			chromosome.addContent(band);
			genome.addContent(chromosome);
		}
        model.put("length", length);
        Document doc = new Document(genome);
        XMLOutputter xmlout = new XMLOutputter();
        xmlout.setFormat(Format.getPrettyFormat());
        xmlout.output(doc, out);
        out.flush();
        
        out = new FileOutputStream(tmpDir + "/003.xml");
        genome = new Element("genome");
        
        for (Feature feature : results) {
			Element XMLfeature = new Element("feature");
			Element chromosome = new Element("chromosome");
			Element chrstart = new Element("start");
			Element chrend = new Element("end");
			Element type = new Element("type");
			Element color = new Element("colour");
			Element label = new Element("label");
			Element link = new Element("link");
			
			Collection<FeatureLoc> temp = feature.getFeatureLocsForFeatureId();
			for (FeatureLoc fl : temp) {
				String name = fl.getFeatureBySrcFeatureId().getUniqueName();
				String number = name.substring(name.length()-1);
				chromosome.setText(number);
				chrstart.setText(fl.getFmin().toString());
				chrend.setText(fl.getFmax().toString());
			}
			
			type.setText("gene");
			color.setText("ox79cc3d");
			label.setText(feature.getUniqueName());
			link.setText("url");
			
			XMLfeature.addContent(chromosome);
			XMLfeature.addContent(chrstart);
			XMLfeature.addContent(chrend);
			XMLfeature.addContent(type);
			XMLfeature.addContent(color);
			XMLfeature.addContent(label);
			XMLfeature.addContent(link);
			genome.addContent(XMLfeature);
		}
        
        doc = new Document(genome);
        xmlout = new XMLOutputter();
        xmlout.setFormat(Format.getPrettyFormat());
        xmlout.output(doc, out);
        out.flush();
        
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
