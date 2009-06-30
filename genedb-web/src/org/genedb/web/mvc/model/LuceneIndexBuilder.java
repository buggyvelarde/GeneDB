package org.genedb.web.mvc.model;

import org.genedb.db.analyzers.AllNamesAnalyzer;

import org.gmod.schema.utils.CvTermUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds Lucene indices from the Chado database. This is an experiment to
 * see how far we can improve on the performance of {@link PopulateLuceneIndices}
 * by using direct JDBC and Lucene calls, rather than using Hibernate and
 * Hibernate Search.
 *
 * @author rh11
 *
 */
public class LuceneIndexBuilder {
    private static final Logger logger = Logger.getLogger(LuceneIndexBuilder.class);

    private static final String TRANSCRIPT_INDEX_NAME = "transcript";

    public static void main(String[] args)
        throws IOException, ClassNotFoundException, SQLException, ProcessingException
    {
        String jdbcConnectionString = args[0];
        String databaseUser = args[1];
        String indexDirectory = args[2];
        String organismCommonName = args[3];

        String databasePassword = new String(System.console().readPassword(
            "Password for %s@%s: ",
            databaseUser, jdbcConnectionString));

        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(
            jdbcConnectionString, databaseUser, databasePassword);
        conn.setAutoCommit(false); // Autocommit must be off to use cursors
        configureLog4j();

        LuceneIndexBuilder luceneIndexBuilder = new LuceneIndexBuilder(new File(indexDirectory));

        // There doesn't seem to be anything to be gained by changing this,
        // so the option is disabled. The default of 100 offers a good tradeoff.
        //
        // if (args.length > 4) {
        //     luceneIndexBuilder.fetchSize = Integer.parseInt(args[4]);
        // }

        luceneIndexBuilder.addOrganism(conn, organismCommonName);
    }

    private static void configureLog4j() {
        String log4jprops = "/log4j.lucene.properties";
        URL url = LuceneIndexBuilder.class.getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    private File indexDirectory;
    // Defaults for new index directories
    private Analyzer analyzer = new StandardAnalyzer(); // Is this an appropriate analyzer?
    private IndexWriter.MaxFieldLength maxFieldLength = IndexWriter.MaxFieldLength.LIMITED;

    private IndexWriter getIndexWriter(String name)
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        Directory directory = FSDirectory.getDirectory(new File(indexDirectory, name));
        return new IndexWriter(directory, analyzer, maxFieldLength);
    }

    private Connection conn;
    private int fetchSize = 100;
    private IndexWriter featureIndexWriter;
    public LuceneIndexBuilder(File indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public void addOrganism(Connection conn, String commonName)
        throws CorruptIndexException, LockObtainFailedException, IOException, SQLException, ProcessingException
    {
        this.conn = conn;
        this.featureIndexWriter = getIndexWriter(TRANSCRIPT_INDEX_NAME);
        try {
            addGenes(commonName);
        } finally {
            this.featureIndexWriter.close();
            this.featureIndexWriter = null;
        }
    }

    private Set<Integer> typeIds(String... accessions) throws SQLException {
        Set<Integer> typeIds = new HashSet<Integer>();

        StringBuilder questionMarks = new StringBuilder();
        for (int i=0; i < accessions.length; i++) {
            if (questionMarks.length() > 0) {
                questionMarks.append(", ");
            }
            questionMarks.append("?");
        }

        CvTermUtils.checkCvTermPath(conn);

        String sql =
            "select cvtermpath.subject_id"+
            " from cvtermpath" +
            " join cvterm type on cvtermpath.type_id = type.cvterm_id"+
            " join cvterm object on cvtermpath.object_id = object.cvterm_id"+
            " join dbxref object_dbxref on object.dbxref_id = object_dbxref.dbxref_id"+
            " join cv object_cv on object.cv_id = object_cv.cv_id"+
            " where lower(type.name) = 'is_a'"+
            " and object_cv.name = 'sequence'"+
            " and object_dbxref.accession in (" + questionMarks + ")";

        logger.debug("SQL: " + sql);
        PreparedStatement st = conn.prepareStatement(sql);
        try {
            for (int i=0; i < accessions.length; i++) {
                st.setString(1 + i, accessions[i]);
            }
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                typeIds.add(rs.getInt("subject_id"));
            }
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                logger.error("Error during close()", e);
            }
        }

        return typeIds;
    }

    private interface ResultSetProcessor {
        public void process(GeneInfo geneInfo) throws Exception;
        public void noResults() throws Exception;
    }
    private class ProcessingException extends Exception {
        public ProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private class GeneInfo {
        int featureId;
        String uniqueName;
        String name;
        boolean isAnalysis;

        int fmin;
        int fmax;
        int strand;
        int srcFeatureId;
        String srcFeatureUniqueName;
        int srcFeatureSeqLen;
        List<String> synonyms = new ArrayList<String>();

        int organismId;
        String organismCommonName;
        String organismAbbreviation;
        String organismGenus;
        String organismSpecies;
        int cvTermId;
        String cvTermName;

        public GeneInfo(ResultSet rs) throws SQLException {
            featureId = rs.getInt("feature_id");
            uniqueName = rs.getString("uniquename");
            name = rs.getString("name");
            isAnalysis = rs.getBoolean("is_analysis");

            fmin = rs.getInt("fmin");
            fmax = rs.getInt("fmax");
            strand = rs.getShort("strand");
            srcFeatureId = rs.getInt("srcfeature_id");
            srcFeatureUniqueName = rs.getString("srcfeature_uniquename");
            srcFeatureSeqLen = rs.getInt("srcfeature_seqlen");

            String synonym = rs.getString("synonym");
            if (synonym != null) {
                synonyms.add(synonym);
            }

            organismId = rs.getInt("organism_id");
            organismCommonName = rs.getString("organism_common_name");
            organismAbbreviation = rs.getString("organism_abbreviation");
            organismGenus = rs.getString("organism_genus");
            organismSpecies = rs.getString("organism_species");

            cvTermId = rs.getInt("type_cvterm_id");
            cvTermName = rs.getString("type_name");
        }
    }

    int serialNumber;

    private void processFeatures(String organismCommonName,
            Collection<Integer> typeIds, ResultSetProcessor processor)
        throws SQLException, ProcessingException
    {
        if (typeIds.isEmpty()) {
            throw new IllegalArgumentException("typeIds is empty");
        }
        StringBuilder typeIdsCommaSeparated = new StringBuilder();
        for(int typeId: typeIds) {
            if (typeIdsCommaSeparated.length() > 0) {
                typeIdsCommaSeparated.append(", ");
            }
            typeIdsCommaSeparated.append(typeId);
        }

        String sql =
            "select feature.feature_id"+
            "     , feature.uniquename"+
            "     , feature.name"+
            "     , feature.is_analysis"+
            "     , featureloc.fmin"+
            "     , featureloc.fmax"+
            "     , featureloc.strand"+
            "     , srcfeature.feature_id as srcfeature_id"+
            "     , srcfeature.uniquename as srcfeature_uniquename"+
            "     , srcfeature.seqlen as srcfeature_seqlen"+
            "     , synonym_sub.name as synonym"+
            "     , organism.organism_id"+
            "     , organism.genus as organism_genus"+
            "     , organism.species as organism_species"+
            "     , organism.common_name as organism_common_name"+
            "     , organism.abbreviation as organism_abbreviation"+
            "     , type.cvterm_id as type_cvterm_id"+
            "     , type.name as type_name"+
            " from feature"+
            " join featureloc on feature.feature_id = featureloc.feature_id"+
            " join feature srcfeature on featureloc.srcfeature_id = srcfeature.feature_id"+
            " left join (" +
            "   select feature_synonym.feature_id, synonym.name" +
            "   from feature_synonym join synonym on feature_synonym.synonym_id = synonym.synonym_id" +
            " ) synonym_sub on feature.feature_id = synonym_sub.feature_id"+
            " join organism on feature.organism_id = organism.organism_id"+
            " join cvterm type on feature.type_id = type.cvterm_id"+
            " where feature.type_id in (" + typeIdsCommaSeparated + ")"+
            " and featureloc.locgroup = 0 and featureloc.rank = 0"+
            " and not feature.is_obsolete"+
            " and feature.organism_id = ("+
            "   select organism_id from organism where common_name = ?" +
            " )";
        logger.debug("SQL: " + sql);
        PreparedStatement st = conn.prepareStatement(sql);
        st.setFetchSize(fetchSize);
        serialNumber = 1;

        try {
            st.setString(1, organismCommonName);
            ResultSet rs = st.executeQuery();
            int previousFeatureId = -1;
            GeneInfo geneInfo = null;

            while (rs.next()) {
                int thisFeatureId = rs.getInt("feature_id");
                if (thisFeatureId == previousFeatureId) {
                    geneInfo.synonyms.add(rs.getString("synonym"));
                } else {
                    if (previousFeatureId > 0) {
                        processFeature(processor, geneInfo);
                    }
                    geneInfo = new GeneInfo(rs);
                    previousFeatureId = thisFeatureId;
                }
            }
            if (previousFeatureId > 0) {
                processFeature(processor, geneInfo);
            }
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                logger.error("Error during close()", e);
            }
        }
    }

    /**
     * @param processor
     * @param rs
     * @param n
     * @param geneInfo
     * @throws ProcessingException
     * @throws SQLException
     */
    private void processFeature(ResultSetProcessor processor, GeneInfo geneInfo)
            throws ProcessingException, SQLException {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("[%d] Processing gene '%s'",
                    serialNumber++, geneInfo.uniqueName));
            }
            processor.process(geneInfo);
        } catch (Exception e) {
            throw new ProcessingException(
                String.format("Error processing feature '%s' (ID=%d)",
                    geneInfo.uniqueName, geneInfo.featureId), e);
        }
    }

    Set<Integer> processedGeneIds;
    private void addGenes(final String commonName)
        throws CorruptIndexException, IOException, SQLException, ProcessingException
    {
        Set<Integer> geneTypeIds = typeIds("0000704", "0000336");
        processedGeneIds = new HashSet<Integer>();
        processFeatures(commonName, geneTypeIds, new ResultSetProcessor() {

            @Override
            public void process(GeneInfo geneInfo)
            throws SQLException, CorruptIndexException, IOException {
                if (processedGeneIds.contains(geneInfo.featureId)) {
                    logger.error(String.format("Gene '%s' (ID=%d) already processed"));
                    return;
                }
                processedGeneIds.add(geneInfo.featureId);
                Document doc = documentForGene(geneInfo);
                featureIndexWriter.addDocument(doc);
            }

            @Override
            public void noResults() {
                logger.error(String.format("No genes found for '%s' - check spelling", commonName));
            }

        });
        featureIndexWriter.close();
    }

    private static Analyzer allNamesAnalyzer = new AllNamesAnalyzer();
    private Document documentForGene(GeneInfo geneInfo) {
        Document doc = new Document();

        StringBuilder synonymsAsTabSeparatedString = new StringBuilder();
        for (String synonym: geneInfo.synonyms) {
            if (synonymsAsTabSeparatedString.length() > 0) {
                synonymsAsTabSeparatedString.append('\t');
            }
            synonymsAsTabSeparatedString.append(synonym);
        }

        StringBuilder allNames = new StringBuilder();
        if (geneInfo.name != null) {
            allNames.append(geneInfo.name + ' ');
        }
        allNames.append(geneInfo.uniqueName + ' ');
        allNames.append(synonymsAsTabSeparatedString);

        TokenStream allNamesTokenized = allNamesAnalyzer.tokenStream(
            "allNames", new StringReader(allNames.toString()));

        doc.add(new Field("featureId", Integer.toString(geneInfo.featureId),
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("uniqueName", geneInfo.uniqueName,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("name", geneInfo.name == null ? "" : geneInfo.name,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("analysis", Boolean.toString(geneInfo.isAnalysis),
            Field.Store.NO, Field.Index.NOT_ANALYZED));

        doc.add(new Field("synonym", synonymsAsTabSeparatedString.toString(),
            Field.Store.YES, Field.Index.ANALYZED));

        doc.add(new Field("allNames", allNamesTokenized));

        doc.add(new Field("start", String.format("%09d", geneInfo.fmin),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("stop", String.format("%09d", geneInfo.fmax),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("strand", Integer.toString(geneInfo.strand),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("chr", geneInfo.srcFeatureUniqueName,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("chrId", Integer.toString(geneInfo.srcFeatureId),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("chrlen", Integer.toString(geneInfo.srcFeatureSeqLen),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("organism.organismId", Integer.toString(geneInfo.organismId),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("organism.commonName", geneInfo.organismCommonName,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("organism.abbreviation", geneInfo.organismAbbreviation,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("organism.genus", geneInfo.organismGenus,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("organism.species", geneInfo.organismSpecies,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("type.cvTermId", Integer.toString(geneInfo.cvTermId),
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("type.name", geneInfo.cvTermName,
            Field.Store.YES, Field.Index.NOT_ANALYZED));

        // protein
        return doc;
    }

}
