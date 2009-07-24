package org.genedb.db.loading;

import org.genedb.db.loading.AGPLoader;

import org.gmod.schema.feature.BACEnd;
import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.EST;
import org.gmod.schema.feature.Plasmid;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.io.InputStream;

public class LoadAGP extends FileProcessor{

    private static final Logger logger = Logger.getLogger(LoadAGP.class);

    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputAGPFileName = getRequiredProperty("load.AGPFile");
        String topLevelFeatureType = getPropertyWithDefault("load.topLevel", "supercontig");
        String entryType = getPropertyWithDefault("load.entryType", "contig");
        String overwriteExisting = getPropertyWithDefault("load.overwriteExisting", "no").toLowerCase();
        Boolean skipTopLevelLoad = hasProperty("load.skipTopLevelLoad");
        
        logger.info(String.format("Options: organismCommonName=%s, AGPFile=%s",
                   organismCommonName, inputAGPFileName));

        LoadAGP loadAGP = new LoadAGP(organismCommonName, overwriteExisting, topLevelFeatureType, entryType, skipTopLevelLoad);
        loadAGP.processFile(inputAGPFileName);
    }


	private AGPLoader loader;
    private LoadAGP(String organismCommonName, String overwriteExistingString, String topLevelFeatureType, String entryType, Boolean skipTopLevelLoad) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = applicationContext.getBean("agpLoader", AGPLoader.class);
        
        AGPLoader.OverwriteExisting overwriteExisting;
        if (overwriteExistingString.equals("yes")) {
            overwriteExisting = AGPLoader.OverwriteExisting.YES;
        } else if (overwriteExistingString.equals("no")) {
            overwriteExisting = AGPLoader.OverwriteExisting.NO;
        } else {
            throw new RuntimeException("Unrecognised value for load.overwriteExisting: " + overwriteExistingString);
        }
        
        loader.setOrganismCommonName(organismCommonName);
        loader.setOverwriteExisting(overwriteExisting);
        loader.setSkipTopLevelLoad(skipTopLevelLoad);
        
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
        } else {
            throw new RuntimeException(
                String.format("Unrecognised value for load.entryType: '%s'", topLevelFeatureType));
        }
    }

    private void processFile(String inputAGPFile) throws IOException, ParsingException {
    	File AGPFile = new File(inputAGPFile);
    	Reader reader = new FileReader(AGPFile); 
    	reader.close();	
    	processFile(AGPFile, reader);
    }

	@Override
	protected void processFile(File inputFile, Reader reader)
			throws IOException, ParsingException {

		InputStream inputStream = new FileInputStream(inputFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        loader.load(new AGPFile(bufferedReader));
	}
}
