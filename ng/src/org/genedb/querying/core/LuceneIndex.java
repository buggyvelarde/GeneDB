package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

public class LuceneIndex {

    private static final int DEFAULT_MAX_RESULTS = 1000000;

    private int maxResults = DEFAULT_MAX_RESULTS;


    public int getMaxResults() {
        return maxResults;
    }


    public void setMaxResults(int maxResults) {
        if (maxResults <= 1 || maxResults > Integer.MAX_VALUE - 3) {
            throw new IllegalArgumentException("The maximum number of results must be a positive integer less than " + (Integer.MAX_VALUE-3)+". Beware of running out of memory well before that");
        }
        this.maxResults = maxResults;
    }


    public String getIndexName() {
        return indexName;
    }

    private static final Logger logger = Logger.getLogger(LuceneIndex.class);

    private IndexReader indexReader;

    private String indexDirectoryName;
    private String indexName;
    private Directory directory;

    // Effectively disable the clause limit.
    // (Wildcard and range queries are expanded into many clauses.)
    public LuceneIndex() {
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
    }


    /**
     * Open the named Lucene index from the default location.
     *
     * @param indexName The name of the index
     * @return
     * @throws IOException
     */
    @PostConstruct
    private void openIndex() {
        if ( ! (StringUtils.hasText(indexDirectoryName) && StringUtils.hasText(indexName)) ) {
            return;
        }
        String indexFilename = indexDirectoryName + File.separatorChar + indexName;
        logger.info(String.format("Opening Lucene index at '%s'", indexFilename));
        try {
        	// gv1 - made the reader read-only - this prevents a long (45 minute) dictionary 
        	// creation step to occur at the start of Tomcat deployment if one hasn't been created
        	// is there a reason NOT to make it read-only?
        	directory = FSDirectory.getDirectory(new File(indexFilename));
            indexReader = IndexReader.open(directory, true);

        } catch (IOException exp) {
            exp.printStackTrace(System.err);
            throw new RuntimeException(String.format("Failed to open lucene index '%s'", indexFilename), exp);
        }
    }

//    public void close() {
//        if (indexReader != null) {
//            try {
//                indexReader.flush();
//                indexReader.close();
//                indexReader = null;
//            }
//            catch (IOException exp) {
//                throw new RuntimeException(String.format("Failed to close lucene index '%s'", exp.getMessage()));
//            }
//        }
//    }

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
    public TopDocs search(Analyzer analyzer, String defaultField, String queryString) {
        Query query = null;
        QueryParser qp = new QueryParser(defaultField, analyzer);

        try {
            query = qp.parse(queryString);
            logger.debug("query is -> " + query.toString());

            return search(query);
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
    public TopDocs search(Query query) {
        return search(query, null);
    }



    /**
     * Perform a Lucene search using a prebuilt Query object.
     *
     * @param ir A Lucene index, as returned by {@link #openIndex(String)}
     * @param query The query
     * @return The result of the search
     */
    public TopDocs search(Query query, Sort sort) {
        IndexSearcher searcher = new IndexSearcher(indexReader);
        logger.debug("searcher is -> " + searcher.toString());
        try {
            if (sort == null) {
                return searcher.search(query, maxResults);
            } else {
                return searcher.search(query, null, maxResults, sort);
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("I/O error during Lucene query '%s'",
                query), e);
        }
    }

    public void setIndexDirectoryName(String indexDirectoryName) {
        this.indexDirectoryName = indexDirectoryName;
        openIndex();
    }

    public String getIndexDirectoryName() {
        return indexDirectoryName;
    }

    /**
     *  Used by SuggestQuery.
     */
    public Directory getDirectory() {
    	return directory;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
        openIndex();
    }
    
    /**
     *  Used by SuggestQuery.
     */
    public IndexReader getReader() {
    	return this.indexReader;
    }


    public Document getDocument(int docId) throws CorruptIndexException, IOException {
        return indexReader.document(docId);
    }

}
