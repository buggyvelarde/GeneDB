package org.genedb.db.loading;


import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.EST;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

/**
 * Command-line entry point for loading EMBL files.
 * The class {@link EmblLoader} is used to do the heavy lifting.
 *
 * @author rh11
 *
 */
public class LoadEmbl extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadEmbl.class);
    /**
     * Recurse through a directory structure, loading each EMBL file we encounter.
     * <p>
     * Takes no command-line arguments, but expects to find the system properties
     * <code>load.organismCommonName</code> and <code>load.startingDirectory</code>.
     * Optionally, the property <code>load.fileNamePattern</code> may contain a regular
     * expression aganst which file names are matched. If this property is not specified,
     * we default to <code>.*\.embl(?:\\.gz)?</code>, which matches any file name with
     * the extension <code>.embl</code> or <code>.embl.gz</code>.
     * <p>
     * Other system properties control various options:
     * <ul>
     *   <li> <code>load.overwriteExisting</code> can be set to <b>yes</b>
     *                  (delete the existing copy of the top-level feature before loading),
     *                  <b>no</b> (skip top-level features that already exist),
     *                  or <b>merge</b> (add the features from the file to the existing
     *                  top-level feature, if there is one).
     *    <li> <code>load.topLevel</code> should be <code>chromosome</code>, <code>supercontig</code>
     *                  or <code>contig</code>, and determines the type of the top-level feature
     *                  created for each EMBL file. The default is <code>supercontig</code>.
     *    <li> if <code>load.sloppyControlledCuration</code> is set to <code>true</code> (or
     *                  any value other than <code>false</code>, in fact) then /controlled_curation
     *                  qualifiers will be treated as essentially free-text fields, though
     *                  <code>db_xref</code>s will still be extracted if possible. This is required
     *                  for projects such as Staphylococcus aureus, whose controlled_curation qualifiers
     *                  use a non-standard format.
     * </ul>
     *
     * @param args ignored
     * @throws MissingPropertyException if a required system property (as detailed above) is missing
     * @throws ParsingException if an EMBL file cannot be parsed
     * @throws IOException if there's a problem opening or reading a file or directory
     */
    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.embl(?:\\.gz)?");
        String overwriteExisting = getPropertyWithDefault("load.overwriteExisting", "no").toLowerCase();
        String topLevelFeatureType = getRequiredProperty("load.topLevel");
        boolean sloppyControlledCuration = hasProperty("load.sloppyControlledCuration");
        boolean alwaysSkip = hasProperty("load.alwaysSkip");

        logger.info(String.format("Options: organismCommonName=%s, inputDirectory=%s, fileNamePattern=%s," +
                   "overwriteExisting=%s, topLevel=%s, sloppyControlledCuration=%s",
                   organismCommonName, inputDirectory, fileNamePattern, overwriteExisting,
                   topLevelFeatureType, sloppyControlledCuration));

        LoadEmbl loadEmbl = new LoadEmbl(organismCommonName, overwriteExisting,
            topLevelFeatureType, sloppyControlledCuration);
        if (alwaysSkip) {
            loadEmbl.alwaysSkip();
        }

        loadEmbl.processFileOrDirectory(inputDirectory, fileNamePattern);
    }

    private EmblLoader loader;
    private LoadEmbl(String organismCommonName, String overwriteExistingString, String topLevelFeatureType,
            boolean sloppyControlledCuration) {
        EmblLoader.OverwriteExisting overwriteExisting;
        if (overwriteExistingString.equals("yes")) {
            overwriteExisting = EmblLoader.OverwriteExisting.YES;
        } else if (overwriteExistingString.equals("no")) {
            overwriteExisting = EmblLoader.OverwriteExisting.NO;
        } else if (overwriteExistingString.equals("merge")) {
            overwriteExisting = EmblLoader.OverwriteExisting.MERGE;
        } else {
            throw new RuntimeException("Unrecognised value for load.overwriteExisting: " + overwriteExistingString);
        }

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = (EmblLoader) applicationContext.getBean("emblLoader", EmblLoader.class);
        loader.setOrganismCommonName(organismCommonName);
        loader.setOverwriteExisting(overwriteExisting);
        loader.setSloppyControlledCuration(sloppyControlledCuration);

        if (topLevelFeatureType.equals("chromosome")) {
            loader.setTopLevelFeatureClass(Chromosome.class);
        } else if (topLevelFeatureType.equals("supercontig")) {
            loader.setTopLevelFeatureClass(Supercontig.class);
        } else if (topLevelFeatureType.equals("EST")) {
            loader.setTopLevelFeatureClass(EST.class);
        } else {
            throw new RuntimeException(
                String.format("Unrecognised value for load.topLevel: '%s'", topLevelFeatureType));
        }
    }

    @Override
    protected void processFile(File inputFile, Reader reader) throws IOException, ParsingException {
        EmblFile emblFile = new EmblFile(inputFile, reader);
        loader.load(emblFile);
    }
}
