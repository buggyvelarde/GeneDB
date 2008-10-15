package org.genedb.db.loading;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;

public class LoadFasta extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadFasta.class);

    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.fasta");
        String overwriteExisting = getPropertyWithDefault("load.overwriteExisting", "no").toLowerCase();
        String topLevelFeatureType = getPropertyWithDefault("load.topLevel", "supercontig");

        logger.info(String.format("Options: organismCommonName=%s, inputDirectory=%s, fileNamePattern=%s," +
                   "overwriteExisting=%s, topLevel=%s",
                   organismCommonName, inputDirectory, fileNamePattern, overwriteExisting,
                   topLevelFeatureType));

        LoadFasta loadFasta = new LoadFasta(organismCommonName, overwriteExisting,
            topLevelFeatureType);

        loadFasta.processFileOrDirectory(inputDirectory, fileNamePattern);

    }

    private FastaLoader loader;
    private LoadFasta(String organismCommonName, String overwriteExistingString, String topLevelFeatureType) {
        FastaLoader.OverwriteExisting overwriteExisting;
        if (overwriteExistingString.equals("yes")) {
            overwriteExisting = FastaLoader.OverwriteExisting.YES;
        } else if (overwriteExistingString.equals("no")) {
            overwriteExisting = FastaLoader.OverwriteExisting.NO;
        } else {
            throw new RuntimeException("Unrecognised value for load.overwriteExisting: " + overwriteExistingString);
        }

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = (FastaLoader) applicationContext.getBean("fastaLoader", FastaLoader.class);
        loader.setOrganismCommonName(organismCommonName);
        loader.setOverwriteExisting(overwriteExisting);

        if (topLevelFeatureType.equals("chromosome")) {
            loader.setTopLevelFeatureClass(Chromosome.class);
        } else if (topLevelFeatureType.equals("supercontig")) {
            loader.setTopLevelFeatureClass(Supercontig.class);
        } else {
            throw new RuntimeException(
                String.format("Unrecognised value for load.topLevel: '%s'", topLevelFeatureType));
        }
    }

    @Override
    protected void processFile(File inputFile) throws IOException, ParsingException {
        String fileId = inputFile.getName();
        int lastDotIndex = fileId.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            fileId = fileId.substring(0, lastDotIndex);
        }
        loader.load(fileId, new FastaFile(inputFile));
    }
}
