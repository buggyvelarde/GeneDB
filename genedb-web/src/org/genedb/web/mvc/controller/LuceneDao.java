package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class LuceneDao {
	
	public IndexReader openIndex(String indexDir) {
		try {
			return IndexReader.open(indexDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Hits search(IndexReader ir, Analyzer analyzer,List<String> fields,String queryString) {
		
		Query query = null;
		IndexSearcher searcher = new IndexSearcher(ir);
		System.err.println("searcher is -> " + searcher.toString());
		Hits hits = null;
		QueryParser qp = null;
		String searchFields[] = new String[fields.size()];
		if(fields.size() > 1) {
			for(int i=0; i<fields.size();i++){
				searchFields[i] = fields.get(i);
			}
			qp = new QueryParser(fields.get(0),analyzer);
		} else {
			qp = new QueryParser(fields.get(0),analyzer);
		}
		
		try {
			query = qp.parse(queryString);
			System.err.println("query is -> " + query.toString());
			hits = searcher.search(query);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return hits;
	}
}
