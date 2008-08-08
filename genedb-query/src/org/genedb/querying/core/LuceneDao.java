package org.genedb.querying.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class LuceneDao {

    private static final Logger logger = Logger.getLogger(LuceneDao.class);

    private String luceneIndexDirectoryName;
    
    // Effectively disable the clause limit.
    // (Wildcard and range queries are expanded into many clauses.)
    public LuceneDao() {
    	BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
    }

    /**
     * Open the named Lucene index from the default location.
     * 
     * @param indexName The name of the index
     * @return
     * @throws IOException
     */
    public IndexReader openIndex(String indexName) throws IOException {
        String indexDir = String.format("%s/%s", luceneIndexDirectoryName, indexName);
        logger.info(String.format("Opening Lucene index at '%s'", indexDir));
        return IndexReader.open(indexDir);
    }

    /**
     * Perform a Lucene search
     * 
     * @param ir A Lucene index, as returned by {@link #openIndex(String)}
     * @param analyzer An analyzer, used to parse the query
     * @param defaultField The name of the field to use as the default for the
     *                query
     * @param queryString The text of the query
     * @return The result of the search
     */
    public Hits search(IndexReader ir, Analyzer analyzer, String defaultField, String queryString) {
        Query query = null;
        QueryParser qp = new QueryParser(defaultField, analyzer);

        try {
            query = qp.parse(queryString);
            logger.debug("query is -> " + query.toString());

            return search(ir, query);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Lucene failed to parse query '%s'",
                queryString), e);
        }
    }

    /**
     * Perform a Lucene search using a prebuilt Query object.
     * 
     * @param ir A Lucene index, as returned by {@link #openIndex(String)}
     * @param query The query
     * @return The result of the search
     */
    public Hits search(IndexReader ir, Query query) {
        IndexSearcher searcher = new IndexSearcher(ir);
        logger.debug("searcher is -> " + searcher.toString());
        try {
            return searcher.search(query);
        } catch (IOException e) {
            throw new RuntimeException(String.format("I/O error during Lucene query '%s'",
                query), e);
        }
    }

	public void setLuceneIndexDirectoryName(String luceneIndexDirectoryName) {
		this.luceneIndexDirectoryName = luceneIndexDirectoryName;
	}
}
