package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class CurationController extends TaxonNodeBindingFormController {
	
private String listResultsView;
	
	private LuceneDao luceneDao;

	public LuceneDao getLuceneDao() {
		return luceneDao;
	}

	public void setLuceneDao(LuceneDao luceneDao) {
		this.luceneDao = luceneDao;
	}

	public String getListResultsView() {
		return listResultsView;
	}

	public void setListResultsView(String listResultsView) {
		this.listResultsView = listResultsView;
	}
	
	protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
		
		CurationBean cb = (CurationBean) command;
		String orgs = cb.getOrgs();
		String input = cb.getQuery();
		String field = cb.getField();
		
		Map<String,Object> model = new HashMap<String,Object>(2);
		List<CurationHits> results = new ArrayList<CurationHits>();
		String viewName = listResultsView;
		
		IndexReader ir = luceneDao.openIndex("org.gmod.schema.sequence.FeatureProp");
		String query = "";
		
		if(field.equals("ALL")) {
			query = "value:" + input + " AND feature.organism.commonName:" + orgs;
		} else {
			query = "value:" + input + " AND cvTerm.name:" + field + " AND feature.organism.commonName:" + orgs;
		}
		
		Hits hits = luceneDao.search(ir, new StandardAnalyzer(), "cvTerm.name", query);
		if (hits.length() == 0) {
			be.reject("No Result");
			return showForm(request, response, be);
			//return new ModelAndView(viewName,null);
		} else {
			for (int i=0;i<hits.length();i++) {
				Document doc = hits.doc(i);
				CurationHits ch = new CurationHits();
				ch.setCvTerm(doc.get("cvTerm.name"));
				ch.setFeature(doc.get("feature.UniqueName"));
				ch.setOrganism(doc.get("feature.organism.commonName"));
				ch.setValue(doc.get("value"));
				results.add(ch);
			}
			model.put("results", results);
		}
		return new ModelAndView(viewName,model);
	}
}

class CurationBean {
	
	private String query;
	
	private String orgs;
	
	private String field;
	
	private boolean history = false;

	public boolean isHistory() {
		return history;
	}

	public void setHistory(boolean history) {
		this.history = history;
	}

	public String getOrgs() {
		return orgs;
	}

	public void setOrgs(String orgs) {
		this.orgs = orgs;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}
