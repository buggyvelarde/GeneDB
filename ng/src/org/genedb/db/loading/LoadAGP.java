package org.genedb.db.loading;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Supercontig;

import org.apache.log4j.Logger;
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
 * @author 
 *
 */

//We extend FileProcessor here even though we do not need any recursive file processing
//action since we find the getRequiredProperty methods useful to extract system properties.

public class LoadAGP extends FileProcessor{

    private static final Logger logger = Logger.getLogger(LoadAGP.class);
    
    /**
     * Takes no command-line arguments, but expects to find the following system properties that
     * determine how the loader will function.
     * <ul>
     *   <li> <code>load.organismCommonName</code> 
     *   <li> <code>load.mode</code> can be set to <b>1</b> (load all the contigs and gaps)
     *                  or <b>2</b> (load the top level features, not the contigs and gaps).
     *   <li> <code>load.topLevel</code> tells the loader if the top level feature(s) we need to deal with for this organism is a 
     *          chromosome or supercontig. The default will be a chromosome. 
     *   <li> <code>load.createMissingContigs</code> tells the loader if it should create missing contigs in mode 1 where, in theory, all the contigs
     *   should already be in the database. In some cases, however, like Tcongolense it is ok to create contigs when they cannot be found as there are
     *   no contig features in the database for it anyway. </li>        
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
        
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String mode = getPropertyWithDefault("load.mode", "1");
        String topLevelFeatureType = getPropertyWithDefault("load.topLevel", "chromosome").toLowerCase();
        String createMissingContigs = getPropertyWithDefault("load.createMissingContigs", "no");
        String inputAGPFileName = getRequiredProperty("load.AGPFile");
        
        logger.info(String.format("Options: organismCommonName=%s, mode=%s, topLevel=%s, AGPFile=%s", organismCommonName, mode, topLevelFeatureType, inputAGPFileName));

        LoadAGP loadAGP = new LoadAGP(organismCommonName, mode, topLevelFeatureType, createMissingContigs, inputAGPFileName);
      
    }


    private AGPLoader loader;

    /**
     * Constructor. Gets the bean from the application context, validates the arguments and calls the AGPLoader.load method with the reader for the AGPFile.
     * @param organismCommonName
     * @param mode
     * @param topLevelFeatureType
     * @param inputAGPFileName
     */
    private LoadAGP(String organismCommonName, String mode, String topLevelFeatureType, String createMissingContigs, String inputAGPFileName) throws IOException{
        
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
        
        //Should the loader create contigs in mode 1 (when it expects to find all the contigs in the database)
        if(createMissingContigs.equalsIgnoreCase("yes") || createMissingContigs.equalsIgnoreCase("no")){
            loader.setCreateMissingContigs(createMissingContigs);      
        }else{
            throw new RuntimeException(String.format("Unrecognised value for load.createMissingContigs: %s", createMissingContigs));
        }
        
        //Call the load method with a reader for the AGP file
        File AGPFile = new File(inputAGPFileName);
        InputStream inputStream = new FileInputStream(AGPFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        loader.load(new AGPFile(bufferedReader));
      

    }


    @Override
    protected void processFile(File inputFile, Reader reader)throws IOException, ParsingException {
        //We have to implement this because we extend FileProcessor  
    }

	
}
