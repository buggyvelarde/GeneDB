package org.genedb.db.fixup;

import org.genedb.util.SequenceUtils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Populate the 'residues' column of transcript features with the CDS,
 * and polypeptide features with the protein sequence, using the feature
 * location information and the top-level sequence.
 *
 * @author rh11
 *
 */
public class FixResidues {
    private final Logger logger = Logger.getLogger(FixResidues.class);

    /*
     * This is what we're currently using for apicoplast chromosomes, but it ain't right.
     */
    private static final String APICOPLAST_CHROMOSOME = "apicoplast_sequence";

    private static final String MITOCHONDRIAL_CHROMOSOME = "mitochondrial_chromosome";

    private static final int APICOPLAST_TRANSLATION_TABLE = 11;

    private void debug(String format, Object... args) {
        logger.debug(String.format(format, args));
    }
    private void info(String format, Object... args) {
        logger.info(String.format(format, args));
    }

    private void error(Exception exception) {
        error(exception.getMessage(), exception);
    }
    private void error(String message, Exception exception) {
        logger.error(message, exception);
    }
    private void error(String format, Object... args) {
        logger.error(String.format("Error: "+format, args));
    }

    private void warn(List<String> warnings, String format, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(format, args));
        for (String warning: warnings) {
            sb.append("\n\t");
            sb.append(warning);
        }
        logger.warn(sb.toString());
    }

    public static void main(String[] args) throws Exception {

        boolean verbose = false;
        int i;
        for (i=0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-verbose")
                        || args[i].equals("-v")
                        || args[i].equals("--verbose"))
                    verbose = true;
                else {
                    System.err.printf("Unrecognised option '%s'\n", args[i]);
                    System.exit(1);
                }
            }
            else
                break;
        }

        FixResidues fr = new FixResidues(verbose);
        if (i == args.length)
            fr.fixAll();
        else
            for (; i < args.length; i++)
                fr.fixOrganismByName(args[i]);
    }


    private Connection conn;
    private static ResourceBundle config = ResourceBundle.getBundle("project");
    private TypeCodes typeCodes;

    private FixResidues (boolean verbose)
        throws SQLException, ClassNotFoundException {

        if (verbose) {
            logger.setLevel(Level.DEBUG);
        }

        String url = String.format("jdbc:postgresql://%s:%s/%s",
            config.getString("dbhost"),
            config.getString("dbport"),
            config.getString("dbname"));
        String username = config.getString("dbuser");
        String password = config.getString("dbpassword");

        Class.forName("org.postgresql.Driver");
        info("Connecting to database '%s' as user '%s'", url, username);
        this.conn = DriverManager.getConnection(url, username, password);
        this.typeCodes = new TypeCodes(conn);
    }

    private void fixAll() throws SQLException {
        for (Organism organism: getOrganisms(conn)) {
            fixOrganism(organism);
        }
    }

    private void fixOrganismByName(String organismName) throws SQLException {
        for (Organism organism: getOrganisms(conn)) {
            if (!organism.commonName.equals(organismName))
                continue;

            fixOrganism(organism);
        }
    }

    private List<Organism> getOrganisms(Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(
             "select organism_id"
            +"    , common_name"
            +"    , translation_table_prop.value as translation_table"
            +"    , mitochondrial_translation_table_prop.value as mitochondrial_translation_table"
            +" from organism"
            +" left join organismprop translation_table_prop using (organism_id)"
            +" left join organismprop mitochondrial_translation_table_prop using (organism_id)"
            +" where (translation_table_prop.type_id is null or translation_table_prop.type_id = ?)"
            +" and   (mitochondrial_translation_table_prop.type_id is null or mitochondrial_translation_table_prop.type_id = ?)"
        );
        List<Organism> organisms = new ArrayList<Organism>();
        try {
            st.setInt(1, typeCodes.typeId("genedb_misc", "translationTable"));
            st.setInt(2, typeCodes.typeId("genedb_misc", "mitochondrialTranslationTable"));
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int organismId = rs.getInt("organism_id");
                String organismName = rs.getString("common_name");
                String translationTableString = rs.getString("translation_table");
                String mitochondrialTranslationTableString = rs.getString("mitochondrial_translation_table");

                int translationTable;
                if (translationTableString == null || translationTableString.length() == 0) {
                    translationTable = 1;
                    error("No translation table found for '%s'. Defaulting to %d, which is probably okay.", organismName, translationTable);
                }
                else
                    translationTable = Integer.parseInt(translationTableString);

                int mitochondrialTranslationTable;
                if (mitochondrialTranslationTableString == null || mitochondrialTranslationTableString.length() == 0) {
                    mitochondrialTranslationTable = 4;
                    error("No mitochondrial translation table found for '%s'. Defaulting to %d, which is probably wrong!",
                        organismName, mitochondrialTranslationTable);
                }
                else
                    mitochondrialTranslationTable = Integer.parseInt(mitochondrialTranslationTableString);

                organisms.add(new Organism(organismId, organismName, translationTable, mitochondrialTranslationTable));
            }
            return organisms;
        }
        finally {
            try {
                st.close();
            } catch (SQLException e) {
                error("Exception closing statement", e);
            }
        }
    }

    private void fixOrganism(Organism organism) throws SQLException {
        info("Processing organism: %s", organism.commonName);
        info("\t(translation table = %d, mitochondrial = %d)", organism.translationTable, organism.mitochondrialTranslationTable);

        PreparedStatement st = conn.prepareStatement(
            "select organism.common_name"
            +"      , toplevel.feature_id"
            +"      , toplevel.uniquename"
            +"      , toplevel.type_id"
            +" from feature toplevel"
            +" join organism using (organism_id)"
            +" join ("
            +"     select distinct geneloc.srcfeature_id as feature_id"
            +"     from feature gene"
            +"     join featureloc geneloc using (feature_id)"
            +"     where gene.type_id in (?, ?)"
            +" ) gene_srcfeature using (feature_id)"
            +" where toplevel.organism_id = ?",
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        try {
            st.setInt(1, typeCodes.typeId("sequence", "gene"));
            st.setInt(2, typeCodes.typeId("sequence", "pseudogene"));
            st.setInt(3, organism.organismId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int topLevelFeatureId = rs.getInt("feature_id");
                String topLevelFeatureUniqueName = rs.getString("uniquename");
                int topLevelFeatureTypeId = rs.getInt("type_id");

                String topLevelFeatureCv = typeCodes.cvName(topLevelFeatureTypeId);
                String topLevelFeatureTerm = typeCodes.termName(topLevelFeatureTypeId);
                if (!topLevelFeatureCv.equals("sequence"))
                    throw new IllegalStateException(String.format("Top-level feature '%s' has type '%s'/'%s', which does not belong to the sequence ontology",
                        topLevelFeatureUniqueName, topLevelFeatureCv, topLevelFeatureTerm));

                int translationTableId;
                if (topLevelFeatureTerm.equals(MITOCHONDRIAL_CHROMOSOME))
                    translationTableId = organism.mitochondrialTranslationTable;
                else if (topLevelFeatureTerm.equals(APICOPLAST_CHROMOSOME))
                    translationTableId = APICOPLAST_TRANSLATION_TABLE;
                else
                    translationTableId = organism.translationTable;

                debug("Processing top-level feature %s '%s'", topLevelFeatureTerm, topLevelFeatureUniqueName);
                fixTopLevelFeature(translationTableId, topLevelFeatureId);
            }
        }
        finally {
            try {
                st.close();
            } catch (SQLException e) {
                error("Exception closing statement", e);
            }
        }
    }

    private void fixTopLevelFeature(int translationTableId, int topLevelFeatureId) throws SQLException {
        String topLevelSequence = loadResidues(topLevelFeatureId);
        PreparedStatement st = conn.prepareStatement(
            "select gene.feature_id"
            +"    , gene.uniquename"
            +"    , geneloc.strand"
            +" from feature gene"
            +" join featureloc geneloc using (feature_id)"
            +" where gene.type_id in (?, ?)"
            +" and geneloc.rank = 0"
            +" and geneloc.srcfeature_id = ?"
        );
        try {
            st.setInt(1, typeCodes.typeId("sequence", "gene"));
            st.setInt(2, typeCodes.typeId("sequence", "pseudogene"));
            st.setInt(3, topLevelFeatureId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int geneFeatureId = rs.getInt("feature_id");
                int strand = rs.getInt("strand");
                String geneUniqueName = rs.getString("uniquename");

                fixGene(translationTableId, topLevelSequence, strand, geneFeatureId, geneUniqueName);
            }
        }
        finally {
            try {
                st.close();
            } catch (SQLException e) {
                error("Exception closing statement", e);
            }
        }
    }

    private String loadResidues(int topLevelFeatureId) throws SQLException {
        PreparedStatement st = conn.prepareStatement(
            "select residues from feature where feature_id = ?"
        );
        try {
            st.setInt(1, topLevelFeatureId);
            ResultSet rs = st.executeQuery();
            if (!rs.next())
                throw new IllegalStateException(String.format("Top-level feature ID=%d could not be found", topLevelFeatureId));
            return rs.getString("residues");
        }
        finally {
            try {
                st.close();
            }
            catch (SQLException e) {
                error("Exception closing statement", e);
            }
        }
    }

    private void fixGene(int translationTableId, String topLevelSequence, int strand, int geneFeatureId, String geneUniqueName) throws SQLException {
        debug("Processing gene '%s' (ID=%d) on strand %d", geneUniqueName, geneFeatureId, strand);

        PreparedStatement st = conn.prepareStatement(
            "select transcript.feature_id as transcript_id"
            +"    , transcript.uniquename as transcript_uniquename"
            +"    , transcript.type_id as transcript_type"
            +"    , exon.feature_id as exon_id"
            +"    , exon.uniquename as exon_uniquename"
            +"    , exonloc.phase"
            +"    , exonloc.fmin"
            +"    , exonloc.fmax"
            +", exists ("
            +"     select 8"
            +"     from featureprop"
            +"     where feature_id = transcript.feature_id"
            +"     and type_id = ?"
            +" ) as stop_codon_redefined"
            +" from feature exon"
            +" join featureloc exonloc"
            +"      on exonloc.feature_id = exon.feature_id"
            +" join feature_relationship exon_transcript"
            +"      on exon_transcript.subject_id = exon.feature_id"
            +" join feature transcript"
            +"      on exon_transcript.object_id = transcript.feature_id"
            +" join feature_relationship transcript_gene"
            +"      on transcript_gene.subject_id = transcript.feature_id"
            +" where transcript_gene.object_id = ?"
            +" and exon.type_id in (?, ?)"
            +" and transcript.type_id in (?, ?, ?, ?, ?)"
        );
        Map<Integer,Transcript> transcriptsById = new HashMap<Integer,Transcript>();
        try {
            st.setInt(1, typeCodes.typeId("sequence", "stop_codon_redefined_as_selenocysteine"));
            st.setInt(2, geneFeatureId);
            st.setInt(3, typeCodes.typeId("sequence", "exon"));
            st.setInt(4, typeCodes.typeId("sequence", "pseudogenic_exon"));

            st.setInt(5, typeCodes.typeId("sequence", "mRNA"));
            st.setInt(6, typeCodes.typeId("sequence", "rRNA"));
            st.setInt(7, typeCodes.typeId("sequence", "tRNA"));
            st.setInt(8, typeCodes.typeId("sequence", "snRNA"));
            st.setInt(9, typeCodes.typeId("sequence", "pseudogenic_transcript"));
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int transcriptId = rs.getInt("transcript_id");
                String transcriptName = rs.getString("transcript_uniquename");
                int transcriptType = rs.getInt("transcript_type");

                int exonId = rs.getInt("exon_id");
                String exonName = rs.getString("exon_uniquename");
                int phase = rs.getInt("phase");
                int fmin = rs.getInt("fmin");
                int fmax = rs.getInt("fmax");

                if (!transcriptsById.containsKey(transcriptId)) {
                    boolean stopCodonRedefined = rs.getBoolean("stop_codon_redefined");
                    boolean isCoding = transcriptType == typeCodes.typeId("sequence", "mRNA")
                                    || transcriptType == typeCodes.typeId("sequence", "pseudogenic_transcript");
                    boolean isPseudo = transcriptType == typeCodes.typeId("sequence", "pseudogenic_transcript");
                    Transcript transcript = new Transcript(
                        transcriptId, transcriptName,
                        isCoding,
                        translationTableId, phase,
                        isPseudo, stopCodonRedefined);
                    transcriptsById.put(transcriptId, transcript);
                }
                Transcript transcript = transcriptsById.get(transcriptId);
                transcript.addExon(new Exon(exonId, exonName, fmin, fmax));
            }
        }
        finally {
            try {
                st.close();
            }
            catch (SQLException e) {
                error("Exception closing statement", e);
            }
        }

        if (transcriptsById.isEmpty())
            error("Gene '%s' (ID=%d) has no transcripts, or at any rate no exons",
                geneUniqueName, geneFeatureId);
        for (Transcript transcript: transcriptsById.values()) {
            debug("Transcript: %s", transcript.uniqueName);
            fixTranscript(topLevelSequence, strand, transcript);
        }
    }

    private void fixTranscript(String topLevelSequence, int strand, Transcript transcript) throws SQLException {
        StringBuilder cdsBuilder = new StringBuilder();
        for (Exon exon: transcript.exons) {
            debug("\tExon %s (%d-%d)", exon.uniqueName, exon.fmin, exon.fmax);
            cdsBuilder.append(topLevelSequence.substring(exon.fmin, exon.fmax));
        }
        String cds;
        if (strand == 1)
            cds = cdsBuilder.toString();
        else if (strand == -1)
            cds = SequenceUtils.reverseComplement(cdsBuilder.toString());
        else
            throw new IllegalStateException(String.format("Strand is neither +1 nor -1 (%d)", strand));

        debug("CDS: %s", cds);
        transcript.cds = cds;
        setResidues(transcript.featureId, cds);

        if (!transcript.isCoding) {
            debug("\t...non-coding transcript; nothing more to do");
            return;
        }

        PreparedStatement st = conn.prepareStatement(
            "select polypeptide.feature_id"
            +"    , polypeptide.uniquename"
            +" from feature polypeptide"
            +" join feature_relationship polypeptide_transcript"
            +"     on polypeptide_transcript.subject_id = polypeptide.feature_id"
            +" join feature transcript"
            +"     on polypeptide_transcript.object_id = transcript.feature_id"
            +" where transcript.feature_id = ?"
            +" and polypeptide.type_id = ?");
        try {
            st.setInt(1, transcript.featureId);
            st.setInt(2, typeCodes.typeId("sequence", "polypeptide"));

            ResultSet rs = st.executeQuery();
            if (!rs.next()) {
                error("No polypeptide found for mRNA transcript '%s'", transcript.uniqueName);
                return;
            }
            if (!rs.isLast())
                error("More than one polypeptide found for mRNA transcript '%s'", transcript.uniqueName);

            int polypeptideId = rs.getInt("feature_id");
            String polypeptideName = rs.getString("uniquename");
            debug("Polypeptide '%s'", polypeptideName);
            fixPolypeptide(transcript, polypeptideId);
        }
        finally {
            try {
                st.close();
            }
            catch (SQLException e) {
                error("Exception closing statement", e);
            }
        }
    }

    private void fixPolypeptide(Transcript transcript, int polypeptideId) throws SQLException {
        try {
            debug("Translating to protein sequence with phase %d", transcript.phase);
            String protein = transcript.translateToProteinSequence();
            debug("Translated sequence: %s", protein);

            if (logger.isEnabledFor(Level.WARN) && !transcript.isPseudo) {
                List<String> warnings = new ArrayList<String>();
                if (protein.length()==0)
                    warnings.add("Translated protein sequence is empty");
                else {
                    if (!protein.startsWith("M"))
                        warnings.add("Translated protein sequence does not start with Methionine");
                    if (!protein.endsWith("*"))
                        warnings.add("Translated protein sequence does not end with a stop codon");
                    int firstStar = protein.indexOf('*');
                    if (firstStar >= 0 && firstStar != protein.length()-1)
                        warnings.add("Translated protein sequence contains an internal stop codon");
                }
                if (!warnings.isEmpty()) {
                    warn(warnings, "%s: problems with translated protein sequence:\n%s", transcript.uniqueName, protein);
                }
            }

            setResidues(polypeptideId, protein);
        }
        catch (TranslationException e) {
            error(e);
        }
    }

    PreparedStatement updateResiduesSt = null;
    private void setResidues(int featureId, String residues) throws SQLException {
        if (updateResiduesSt == null) {
            updateResiduesSt = conn.prepareStatement(
                "update feature set residues = ?, seqlen = ? where feature_id = ?"
            );
        }
        updateResiduesSt.setString(1, residues);
        updateResiduesSt.setInt(2, residues.length());
        updateResiduesSt.setInt(3, featureId);
        int updatedRows = updateResiduesSt.executeUpdate();
        if (updatedRows != 1)
            throw new IllegalArgumentException(String.format("Unexpected result (%d) updating residues for feature %d",
                updatedRows, featureId));
    }
}


class Transcript {
    public int featureId;
    public String uniqueName;
    public boolean isCoding;
    public boolean isPseudo;
    public boolean stopCodonTranslatedAsSelenocysteine;
    public int translationTableId;
    public int phase = 0;
    public String cds;
    public Transcript(int featureId, String uniqueName, boolean isCoding, int translationTableId,
            int phase, boolean isPseudo, boolean stopCodonTranslatedAsSelenocysteine) {
        this.featureId = featureId;
        this.uniqueName = uniqueName;
        this.isCoding = isCoding;
        this.translationTableId = translationTableId;
        this.phase = phase;
        this.isPseudo = isPseudo;
        this.stopCodonTranslatedAsSelenocysteine = stopCodonTranslatedAsSelenocysteine;
    }
    public SortedSet<Exon> exons = new TreeSet<Exon>();
    public void addExon(Exon exon) {
        this.exons.add(exon);
    }

    public String translateToProteinSequence() throws TranslationException {
        if (this.cds.length() < 3 + this.phase) {
            throw new TranslationException(String.format("this '%s' is too short to translate (length=%d, phase=%d)",
                this.uniqueName, this.cds.length(), this.phase));
        }
        return Translator.getTranslator(this.translationTableId).translate(this.cds, this.phase, this.stopCodonTranslatedAsSelenocysteine);
    }
}

class Organism {
    public int organismId;
    public String commonName;
    public int translationTable, mitochondrialTranslationTable;
    public Organism(int organismId, String commonName, int translationTable, int mitochondrialTranslationTable) {
        this.organismId = organismId;
        this.commonName = commonName;
        this.translationTable = translationTable;
        this.mitochondrialTranslationTable = mitochondrialTranslationTable;
    }
}

class Exon implements Comparable<Exon> {
    int featureId;
    String uniqueName;
    int fmin, fmax;
    public Exon(int featureId, String uniqueName, int fmin, int fmax) {
        this.featureId = featureId;
        this.uniqueName = uniqueName;
        this.fmin = fmin;
        this.fmax = fmax;
    }
    public int compareTo(Exon other) {
        return this.fmin - other.fmin;
    }
}