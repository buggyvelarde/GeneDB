package org.genedb.db.loading;


import org.gmod.schema.feature.BACEnd;
import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.EST;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Plasmid;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
     *                  <code>contig</code>, <code>plasmid</code>, <code>EST</code> or <code>BAC_end</code>,
     *                  and determines the type of the top-level feature
     *                  created for each EMBL file. The default is <code>supercontig</code>.
     *    <li> if <code>load.sloppyControlledCuration</code> is set to <code>true</code> (or
     *                  any value other than <code>false</code>, in fact) then /controlled_curation
     *                  qualifiers will be treated as essentially free-text fields, though
     *                  <code>db_xref</code>s will still be extracted if possible. This is required
     *                  for projects such as Staphylococcus aureus, whose controlled_curation qualifiers
     *                  use a non-standard format.
     *    <li> if <code>load.goTermErrorsAreNotFatal</code> is set to <code>true</code> (or
     *                  any value other than <code>false</code>, in fact) then errors loading /GO
     *                  qualifiers will be reported, but loading will continue. This does <strong>not</strong>
     *                  affect the parsing of /GO qualifiers: parsing errors will still be fatal, as
     *                  usual. It affects situations where there is no GO term with the specified
     *                  accession number in the database, for example.
     *    <li> <code>load.ignoreQualifiers</code> may be set to a comma-separated list of qualifiers
     *                  to ignore. The qualifier name may be prefixed with a feature type, for example
     *                  <code>-Dload.ignoreQualifiers=CDS:similarity</code> would cause all /similarity
     *                  qualifiers on CDS features to be ignored.
     *    <li> <code>load.ignoreFeatures</code> may be set to a comma-separated list of feature types
     *                  to ignore.
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
        
        PropertyConfigurator.configure("resources/classpath/log4j.loader.properties"); 
        
        
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.(embl|tab)(?:\\.gz)?");
        String overwriteExisting = getPropertyWithDefault("load.overwriteExisting", "no").toLowerCase();
        String topLevelFeatureType = getRequiredProperty("load.topLevel");
        boolean sloppyControlledCuration = hasProperty("load.sloppyControlledCuration");
        boolean goTermErrorsAreNotFatal = hasProperty("load.goTermErrorsAreNotFatal");
        boolean quickAndDirty = hasProperty("load.quickAndDirty");
        String ignoreQualifiers = getPropertyWithDefault("load.ignoreQualifiers", null);
        String ignoreFeatures = getPropertyWithDefault("load.ignoreFeatures", null);

        logger.info(String.format("Options: organismCommonName=%s, inputDirectory=%s, fileNamePattern=%s," +
                   "overwriteExisting=%s, topLevel=%s, sloppyControlledCuration=%b, goTermErrorsAreNotFatal=%b," +
                   "ignoreQualifiers=%s, ignoreFeatures=%s",
                   organismCommonName, inputDirectory, fileNamePattern, overwriteExisting,
                   topLevelFeatureType, sloppyControlledCuration, goTermErrorsAreNotFatal,
                   ignoreQualifiers, ignoreFeatures));

        if (quickAndDirty) {
            ((AppenderSkeleton) Logger.getRootLogger().getAppender("stdout")).setThreshold(Level.WARN);
        }
        LoadEmbl loadEmbl = new LoadEmbl(organismCommonName, overwriteExisting,
            topLevelFeatureType, sloppyControlledCuration, goTermErrorsAreNotFatal,
            ignoreQualifiers, ignoreFeatures);
        if (quickAndDirty) {
            loadEmbl.quickAndDirty();
        }

        loadEmbl.processFileOrDirectory(inputDirectory, fileNamePattern);
    }

    private EmblLoader loader;
    private static final Pattern ignoreQualifiersPattern = Pattern.compile("\\G(?:(\\w+):)?(\\w+)(?:,|\\Z)");
    private static final Pattern ignoreFeaturesPattern = Pattern.compile("\\G\\s*(\\S+)\\s*(?:,|\\Z)");
    private LoadEmbl(String organismCommonName, String overwriteExistingString, String topLevelFeatureType,
            boolean sloppyControlledCuration, boolean goTermErrorsAreNotFatal, String ignoreQualifiers,
            String ignoreFeatures) {
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
        
        // set this to true if you want to find out what the actual database connection is.
        boolean debug = false;
        if (debug == true) {
        	org.apache.commons.dbcp.BasicDataSource ds = applicationContext.getBean("dataSource", org.apache.commons.dbcp.BasicDataSource.class);
            logger.info("Connecting to " + ds.getUrl() + " with username " + ds.getUsername());
        }
        
        this.loader = applicationContext.getBean("emblLoader", EmblLoader.class);
        loader.setOrganismCommonName(organismCommonName);
        loader.setOverwriteExisting(overwriteExisting);
        loader.setSloppyControlledCuration(sloppyControlledCuration);
        loader.setGoTermErrorsAreNotFatal(goTermErrorsAreNotFatal);

        if (topLevelFeatureType.equals("chromosome")) {
            loader.setTopLevelFeatureClass(Chromosome.class);
        } else if (topLevelFeatureType.equals("supercontig")) {
            loader.setTopLevelFeatureClass(Supercontig.class);
        } else if (topLevelFeatureType.equals("contig")) {
            loader.setTopLevelFeatureClass(Contig.class);
        } else if (topLevelFeatureType.equals("plasmid")) {
            loader.setTopLevelFeatureClass(Plasmid.class);
        } else if (topLevelFeatureType.equals("EST")) {
            loader.setTopLevelFeatureClass(EST.class);
        } else if (topLevelFeatureType.equals("BAC_end")) {
            loader.setTopLevelFeatureClass(BACEnd.class);
        } else if (topLevelFeatureType.equals("gene")) {
            loader.setTopLevelFeatureClass(Gene.class);
        } else {
            throw new RuntimeException(
                String.format("Unrecognised value for load.topLevel: '%s'", topLevelFeatureType));
        }

        if (ignoreQualifiers != null) {
            Matcher ignoreQualifiersMatcher = ignoreQualifiersPattern.matcher(ignoreQualifiers);
            int end = 0;
            while (ignoreQualifiersMatcher.find()) {
                end = ignoreQualifiersMatcher.end();

                String feature = ignoreQualifiersMatcher.group(1);
                String qualifier = ignoreQualifiersMatcher.group(2);
                if (feature == null) {
                    loader.ignoreQualifier(qualifier);
                } else {
                    loader.ignoreQualifier(qualifier, feature);
                }
            }
            if (end < ignoreQualifiersMatcher.regionEnd()) {
                throw new RuntimeException("Failed to parse load.ignoreQualifiers: " + ignoreQualifiers);
            }
        }

        if (ignoreFeatures != null) {
            Matcher ignoreFeaturesMatcher = ignoreFeaturesPattern.matcher(ignoreFeatures);
            while (ignoreFeaturesMatcher.find()) {
                loader.ignoreFeature(ignoreFeaturesMatcher.group(1));
            }
        }
    }


    private boolean continueOnError = false;

    @Override
    protected void processFile(File inputFile, Reader reader) throws IOException, ParsingException {
        EmblFile emblFile = new EmblFile(inputFile, reader, continueOnError, loader.getOverwriteExisting());
        loader.load(emblFile);
    }

    private void quickAndDirty() {
        alwaysSkip();
        loader.setContinueOnError(true);
        continueOnError = true;
    }
}
