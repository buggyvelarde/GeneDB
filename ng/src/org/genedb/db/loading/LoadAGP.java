package org.genedb.db.loading;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;

/**
 * Retrieves and validates the arguments sent into the AGPLoader and calls the load method 
 * 
 * More documentation:
 * 
 * @author nds
 * @contact path-help@sanger.ac.uk
 */

public class LoadAGP extends FileProcessor{

    private static final Logger logger = Logger.getLogger(LoadAGP.class);
    
    /**
     * Takes no command-line arguments, but expects to find the following system properties that
     * determine how the loader will function.
     * <ul>
     *   <li> <code>load.organismCommonName</code> 
     *   <li> <code>load.mode</code> can be set to <b>1</b> (create a toplevel feature, and add contig & gap locations on it)
     *                  or <b>2</b> (create contig and gap features based on an existing toplevel feature).
     *   <li> <code>load.topLevel</code> tells the loader if the top level feature(s) we need to deal with for this organism is a 
     *          chromosome or supercontig. The default will be a supercontig. 
     *   <li> <code>load.childLevel</code> tells the loader what the child level features are (usually contigs, occasionally supercontigs when
     *          they are assembled into chromosomes). The default will be a contig. 
     *   <li> <code>load.createMissingContigs</code> tells the loader if it should create missing contigs in mode 1 where, in theory, all the contigs
     *   should already be in the database. In some cases, however, like Tcongolense it is ok to create contigs when they cannot be found as there are
     *   no contig features in the database for it anyway. Default is no.</li>        
     *   <li> <code>load.putUnusedContigsInBin</code> tells the loader if it should put any unused child features (in mode 1) in the bin. If set to yes,
     *   it will look for a toplevel feature of the type specified with a name like '%bin%'. Default no.
     *   <li> <code>load.AGPFile</code>
     * </ul>
     * </p>
     *
     * The actual loading is dealt with by AGPLoader. 
     * This can be called from the command line using
     * 
         ant load-agp -Dconfig=localcopy -Dorganism=Tcongolense -Dload.mode=1 -Dload.topLevel=chromosome -Dfile=Tcongolense.agp
     *  
     * @param args ignored
     * @throws MissingPropertyException if a required system property (as detailed above) is missing
     * @throws ParsingException if a AGP file cannot be parsed
     * @throws IOException if there's a problem opening or reading the file 
     */

    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        
        //PropertyConfigurator.configure("resources/classpath/log4j.loader.properties"); 
        
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String mode = getPropertyWithDefault("load.mode", "1");
        String topLevelFeatureType = getPropertyWithDefault("load.topLevel", "supercontig").toLowerCase();
        String childLevelFeatureType = getPropertyWithDefault("load.childLevel", "contig").toLowerCase();
        String createMissingContigs = getPropertyWithDefault("load.createMissingContigs", "no");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.(agp)(?:\\.gz)?");
        String putUnusedContigsInBin = getPropertyWithDefault("load.putUnusedContigsInBin", "no");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        
        logger.info(String.format("Options: organismCommonName=%s, mode=%s, topLevel=%s, inputDirectory=%s", organismCommonName, mode, topLevelFeatureType, childLevelFeatureType, inputDirectory));
      

        LoadAGP loadAGP = new LoadAGP(organismCommonName, mode, topLevelFeatureType, childLevelFeatureType, createMissingContigs, putUnusedContigsInBin);
        loadAGP.processFileOrDirectory(inputDirectory, fileNamePattern);
      
    }


    private AGPLoader loader;

    /**
     * Constructor. Gets the bean from the application context, validates the arguments and calls the AGPLoader.load method with the reader for the AGPFile.
     * @param organismCommonName
     * @param mode
     * @param topLevelFeatureType
     * @param childLevelFeatureType
     * @param inputAGPFileName
     */
    private LoadAGP(String organismCommonName, String mode, String topLevelFeatureType, String childLevelFeatureType, String createMissingContigs, String putUnusedContigsInBin) throws IOException{
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});
        this.loader = applicationContext.getBean("agpLoader", AGPLoader.class);
        
        //Organism name
        loader.setOrganismCommonName(organismCommonName);
        
        //Mode
        if(mode.equals("1") || mode.equals("2")){
            loader.setMode(mode);
        }else{
            throw new RuntimeException(String.format("Unrecognised value for load.mode: %s", mode));
        }
        
        //Top level: Only allows chromosome and supercontig type at the moment. Can add plasmid etc later on
        if (topLevelFeatureType.equals("chromosome")) {
            loader.setTopLevelFeatureClass(Chromosome.class);
        } else if (topLevelFeatureType.equals("supercontig")) {
            loader.setTopLevelFeatureClass(Supercontig.class);
        } else {
            throw new RuntimeException(String.format("Unrecognised value for load.topLevel: %s", topLevelFeatureType));
        }
        
        //Child level: Only allows contig or supercontig for now.
        if (childLevelFeatureType.equals("contig")) {
            loader.setChildLevelFeatureClass(Contig.class);
        } else if (childLevelFeatureType.equals("supercontig")) {
            loader.setChildLevelFeatureClass(Supercontig.class);
        } else {
            throw new RuntimeException(String.format("Unrecognised value for load.childLevel: %s", childLevelFeatureType));
        }
        
        //Check again that both the top level and child level are not set to be the same type of feature!
        if(childLevelFeatureType.equals(topLevelFeatureType)){
            throw new RuntimeException(String.format("Both the child level and the top level feature types are set to: %s", childLevelFeatureType));
        }
        
        //Should the loader create missing contigs 
        if(createMissingContigs.equalsIgnoreCase("yes") || createMissingContigs.equalsIgnoreCase("no")){
            loader.setCreateMissingContigs(createMissingContigs);      
        }else{
            throw new RuntimeException(String.format("Unrecognised value for load.createMissingContigs: %s", createMissingContigs));
        }
      
        //Should the loader put any unused contigs in the bin
        if(putUnusedContigsInBin.equalsIgnoreCase("yes") || putUnusedContigsInBin.equalsIgnoreCase("no")){
            loader.setCreateMissingContigs(createMissingContigs);      
        }else{
            throw new RuntimeException(String.format("Unrecognised value for load.putUnusedContigsInBin: %s", createMissingContigs));
        }

    }


    @Override
    protected void processFile(File inputFile, Reader reader)throws IOException, ParsingException {
         loader.load(new AGPFile(new BufferedReader(reader)));
        
    }

	
}
