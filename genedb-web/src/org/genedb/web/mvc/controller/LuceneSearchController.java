package org.genedb.web.mvc.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class LuceneSearchController extends SimpleFormController {
	
	private String listResultsView;
    private String formInputView;
	private SequenceDao sequenceDao;
    private OrganismDao organismDao;
    
    @Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        //System.out.println("called first time : " + request.getAttribute("query"));
    	return true;
    }
    
    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
		LuceneSearch luceneSearch = (LuceneSearch) command;
		String queryString = luceneSearch.getQuery();
		Map<String, Object> model = new HashMap<String, Object>(4);
		String viewName = null;
				
		IndexReader ir = IndexReader.open("/nfs/team81/cp2/Desktop/lucene/index/gff/");
		Collection<String> c = ir.getFieldNames(IndexReader.FieldOption.INDEXED);
		List<String> fields = new ArrayList<String>();
		for (String object : c) {
			fields.add(object);
		}
		if (queryString == "Please enter search text ..."){
			List <String> err = new ArrayList <String> ();
			err.add("No search String found");
        	err.add("please use the form below to search again");
        	model.put("status", err);
        	model.put("fields", fields);
        	model.put("luceneSearch", luceneSearch);
        	viewName = formInputView;
        	return new ModelAndView(viewName,model);
		} else if (queryString == null){
			viewName = formInputView;
			model.put("fields", fields);
			model.put("luceneSearch", luceneSearch);
        	return new ModelAndView(viewName,model);
		}
		
		Query query = null;
		 
		IndexSearcher searcher = null;
		Hits hits = null;
		
		searcher = new IndexSearcher(ir);
		Analyzer analyzer = new StopAnalyzer();
		String field = luceneSearch.getField();
		String searchFields[] = new String[fields.size()];
		QueryParser qp = null;
		if ("ALL".equals(field)){
			System.out.println("searching all fields ...");
			for(int i=0; i<fields.size();i++){
				searchFields[i] = fields.get(i);
			}
			qp = new MultiFieldQueryParser(searchFields,analyzer);
		} else {
			qp = new QueryParser(field,analyzer);
		}
		String searchString = luceneSearch.getQuery();
		if(searchString.matches("\\d+")) {
			
			StringBuffer s = new StringBuffer(11);
			int length = 11 - searchString.length();
			s.append("0", 0, length);
			s.append(searchString);
			searchString = s.toString();
		}
		query = qp.parse(searchString+"*");
		
		hits = searcher.search(query);
		if (hits.length() == 0) {
			List <String> err = new ArrayList <String> ();
			err.add("Your search string did not return any results");
        	err.add("Please try again ...");
        	model.put("status", err);
        	model.put("fields", fields);
        	model.put("luceneSearch", luceneSearch);
        	viewName = formInputView;
        	return new ModelAndView(viewName,model);
		}
        
		List<SearchHit> results = new ArrayList<SearchHit>();
		
		for (int i=0;i<hits.length();i++) {
		    Document doc = hits.doc(i);
		    SearchHit sh = new SearchHit();
		    sh.setTitle(doc.get("ID"));
		    sh.setUrl(doc.get("url"));
		    sh.setChr(doc.get("chr"));
		    sh.setStart(doc.get("start"));
		    sh.setStop(doc.get("stop"));
		    sh.setStrand(doc.get("strand"));
		    results.add(sh);
		}
		model.put("results", results);
		viewName = listResultsView;
		return new ModelAndView(viewName,model);
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
	public OrganismDao getOrganismDao() {
		return organismDao;
	}
	public void setOrganismDao(OrganismDao organismDao) {
		this.organismDao = organismDao;
	}
	public SequenceDao getSequenceDao() {
		return sequenceDao;
	}
	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
    
    
}
