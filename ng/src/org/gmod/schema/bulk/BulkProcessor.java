package org.gmod.schema.bulk;

import org.gmod.schema.bulk.TranscriptInfo.Polypeptide;
import org.gmod.schema.utils.CvTermUtils;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BulkProcessor {
    private static final Logger logger = Logger.getLogger(BulkProcessor.class);

    // SO accession numbers for important terms
    private static final String SO_POLYPEPTIDE            = "0000104";
    private static final String SO_PSEUDOGENIC_TRANSCRIPT = "0000516";
    private static final String SO_TRANSCRIPT             = "0000673";
    private static final String SO_MRNA                   = "0000234";
    private static final String SO_PSEUDOGENE             = "0000336";
    private static final String SO_GENE                   = "0000704";
    private static final String SO_TRANSCRIPT_REGION      = "0000833";
    private static final String SO_PSEUDOGENIC_EXON       = "0000507";

    /* Initialisation */

    public BulkProcessor(Connection conn) throws SQLException {
        this.conn = conn;
        init();
    }

    private Connection conn;
    private int fetchSize = 100;
    private String organismCommonName;

    private Set<Integer> geneTypeIds;
    private Set<Integer> pseudogeneTypeIds;
    private Set<Integer> transcriptTypeIds;
    private Set<Integer> mRNATypeIds;
    private Set<Integer> polypeptideTypeIds;
    private Set<Integer> transcriptRegionTypeIds;

    private void init() throws SQLException {
        this.geneTypeIds = typeIds(SO_GENE, SO_PSEUDOGENE);
        this.pseudogeneTypeIds = typeIds(SO_PSEUDOGENE);
        this.transcriptTypeIds = typeIds(SO_TRANSCRIPT, SO_PSEUDOGENIC_TRANSCRIPT);
        this.mRNATypeIds = typeIds(SO_MRNA);
        this.polypeptideTypeIds = typeIds(SO_POLYPEPTIDE);
        this.transcriptRegionTypeIds = typeIds(SO_TRANSCRIPT_REGION, SO_PSEUDOGENIC_EXON);

        conn.createStatement().execute("set local enable_sort = false");
    }

    /* Main processing loop */

    public void processFeatures(TranscriptHandler handler) throws SQLException,
            ProcessingException {
        TranscriptIterator it = new TranscriptIterator();

        while (it.hasNext()) {
            TranscriptInfo transcriptInfo = it.next();
            processFeature(handler, transcriptInfo);
        }

        it.close();
    }

    private void processFeature(TranscriptHandler handler, TranscriptInfo ti)
            throws ProcessingException {
        try {
            handler.processTranscript(ti);
        } catch (Exception e) {
            throw new ProcessingException(String.format("Error processing transcript '%s' (ID=%d)",
                ti.transcriptUniqueName, ti.transcriptFeatureId), e);
        }
    }


    /* Data fetching and result correlation */

    private class TranscriptIterator extends AbstractIterator<TranscriptInfo> {
        private PreparedStatement stTranscripts;
        private PreparedStatement stExons;

        private ResultSet rsTranscripts;
        private ResultSet rsExons;

        private PolypeptideIterator polypeptideIterator;

        public TranscriptIterator() throws SQLException {
            stTranscripts = st(sqlTranscripts());
            stExons = st(sqlExons());

            logger.trace("Executing transcripts query...");
            rsTranscripts = stTranscripts.executeQuery();
            logger.trace("Executing exons query...");
            rsExons = stExons.executeQuery();
            logger.trace("Transcript queries executed.");

            polypeptideIterator = new PolypeptideIterator();
        }

        @Override
        public void close() throws SQLException {
            stTranscripts.close();
            stExons.close();
            polypeptideIterator.close();
        }

        private boolean exonsExhausted = false;
        private boolean exonsOutOfSync = false;

        /**
         * Get the next transcript.
         *
         * @return the next element if there is one, or <code>null</code> if not.
         * @throws DataIntegrityViolation
         */
        @Override
        public TranscriptInfo getNext() throws SQLException, DataIntegrityViolation {
            if (!rsTranscripts.next()) {
                // No more transcripts to process!
                return null;
            }

            TranscriptInfo ti = fromTranscriptResultSet(rsTranscripts);

            if (exonsExhausted) {
                throw new DataIntegrityViolation("Transcript %s has no exons", ti);
            }

            // Try to incorporate the exons and UTRs
            for(;;) {
                if (!exonsOutOfSync) {
                    // If the exons are out of sync, don't call next
                    if (!rsExons.next()) {
                        exonsExhausted = true;
                        break;
                    }
                }

                int transcriptIdOfExon = rsExons.getInt("transcript_feature_id");

                if (transcriptIdOfExon > ti.transcriptFeatureId) {
                    exonsOutOfSync = true;
                    break;
                }
                exonsOutOfSync = false;

                if (transcriptIdOfExon < ti.transcriptFeatureId) {
                    logger.warn(String.format(
                        "Transcript ID=%d not found while processing exon '%s' (ID=%d)\n" +
                        "(This probably means that the transcript has no gene)",
                        rsExons.getInt("transcript_feature_id"),
                        rsExons.getString("exon_uniquename"),
                        rsExons.getInt("exon_feature_id")));
                    continue;
                }

                assert transcriptIdOfExon == ti.transcriptFeatureId;

                logger.trace(String.format("\t...adding exon '%s' (ID=%d)",
                    rsExons.getString("exon_uniquename"), rsExons.getInt("exon_feature_id")));
                ti.exons.add(fromExonResultSet(rsExons));
            }

            if (ti.exons.isEmpty()) {
                throw new DataIntegrityViolation("Transcript %s has no exons", ti);
            }

            // Now try to incorporate the polypeptide

            return ti;
        }

        private TranscriptInfo fromTranscriptResultSet(ResultSet rs) throws SQLException {
            TranscriptInfo ti = new TranscriptInfo();

            ti.transcriptFeatureId = rs.getInt("transcript_feature_id");
            ti.transcriptUniqueName = rs.getString("transcript_uniquename");
            ti.transcriptName = rs.getString("transcript_name");
            ti.transcriptType = rs.getString("transcript_type");
            ti.transcriptSynonyms = synonyms(ti.transcriptFeatureId);

            ti.geneFeatureId = rs.getInt("gene_feature_id");
            ti.geneUniqueName = rs.getString("gene_uniquename");
            ti.geneName = rs.getString("gene_name");
            ti.geneType = rs.getString("gene_type");
            ti.geneSynonyms = synonyms(ti.geneFeatureId);

            ti.srcFeatureId = rs.getInt("srcfeature_feature_id");
            ti.srcFeatureUniqueName = rs.getString("srcfeature_uniquename");
            ti.srcFeatureSeqLen = rs.getInt("srcfeature_seqlen");

            return ti;
        }

        private TranscriptInfo.Exon fromExonResultSet(ResultSet rs) throws SQLException {
            TranscriptInfo.Exon exon = new TranscriptInfo.Exon();

            exon.featureId = rs.getInt("exon_feature_id");
            exon.type = rs.getString("exon_type");
            exon.fmin = rs.getInt("fmin");
            exon.fmax = rs.getInt("fmax");

            return exon;
        }
    }

    private class PolypeptideIterator extends AbstractIterator<TranscriptInfo.Polypeptide> {
        private PreparedStatement stPolypeptides;
        private PreparedStatement stPolypeptideProps;
        private PreparedStatement stPolypeptideTerms;

        private ResultSet rsPolypeptides;
        private ResultSet rsPolypeptideProps;
        private ResultSet rsPolypeptideTerms;

        public PolypeptideIterator() throws SQLException {
            stPolypeptides     = st(sqlPolypeptides());
            stPolypeptideProps = st(sqlPolypeptideProps());
            stPolypeptideTerms = st(sqlPolypeptideTerms());

            logger.trace("Executing polypeptides query...");
            rsPolypeptides = stPolypeptides.executeQuery();
            logger.trace("Executing polypeptide properties query...");
            rsPolypeptideProps = stPolypeptideProps.executeQuery();
            logger.trace("Executing polypeptide terms query...");
            rsPolypeptideTerms = stPolypeptideTerms.executeQuery();
            logger.trace("All polypeptide queries executed.");
        }

        @Override
        public void close() throws SQLException {
            stPolypeptides.close();
            stPolypeptideProps.close();
            stPolypeptideTerms.close();
        }

        @Override
        public Polypeptide getNext() throws SQLException {

            if (!rsPolypeptides.next()) {
                // No more polypeptides to process!
                return null;
            }

            Polypeptide p = new Polypeptide();

            p.featureId = rsPolypeptides.getInt("feature_id");
            p.uniqueName = rsPolypeptides.getString("uniquename");
            p.seqLen = rsPolypeptides.getInt("seqlen");



            return p;
        }

    }

    /* SQL, and related utility methods */
    private int cursorNumber = 1;
    private PreparedStatement st(String sql) throws SQLException {
        String cursorName = String.format("curs%03d", cursorNumber++);

        // Fake bind variable
        //**sql = sql.replace("?", "'" + organismCommonName.replace("'", "''") + "'");

        conn.createStatement().execute(
            "declare " + cursorName + " no scroll cursor for\n" + sql
        );

        PreparedStatement st = conn.prepareStatement("fetch forward all in " + cursorName);
        st.setFetchSize(fetchSize);

        return st;
    }

    private String sqlTranscripts() {
        return
        "select gene.feature_id as gene_feature_id\n"
        + "     , gene.uniquename as gene_uniquename\n"
        + "     , gene.name as gene_name\n"
        + "     , gene_type.name as gene_type\n"
        + "     , (gene.type_id in (\n"
        +            commaSeparatedList(this.pseudogeneTypeIds) + "\n"
        + "       )) as is_pseudo\n"
        + "     , transcript.feature_id as transcript_feature_id\n"
        + "     , transcript.uniquename as transcript_uniquename\n"
        + "     , transcript.name as transcript_name\n"
        + "     , transcript_type.name as transcript_type\n"
        + "     , (transcript.type_id in (\n"
        +            commaSeparatedList(this.mRNATypeIds) + "\n"
        + "       )) as is_coding\n"
        + "     , transcriptloc.fmin\n"
        + "     , transcriptloc.fmax\n"
        + "     , transcriptloc.strand\n"
        + "     , srcfeature.feature_id as srcfeature_feature_id\n"
        + "     , srcfeature.uniquename as srcfeature_uniquename\n"
        + "     , srcfeature.seqlen as srcfeature_seqlen\n"
        + " from feature gene\n"
        + " join cvterm gene_type on gene.type_id = gene_type.cvterm_id\n"
        + " join feature_relationship transcript_gene on transcript_gene.object_id = gene.feature_id\n"
        + " join feature transcript on transcript_gene.subject_id = transcript.feature_id\n"
        + " join cvterm transcript_type on transcript.type_id = transcript_type.cvterm_id\n"
        + " join featureloc transcriptloc on transcript.feature_id = transcriptloc.feature_id\n"
        + " join feature srcfeature on transcriptloc.srcfeature_id = srcfeature.feature_id\n"
        + " where transcript.type_id in (\n"
        +     commaSeparatedList(this.transcriptTypeIds)
        + "\n)\n"
        + " and gene.type_id in (\n"
        +     commaSeparatedList(this.geneTypeIds)
        + "\n)\n"
        /*+ " and transcript.organism_id = (\n"
        + "     select organism_id from organism\n"
        + "     where common_name = ?\n"
        + " )\n"*/
        + " and transcriptloc.locgroup = 0 and transcriptloc.rank = 0\n"
        + " and not transcript.is_obsolete\n"
        + " order by transcript.feature_id";
    }

    private String sqlExons() {
        return
        "select exon_transcript.object_id as transcript_feature_id\n"
        + "   , exon.feature_id as exon_feature_id\n"
        + "   , exon.uniquename as exon_uniquename\n"
        + "   , exonloc.fmin\n"
        + "   , exonloc.fmax\n"
        + "   , exon_type.name as exon_type\n"
        + "from feature exon\n"
        + "join featureloc exonloc on exon.feature_id = exonloc.feature_id\n"
        + "join cvterm exon_type on exon.type_id = exon_type.cvterm_id\n"
        + "join feature_relationship exon_transcript on exon_transcript.subject_id = exon.feature_id\n"
        + "where exon.type_id in (\n"
        +     commaSeparatedList(this.transcriptRegionTypeIds)
        + "\n)\n"
        /*+ "and exon.organism_id = (\n"
        + "    select organism_id from organism\n"
        + "    where common_name = ?\n"
        + ")\n" */
        + "and exonloc.locgroup = 0\n"
        + "and exonloc.rank = 0\n"
        + "order by exon_transcript.object_id";
    }

    private String sqlPolypeptides() {
        return
        "select polypeptide.feature_id\n"
        + "   , polypeptide.uniquename\n"
        + "   , polypeptide.seqlen\n"
        + "from feature polypeptide\n"
        + "where type_id in (\n"
        +     commaSeparatedList(this.polypeptideTypeIds) + "\n"
        + ")\n"
        /*+ "and polypeptide.organism_id = (\n"
        + "    select organism_id from organism\n"
        + "    where common_name = ?\n"
        + ")\n"*/
        + "order by polypeptide.feature_id";
    }

    private String sqlPolypeptideProps() {
        return
        "select polypeptide.feature_id\n"
        + "   , featureprop_type_cv.name as type_cv\n"
        + "   , featureprop_type.name as type\n"
        + "   , featureprop.value\n"
        + "from feature polypeptide\n"
        + "join featureprop on featureprop.feature_id = polypeptide.feature_id\n"
        + "join cvterm featureprop_type on featureprop.type_id = featureprop_type.cvterm_id\n"
        + "join cv featureprop_type_cv on featureprop_type.cv_id = featureprop_type.cv_id\n"
        + "where polypeptide.type_id in (\n"
        +     commaSeparatedList(this.polypeptideTypeIds) + "\n"
        + ")\n"
        /*+ "and polypeptide.organism_id = (\n"
        + "    select organism_id from organism\n"
        + "    where common_name = ?\n"
        + ")\n"*/
        + "order by polypeptide.feature_id";
    }

    private String sqlPolypeptideTerms() {
        return
        "select polypeptide.feature_id\n"
        + "   , cv.name as cv\n"
        + "   , cvterm.name as term\n"
        + "from feature polypeptide\n"
        + "join feature_cvterm on feature_cvterm.feature_id = polypeptide.feature_id\n"
        + "join cvterm on feature_cvterm.cvterm_id = cvterm.cvterm_id\n"
        + "join cv on cvterm.cv_id = cv.cv_id\n"
        + "where polypeptide.type_id in (\n"
        +     commaSeparatedList(this.polypeptideTypeIds) + "\n"
        + ")\n"
        /*+ "and polypeptide.organism_id = (\n"
        + "    select organism_id from organism\n"
        + "    where common_name = ?\n"
        + ")\n"*/
        + "order by polypeptide.feature_id";
    }

    /* Type IDs */

    private boolean checkedTypeIds = false;
    private Set<Integer> typeIds(String... accessions) throws SQLException {
        Set<Integer> typeIds = new HashSet<Integer>();

        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < accessions.length; i++) {
            if (questionMarks.length() > 0) {
                questionMarks.append(", ");
            }
            questionMarks.append("?");
        }

        if (!checkedTypeIds) {
            CvTermUtils.checkCvTermPath(conn);
            checkedTypeIds = true;
        }

        String sql = "select cvtermpath.subject_id" + " from cvtermpath"
                + " join cvterm type on cvtermpath.type_id = type.cvterm_id"
                + " join cvterm object on cvtermpath.object_id = object.cvterm_id"
                + " join dbxref object_dbxref on object.dbxref_id = object_dbxref.dbxref_id"
                + " join cv object_cv on object.cv_id = object_cv.cv_id"
                + " where lower(type.name) = 'is_a'" + " and object_cv.name = 'sequence'"
                + " and object_dbxref.accession in (" + questionMarks + ")";

        PreparedStatement st = conn.prepareStatement(sql);
        try {
            for (int i = 0; i < accessions.length; i++) {
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

    private static String commaSeparatedList(Iterable<?> list) {
        StringBuilder s = new StringBuilder();
        for (Object item : list) {
            if (s.length() > 0) {
                s.append(", ");
            }
            s.append(item);
        }
        return s.toString();
    }


    /* Synonyms */

    private Map<Integer, Collection<TranscriptInfo.Synonym>> synonymsByFeatureId;

    private void fetchSynonyms() throws SQLException {
        String sql =
            "select feature_synonym.feature_id\n"
            + "     , synonym.name as synonym\n"
            + "     , synonym_type.name as type\n"
            + " from feature\n"
            + " join feature_synonym on feature.feature_id = feature_synonym.feature_id\n"
            + " join synonym on feature_synonym.synonym_id = synonym.synonym_id\n"
            + " join cvterm synonym_type on synonym.type_id = synonym_type.cvterm_id\n"
            + " where feature.organism_id = (\n"
            + "     select organism_id from organism\n"
            + "     where common_name = ?\n"
            + " )";
        logger.debug("SQL: " + sql);
        PreparedStatement st = conn.prepareStatement(sql);
        try {
            synonymsByFeatureId = new HashMap<Integer, Collection<TranscriptInfo.Synonym>>();
            st.setString(1, organismCommonName);
            ResultSet rs = st.executeQuery();
            logger.debug("Fetching synonyms...");
            int numberOfSynonyms = 0;
            while (rs.next()) {
                int featureId = rs.getInt("feature_id");
                TranscriptInfo.Synonym synonym = new TranscriptInfo.Synonym();
                synonym.name = rs.getString("synonym");
                synonym.type = rs.getString("type");

                if (!synonymsByFeatureId.containsKey(featureId)) {
                    synonymsByFeatureId.put(featureId, new HashSet<TranscriptInfo.Synonym>());
                }
                synonymsByFeatureId.get(featureId).add(synonym);
                numberOfSynonyms++;
            }
            logger.debug(String.format("Loaded %d synonyms", numberOfSynonyms));
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                logger.error("Error closing prepared statement", e);
            }
        }
    }

    private Collection<TranscriptInfo.Synonym> synonyms(int... featureIds) {
        Collection<TranscriptInfo.Synonym> synonyms = new HashSet<TranscriptInfo.Synonym>();
        for (int featureId : featureIds) {
            if (synonymsByFeatureId.containsKey(featureId)) {
                synonyms.addAll(synonymsByFeatureId.get(featureId));
            }
        }
        return synonyms;
    }

}
