package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class LuceneDao {
	
	private static final Logger log = Logger.getLogger(LuceneDao.class);
	
	private static final String PROP_LUCENE_INDEX_DIRECTORY = "lucene.indexDirectory";
	
	private static final ResourceBundle projectProperties = ResourceBundle.getBundle("project");
	private static final String luceneIndexDirectory = projectProperties.getString(PROP_LUCENE_INDEX_DIRECTORY);
	
	/**
	 * Open the named Lucene index from the default location.
	 * (The default location is specifed by lucene.indexDirectory in project.properties.)
	 * 
	 * @param indexName The name of the index
	 * @return
	 * @throws IOException
	 */
	public IndexReader openIndex(String indexName) throws IOException {
		String indexDir = String.format("%s/%s", luceneIndexDirectory, indexName);
		return IndexReader.open(indexDir);
	}
	
	/**
	 * Perform a Lucene search
	 * @param ir           A Lucene index, as returned by #openIndex
	 * @param analyzer     An analyzer, used to parse the query
	 * @param defaultField The name of the field to use as the default for the query
	 * @param queryString  The actual query
	 * @return
	 */
	public Hits search(IndexReader ir, Analyzer analyzer, String defaultField, String queryString) {
		Query query = null;
		IndexSearcher searcher = new IndexSearcher(ir);
		log.debug("searcher is -> " + searcher.toString());
		QueryParser qp = new QueryParser(defaultField, analyzer);
		
		try {
			query = qp.parse(queryString);
			log.debug("query is -> " + query.toString());
			return searcher.search(query);
		} catch (ParseException e) {
			throw new RuntimeException(String.format("Lucene failed to parse query '%s'", queryString), e);
		} catch (IOException e) {
			throw new RuntimeException(String.format("I/O error during Lucene query '%s'", queryString), e);
		}
	}
}
