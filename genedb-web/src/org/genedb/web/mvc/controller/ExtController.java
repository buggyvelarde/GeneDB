package org.genedb.web.mvc.controller;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureRelationship;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class ExtController extends TaxonNodeBindingFormController {
	
	private static final Logger logger = Logger.getLogger(ExtController.class);
	private SequenceDao sequenceDao;
	private HistoryManagerFactory historyManagerFactory;
	
	protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
		
		ExtBean eb = (ExtBean)command;
		
		logger.info("History item number " + eb.getHistory());
		int start = Integer.parseInt(request.getParameter("start"));
		int limit = Integer.parseInt(request.getParameter("limit"));
		
		HistoryManager historyManager = historyManagerFactory.getHistoryManager(request.getSession());
		List<HistoryItem> historyItems = historyManager.getHistoryItems();
		HistoryItem historyItem = historyItems.get(eb.getHistory());
		int total = historyItem.getIds().size();
		int end = start + limit;
		if (end > total) {
			end = total;
		}
		
		List<String> ids = historyItem.getIds().subList(start, end);
		List<Feature> features = sequenceDao.getFeaturesByUniqueNames(ids);
		JSONArray array = new JSONArray();
		for (Feature feature : features) {
			JSONObject obj = new JSONObject();
    		obj.put("id", feature.getFeatureId());
    		obj.put("organism", feature.getOrganism().getCommonName());
    		obj.put("type", feature.getCvTerm().getName());
    		obj.put("name", feature.getUniqueName());
    		obj.put("pname", feature.getDisplayName());
    		String product = "";
    		Iterator<FeatureRelationship> iter = feature.getFeatureRelationshipsForObjectId().iterator();
    		while(iter.hasNext()) {
    			FeatureRelationship fr = iter.next();
    			Iterator<FeatureRelationship> frs = fr.getFeatureBySubjectId().getFeatureRelationshipsForObjectId().iterator();
    			while(frs.hasNext()) {
    				FeatureRelationship frel = frs.next();
    				//logger.info("Type = " + frel.getFeatureBySubjectId().getCvTerm().getName());
    				if(frel.getFeatureBySubjectId().getCvTerm().getName().equals("polypeptide")) {
    					Feature f = frel.getFeatureBySubjectId();
    					//logger.info("Polypeptide is " + f.getUniqueName());
    					Iterator<FeatureCvTerm> fcts = f.getFeatureCvTerms().iterator();
    					while(fcts.hasNext()) {
    						FeatureCvTerm fct = fcts.next();
    						if(fct.getCvTerm().getCv().getName().equals("genedb_products")) {
    							product = fct.getCvTerm().getName();
    						}
    					}
    				}
    			}
    		}
    		obj.put("product",product );
    		
    		Iterator<FeatureLoc> flocs = feature.getFeatureLocsForFeatureId().iterator();
    		while(flocs.hasNext()) {
    			FeatureLoc floc = flocs.next();
    			int min = floc.getFmin();
        		int max = floc.getFmax();
        		obj.put("location", min + "-" + max);
        		obj.put("chromosome",floc.getFeatureBySrcFeatureId().getUniqueName());
        		break;
    		}
    		
    		array.add(obj);
		}
		JSONObject obj = new JSONObject();
    	obj.put("total", total);
    	obj.put("features", array);
    	PrintWriter out = response.getWriter();
    	out.print(obj);
    	out.close();

		return null;
	}
	
	public HistoryManagerFactory getHistoryManagerFactory() {
		return historyManagerFactory;
	}

	public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
		this.historyManagerFactory = historyManagerFactory;
	}

	public SequenceDao getSequenceDao() {
		return sequenceDao;
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
}

class ExtBean {
	private int history;

	public int getHistory() {
		return history;
	}

	public void setHistory(int history) {
		this.history = history;
	}
}