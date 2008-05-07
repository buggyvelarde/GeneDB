    package org.genedb.db.domain.luceneImpls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.services.BasicGeneService;

public class BasicGeneServiceImpl implements BasicGeneService {

    private IndexReader luceneIndex;
    private static Analyzer luceneAnalyzer = new StandardAnalyzer();
    private static Logger log = Logger.getLogger(BasicGeneService.class);

    public BasicGeneServiceImpl(IndexReader luceneIndex) {
        this.luceneIndex = luceneIndex;
    }

    /**
     * Defines a conversion from a Lucene Document to some other
     * class of object. A DocumentConverter can also function as a filter if
     * desired, by returning <code>null</code> from the convert method to
     * indicate that a particular document should be ignored.
     * 
     * @author rh11
     * 
     * @param <T> The return type of the conversion
     */
    private interface DocumentConverter<T> {
        /**
         * Convert the document to the desired form.
         * 
         * @param doc  the document to convert
         * @return     the result of the conversion, or null if this document should be ignored
         */
        T convert(Document doc);
    }
    
    /**
     * This DocumentConverter populates a BasicGene object using the Lucene
     * Document. Currently it makes a new Lucene query for every gene to pull
     * back the associated transcripts. Should this prove unacceptable, the
     * associated transcripts could be all loaded together instead.
     */
    private final DocumentConverter<BasicGene> convertToGene = new DocumentConverter<BasicGene>() {
        public BasicGene convert(Document doc) {
            BasicGene ret = new BasicGene();

            ret.setFeatureId(Integer.parseInt(doc.get("featureId")));
            ret.setSystematicId(doc.get("uniqueName"));
            ret.setName(doc.get("name"));
            ret.setOrganism(doc.get("organism.commonName"));
            ret.setFmin(Integer.parseInt(doc.get("start")));
            ret.setFmax(Integer.parseInt(doc.get("stop")));
            ret.setSynonyms(Arrays.asList(doc.get("synonym").split("\t")));
                
            return ret;
        }
    };

    /**
     * Finds all documents matching the query, and makes a list of matches. Each
     * document is converted to an object of type T using the converter. This 
     * method parses the query text, using a StandardAnalyzer, and then calls
     * findGenesWithQuery(Query, DocumentConverter<T>).
     * 
     * @param <T> The return type
     * @param defaultField The default Lucene field
     * @param queryText Text of the query
     * @param converter Result converter
     * @return
     */
    private <T> List<T> findWithQuery(String defaultField, String queryText,
            DocumentConverter<T> converter) {
        QueryParser qp = new QueryParser(defaultField, luceneAnalyzer);
        if (queryText.startsWith("*"))
            qp.setAllowLeadingWildcard(true);
        Query query;
        try {
            query = qp.parse(queryText);
        } catch (ParseException e) {
            throw new RuntimeException(String
                    .format("Failed to parse Lucene query '%s'", queryText), e);
        }
        return findWithQuery(query, converter);
    }

    /**
     * Finds all documents matching the query, and makes a list of matches. Each
     * document is converted to an object of type T using the converter.
     * 
     * @param <T> The return type
     * @param query The query object
     * @param converter Result converter
     * @return
     */
    private <T> List<T> findWithQuery(Query query, DocumentConverter<T> converter) {
        List<T> ret = new ArrayList<T>();

        IndexSearcher searcher = new IndexSearcher(luceneIndex);
        log.debug("searcher is -> " + searcher.toString());

        Hits hits;
        try {
            hits = searcher.search(query);
        } catch (IOException e) {
            throw new RuntimeException("IOException while running Lucene query", e);
        }

        @SuppressWarnings("unchecked")
        Iterator<Hit> it = hits.iterator();
        while (it.hasNext()) {
            Document doc;
            try {
                doc = it.next().getDocument();
            } catch (CorruptIndexException e) {
                throw new RuntimeException("Lucene index is corrupted!", e);
            } catch (IOException e) {
                throw new RuntimeException("IOException while fetching results of Lucene query", e);
            }
            T convertedDocument = converter.convert(doc);
            if (convertedDocument != null)
                ret.add(convertedDocument);
        }
        return ret;
    }

    private <T> T findUniqueWithQuery(String defaultField, String queryText,
            DocumentConverter<T> converter) {
        List<T> results = findWithQuery(defaultField, queryText, converter);
        int numberOfResults = results.size();
        if (numberOfResults == 0) {
            log.info(String.format("Failed to find gene matching Lucene query '%s'", queryText));
            return null;
        } else if (numberOfResults > 1) {
            log.error(String.format("Found %d genes matching query '%s'; expected only one!",
                numberOfResults, queryText));
        }
        return results.get(0);
    }

    /*
     * This method is not actually used at present; the code is here in case
     * anyone needs it in future. Ñrh11
     */
    private <T> T findUniqueWithQuery(Query query, DocumentConverter<T> converter) {
        List<T> results = findWithQuery(query, converter);
        int numberOfResults = results.size();
        if (numberOfResults == 0) {
            log.info(String.format("Failed to find gene matching Lucene query '%s'", query));
            return null;
        } else if (numberOfResults > 1) {
            log.error(String.format("Found %d genes matching query '%s'; expected only one!",
                numberOfResults, query));
        }
        return results.get(0);
    }

    public BasicGene findGeneByUniqueName(String name) {
        return findUniqueWithQuery(new TermQuery(new Term("uniqueName", name)), convertToGene);
    }

    /**
     * Warning: this method is liable to be very slow, because it results
     * in a Lucene query of the form *foo*: the problem is the initial wildcard.
     */
    public List<String> findGeneNamesByPartialName(String search) {
        return findWithQuery(new WildcardQuery(new Term("uniqueName", String.format("*%s*", search))),
            new DocumentConverter<String>() {
                public String convert(Document doc) {
                    return doc.get("uniqueName");
                }
            });
    }

    public Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax) {
        BooleanQuery query = new BooleanQuery();
//        query.add(new TermQuery(new Term("_hibernate_class", "org.gmod.schema.sequence.Gene")),
//            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("organism.commonName", organismCommonName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("chr", chromosomeUniqueName)),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("start", null, String.format("%09d", locMax), false, false),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("stop", String.format("%09d", locMin), null, false, false),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("strand", String.valueOf(strand))),
            BooleanClause.Occur.MUST);

        return findWithQuery(query, convertToGene);
    }
}
