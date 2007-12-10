package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class ProductSearchController extends TaxonNodeBindingFormController{
	
	private String listResultsView;
	
	protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
		
		ProductLookupBean plb = (ProductLookupBean) command;
		String queryString = plb.getProduct();
		
		Map<String, Object> model = new HashMap<String, Object>(4);
		String viewName = null;
				
		IndexReader ir = IndexReader.open("/Users/cp2/external/lucene/index/product/");
		IndexSearcher searcher = null;
		Hits hits = null;
		
		searcher = new IndexSearcher(ir);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser qp = new QueryParser("Product",analyzer);
		Query query = qp.parse(queryString);
		hits = searcher.search(query);
		
		List<SearchHit> results = new ArrayList<SearchHit>();
		
		for (int i=0;i<hits.length();i++) {
		    Document doc = hits.doc(i);
		    SearchHit ph = new SearchHit();
		    ph.setTitle(doc.get("Product"));
		    ph.setChr(doc.get("SysID"));
		    results.add(ph);
		}
		model.put("results", results);
		viewName = listResultsView;
		return new ModelAndView(viewName,model);
	}

	public String getListResultsView() {
		return listResultsView;
	}

	public void setListResultsView(String listResultsView) {
		this.listResultsView = listResultsView;
	}
}

class ProductLookupBean {
	private String product;

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}
	
}

class ProductHit {
	
	private String product;
	
	private String sysId;
	
	private String temp;
	
	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public ProductHit(String tmp, String sysId) {
		this.temp = tmp;
		this.sysId = sysId;
	}
	
	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getSysId() {
		return sysId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}
	
}