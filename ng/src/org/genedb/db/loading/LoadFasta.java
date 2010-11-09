package org.genedb.db.loading;

import org.gmod.schema.feature.BACEnd;
import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.EST;
import org.gmod.schema.feature.Plasmid;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

/**
 * Recurse through a directory structure, loading any FASTA files we encounter.
 * The actual loading is done by {@link FastaLoader}.
 *
 * @author rh11
 *
 */
public class LoadFasta extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadFasta.class);

    /**
     * Recurse through a directory structure, loading each FASTA file we encounter.
     * Each FASTA file is loaded as a single supercontig (or other type of top-level
     * feature, as specified by the property <code>load.topLevel</load>). Each entry
     * in the file is loaded as a contig.
     * <p>
     * Takes no command-line arguments, but expects to find the system properties
     * <code>load.organismCommonName</code> and <code>load.inputDirectory</code>.
     * Optionally, the property <code>load.fileNamePattern</code> may contain a regular
     * expression aganst which file names are matched. If this property is not specified,
     * we default to <code>.*\.fasta</code>, which matches any file name with the extension
     * <code>.fasta</code>.
     * <p>
     * Other system properties control various options:
     * <ul>
     *   <li> <code>load.overwriteExisting</code> can be set to <b>yes</b>
     *                  (delete the existing copy of the top-level feature before loading),
     *                  or <b>no</b> (skip top-level features that already exist).
     *    <li> <code>load.topLevel</code> may be <code>chromosome</code>, <code>supercontig</code>
     *                  <code>contig</code>, <code>plasmid</code>, or <code>none</code>,
     *                  and determines the type of the top-level feature
     *                  created for each FASTA file. The default is <code>supercontig</code>.
     *                  The special value "none" indicates that no top-level feature should be
     *                  created for the file as a whole; rather each entry represents a top-level
     *                  feature in its own right. This is useful for loading FASTA files containing
     *                  EST or BAC end sequences.
     *    <li> <code>load.entryType</code> may be <code>contig</code>, <code>EST</code>, <code>BAC_end</code>, 
     *                  <code>supercontig</code> or <code>chromosome</code>
     *                  and determines the type of feature that is created for each entry in the file.
     * </ul>
     *
     * @param args ignored
     * @throws MissingPropertyException if a required system property (as detailed above) is missing
     * @throws ParsingException if a FASTA file cannot be parsed
     * @throws IOException if there's a problem opening or reading a file or directory
     */
    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.fasta(?:\\.gz)?");
        String overwriteExisting = getPropertyWithDefault("load.overwriteExisting", "no").toLowerCase();
        String topLevelFeatureType = getPropertyWithDefault("load.topLevel", "supercontig");
        String entryType = getPropertyWithDefault("load.entryType", "contig");

        logger.info(String.format("Options: organismCommonName=%s, inputDirectory=%s, fileNamePattern=%s," +
                   "overwriteExisting=%s, topLevel=%s, entry=%s",
                   organismCommonName, inputDirectory, fileNamePattern, overwriteExisting,
                   topLevelFeatureType, entryType));

        LoadFasta loadFasta = new LoadFasta(organismCommonName, overwriteExisting,
            topLevelFeatureType, entryType);

        loadFasta.processFileOrDirectory(inputDirectory, fileNamePattern);
    }

    private FastaLoader loader;
    private LoadFasta(String organismCommonName, String overwriteExistingString,
            String topLevelFeatureType, String entryType) {
        FastaLoader.OverwriteExisting overwriteExisting;
        if (overwriteExistingString.equals("yes")) {
            overwriteExisting = FastaLoader.OverwriteExisting.YES;
        } else if (overwriteExistingString.equals("no")) {
            overwriteExisting = FastaLoader.OverwriteExisting.NO;
        } else {
            throw new RuntimeException("Unrecognised value for load.overwriteExisting: " + overwriteExistingString);
        }

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = applicationContext.getBean("fastaLoader", FastaLoader.class);
        loader.setOrganismCommonName(organismCommonName);
        loader.setOverwriteExisting(overwriteExisting);

        if (topLevelFeatureType.equals("none")) {
            loader.setTopLevelFeatureClass(null);
        } else if (topLevelFeatureType.equals("chromosome")) {
            loader.setTopLevelFeatureClass(Chromosome.class);
        } else if (topLevelFeatureType.equals("supercontig")) {
            loader.setTopLevelFeatureClass(Supercontig.class);
        } else if (topLevelFeatureType.equals("contig")) {
            loader.setTopLevelFeatureClass(Contig.class);
        } else if (topLevelFeatureType.equals("plasmid")) {
            loader.setTopLevelFeatureClass(Plasmid.class);
        } else {
            throw new RuntimeException(
                String.format("Unrecognised value for load.topLevel: '%s'", topLevelFeatureType));
        }

        if (entryType.equals("contig")) {
            loader.setEntryClass(Contig.class);
        } else if (entryType.equals("EST")) {
            loader.setEntryClass(EST.class);
        } else if (entryType.equals("BAC_end")) {
            loader.setEntryClass(BACEnd.class);
        /* Sometimes the FASTA files have supercontig or chromosome sequences.
         * Example, Schisto version 5
         * nds, 9.11.2010
         */
        } else if (entryType.equals("supercontig")) {          
            loader.setEntryClass(Supercontig.class); 
        } else if (entryType.equals("chromosome")) {          
            loader.setEntryClass(Chromosome.class); 
        } else {
            throw new RuntimeException(
                String.format("Unrecognised value for load.entryType: '%s'", topLevelFeatureType));
        }
}

    @Override
    protected void processFile(File inputFile, Reader reader) throws IOException, ParsingException {
        String fileId = inputFile.getName();
        int lastDotIndex = fileId.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            fileId = fileId.substring(0, lastDotIndex);
        }
        loader.load(fileId, new FastaFile(reader));
    }
}
