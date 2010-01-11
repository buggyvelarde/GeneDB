package org.genedb.db.domain.luceneImpls;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Chromosome;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Gap;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.objects.TranscriptComponent;
import org.genedb.db.domain.objects.UTR;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.querying.core.LuceneIndex;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BasicGeneServiceImpl implements BasicGeneService {

    private LuceneIndex luceneIndex;
    private static Logger logger = Logger.getLogger(BasicGeneServiceImpl.class);

    public BasicGeneServiceImpl(LuceneIndex luceneIndex) {
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

    private static final SortedSet<TranscriptComponent> NO_EXONS = Collections.unmodifiableSortedSet(new TreeSet<TranscriptComponent>());

    /**
     * Convert the document to a Transcript object. If the <code>_hibernate_class</code>
     */
    private final DocumentConverter<Transcript> convertToTranscript = new DocumentConverter<Transcript>() {
        public Transcript convert(Document doc) {
            logger.debug(String.format("Transcript has class '%s'", doc.get("_hibernate_class")));

            Transcript transcript = new Transcript();
            String colourString = doc.get("colour");
            if (colourString == null || colourString.equals("null")) {
                transcript.setColourId(null);
            } else {
                transcript.setColourId(Integer.parseInt(colourString));
            }

            transcript.setUniqueName(doc.get("uniqueName"));

            transcript.setFmin(Integer.parseInt(doc.get("start")));
            transcript.setFmax(Integer.parseInt(doc.get("stop")));
            try {
                transcript.setComponents(parseLocs(doc.get("locs")));
            }
            catch (NullPointerException e) {
                logger.error(String.format("Failed to parse locs for transcript '%s' due to null",
                    doc.get("uniqueName")));
                 transcript.setComponents(NO_EXONS);
            }
            catch (Exception e) {
                logger.error(String.format("Failed to parse locs for transcript '%s'",
                    doc.get("uniqueName")), e);
                 transcript.setComponents(NO_EXONS);
            }
            String productsTabSeparated = doc.get("product");
            if (productsTabSeparated != null) {
                transcript.setProducts(Arrays.asList(productsTabSeparated.split("\t")));
            }

            return transcript;
        }
    };

    /**
     * This DocumentConverter populates a BasicGene object using the Lucene
     * Document. Currently it makes a new Lucene query for every gene to pull
     * back the associated transcripts. Should this prove unacceptable, the
     * associated transcripts could instead all be loaded at once.
     *
     * If the <code>_hibernate_class</code> is neither equal to <code>org.gmod.schema.feature.Gene</code>
     * nor to <code>org.gmod.schema.feature.Pseudogene</code>, returns null.
     */
    private final DocumentConverter<BasicGene> convertToGene = new DocumentConverter<BasicGene>() {
        public BasicGene convert(Document doc) {
            if (!doc.get("_hibernate_class").equals("org.gmod.schema.feature.Gene")
             && !doc.get("_hibernate_class").equals("org.gmod.schema.feature.Pseudogene")) {
                logger.debug(String.format("It's not a Gene, it's a '%s'", doc.get("_hibernate_class")));
                return null;
            }

            BasicGene ret = new BasicGene();

            String geneUniqueName = doc.get("uniqueName");
            ret.setOrganism(doc.get("organism.commonName"));
            ret.setFeatureId(Integer.parseInt(doc.get("featureId")));
            ret.setUniqueName(geneUniqueName);
            ret.setName(doc.get("name"));
            ret.setChromosome(new Chromosome(doc.get("chr"), Integer.parseInt(doc.get("chrId")), Integer.parseInt(doc.get("chrlen"))));
            ret.setOrganism(doc.get("organism.commonName"));
            ret.setFmin(Integer.parseInt(doc.get("start")));
            ret.setFmax(Integer.parseInt(doc.get("stop")));

            String synonyms = doc.get("synonym");
            if (synonyms != null) {
                ret.setSynonyms(Arrays.asList(synonyms.split("\t")));
            }

            BooleanQuery transcriptQuery = new BooleanQuery();
            transcriptQuery.add(new TermQuery(new Term("gene", geneUniqueName)),
                BooleanClause.Occur.MUST);

            List<Transcript> transcripts = findWithQuery(transcriptQuery, convertToTranscript);
            if (transcripts.size() == 0) {
                logger.warn(String.format("No mRNA transcripts found for gene '%s'", geneUniqueName));
            }
            ret.setTranscripts(transcripts);

            return ret;
        }
    };

    /**
     * This DocumentConverter populates a Gap object using the Lucene Document.
     */
    private final DocumentConverter<Gap> convertToGap = new DocumentConverter<Gap>() {
        public Gap convert(Document doc) {
            return new Gap(doc.get("uniqueName"), Integer.parseInt(doc.get("start")), Integer.parseInt(doc.get("stop")));
        }
    };


    private static Set<TranscriptComponent> parseLocs(String locs) {
        Set<TranscriptComponent> components = new HashSet<TranscriptComponent>();
        for (String loc: locs.split(",")) {
            int colonIndex = loc.indexOf(':');
            boolean isExon = (colonIndex == -1);
            if (!isExon) {
                String type = loc.substring(0, colonIndex);
                if (!type.endsWith("UTR")) {
                    logger.warn(String.format("Unknown transcript component type '%s'", type));
                    continue;
                }
            }

            int numberStart = colonIndex + 1;
            int numberEnd = loc.length();
            if (loc.charAt(numberStart) == '(') {
                if (!loc.endsWith(")")) {
                    throw new IllegalArgumentException(String.
                            format("Exon location '%s' starts with '(' but does not end with ')'; from string '%s'", loc, locs));
                }
                numberStart++;
                numberEnd--;
            }
            int dots = loc.indexOf("..");
            if (dots < 1) {
                throw new IllegalArgumentException(String.format("Failed to parse exon location '%s' from string '%s'", loc, locs));
            }
            int componentStart = Integer.parseInt(loc.substring(numberStart, dots));
            int componentStop  = Integer.parseInt(loc.substring(dots+2, numberEnd));

            if (isExon) {
                components.add(new Exon(componentStart, componentStop));
            } else {
                components.add(new UTR(componentStart, componentStop));
            }
        }

        return components;
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
        return findWithQuery(query, null, converter);
    }

    /**
     * Finds all documents matching the query, sorted using the specified sort order,
     * and makes a list of matches. Each document is converted to an object of type T
     * using the converter.
     *
     * @param <T> The return type
     * @param query The query object
     * @param sort How to order the results
     * @param converter Result converter
     * @return
     */
    private <T> List<T> findWithQuery(Query query, Sort sort, DocumentConverter<T> converter) {
        List<T> ret = new ArrayList<T>();

        //IndexSearcher searcher = new IndexSearcher(luceneIndex);
        logger.debug("Running Lucene query: "+query);

        TopDocs topDocs = luceneIndex.search(query, sort);

        logger.debug(String.format("Query returned %d results", topDocs.totalHits));


        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc;
            try {
                doc = fetchDocument(scoreDoc.doc);
            } catch (CorruptIndexException e) {
                throw new RuntimeException("Lucene index is corrupted!", e);
            } catch (IOException e) {
                throw new RuntimeException("IOException while fetching results of Lucene query", e);
            }
            T convertedDocument = converter.convert(doc);
            if (convertedDocument != null) {
                ret.add(convertedDocument);
            }
        }
        return ret;
    }

    protected Document fetchDocument(int docId) throws CorruptIndexException, IOException {
        return luceneIndex.getDocument(docId);
    }

    private <T> T findUniqueWithQuery(Query query, DocumentConverter<T> converter) {
        List<T> results = findWithQuery(query, converter);
        int numberOfResults = results.size();
        if (numberOfResults == 0) {
            logger.info(String.format("Failed to find gene matching Lucene query '%s'", query));
            return null;
        } else if (numberOfResults > 1) {
            logger.error(String.format("Found %d genes matching query '%s'; expected only one!",
                numberOfResults, query));
        }
        return results.get(0);
    }

    public BasicGene findGeneByUniqueName(String uniqueName) {
        return findUniqueWithQuery(new TermQuery(new Term("uniqueName", uniqueName)), convertToGene);
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

    private static BooleanQuery geneOrPseudogeneQuery = new BooleanQuery();
    static {
        geneOrPseudogeneQuery.add(new TermQuery(new Term("_hibernate_class", "org.gmod.schema.feature.Gene")),
            BooleanClause.Occur.SHOULD);
        geneOrPseudogeneQuery.add(new TermQuery(new Term("_hibernate_class", "org.gmod.schema.feature.Pseudogene")),
            BooleanClause.Occur.SHOULD);
    }

    public Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax) {
        BooleanQuery query = new BooleanQuery();
        query.add(geneOrPseudogeneQuery, BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("organism.commonName", organismCommonName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("chr", chromosomeUniqueName)),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("start", null, String.format("%09d", locMax), false, false),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("stop", String.format("%09d", locMin), null, true, false),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("strand", String.valueOf(strand))),
            BooleanClause.Occur.MUST);

        return findWithQuery(query, convertToGene);
    }

    private static final SortField START_ASC = new SortField("start");
    private static final SortField STOP_DESC = new SortField("stop", true);
    private static final Sort SORT_BY_LOCATION = new Sort(new SortField[] {START_ASC, STOP_DESC});
    public Collection<Gap> findGapsOverlappingRange(String organismCommonName,
        String chromosomeUniqueName, long locMin, long locMax) {

        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term("_hibernate_class", "org.gmod.schema.feature.Gap")),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("organism.commonName", organismCommonName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("chr", chromosomeUniqueName)),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("start", null, String.format("%09d", locMax), false, false),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("stop", String.format("%09d", locMin), null, true, false),
            BooleanClause.Occur.MUST);

        return findWithQuery(query, SORT_BY_LOCATION, convertToGap);
    }

    public Collection<Gap> findGapsOnChromosome(String organismCommonName,
            String chromosomeUniqueName) {

        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term("_hibernate_class", "org.gmod.schema.feature.Gap")),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("organism.commonName", organismCommonName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("chr", chromosomeUniqueName)),
            BooleanClause.Occur.MUST);

        return findWithQuery(query, SORT_BY_LOCATION, convertToGap);
    }

    public Collection<BasicGene> findGenesExtendingIntoRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax) {
        BooleanQuery query = new BooleanQuery();
        query.add(geneOrPseudogeneQuery, BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("organism.commonName", organismCommonName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("chr", chromosomeUniqueName)),
            BooleanClause.Occur.MUST);
        query.add(new ConstantScoreRangeQuery("stop", String.format("%09d", locMin), String.format("%09d", locMax), true, false),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("strand", String.valueOf(strand))),
            BooleanClause.Occur.MUST);

        return findWithQuery(query, convertToGene);
    }

    public Collection<BasicGene> findGenesOnStrand(String organismCommonName,
            String chromosomeUniqueName, int strand) {
        BooleanQuery query = new BooleanQuery();
        query.add(geneOrPseudogeneQuery, BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("organism.commonName", organismCommonName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("chr", chromosomeUniqueName)),
            BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("strand", String.valueOf(strand))),
            BooleanClause.Occur.MUST);

        return findWithQuery(query, convertToGene);
    }
}
