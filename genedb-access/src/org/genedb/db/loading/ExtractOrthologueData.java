/*
 * Copyright (c) 2007 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.db.loading;

import static org.genedb.db.loading.EmblQualifiers.QUAL_PSEUDO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeVetoException;
import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.featureProcessors.CDS_Processor;
import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.cv.CvTermRelationship;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.FeatureLoc;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;



/**
 * This class extracts orthologue, paralogue and cluster data from an EMBL file. It's 
 * designed to be called from the command-line. It looks for a config. file which specifies 
 * which files to process.
 *
 * Usage: ExtractOrthologueInfo common_nane [config_file]
 *
 *
 * @author Adrian Tivey (art)
 */
public class ExtractOrthologueData implements ApplicationContextAware {

    private static String usage="ExtractOrthologueInfo commonname [config file]";

    protected static final Log logger = LogFactory.getLog(ExtractOrthologueData.class);

    private RunnerConfig runnerConfig;

    private RunnerConfigParser runnerConfigParser;

    //private FeatureUtils featureUtils;

    private ApplicationContext applicationContext;


    Map<String,String> cdsQualifiers = new HashMap<String,String>();
    
    private OrthologueStorage orthologueStorage = new OrthologueStorage();
    
    private Map<String, List<String>> globalCuratedOrthologues = new HashMap<String, List<String>>();
    
    private Map<String, List<String>> globalCuratedParalogues = new HashMap<String, List<String>>();

    private Map<String, List<String>> globalClusters = new HashMap<String, List<String>>();
    
	private NomenclatureHandler nomenclatureHandler;

	private String organismCommonName;
    

	/**
     * This is called once the ApplicationContext has set up all of this
     * beans properties. It fetches/creates beans which can't be injected
     * as they depend on command-line args
     */
    public void afterPropertiesSet() {
        //logger.warn("Skipping organism set as not connected to db");
        runnerConfig = runnerConfigParser.getConfig();

        //gp.setCvDao(cvDao);

        Map<String, String> nomenclatureOptions = runnerConfig.getNomenclatureOptions();
        String nomenclatureHandlerName = nomenclatureOptions.get("beanName");
        if (nomenclatureHandlerName == null) {
            nomenclatureHandlerName = "standardNomenclatureHandler";
        }
        this.nomenclatureHandler = (NomenclatureHandler)
        this.applicationContext.getBean(nomenclatureHandlerName, NomenclatureHandler.class);
        nomenclatureHandler.setOptions(nomenclatureOptions);

    }



    /**
     * Create a list of Biojava sequences from an EMBL file. It fails fatally if no sequences are found.
     *
     * @param file the file to read in
     * @return the list of sequences, >1 if an EMBL stream
     */
    public List<Sequence> extractSequencesFromFile(File file) {
        if (logger.isInfoEnabled()) {
            logger.info("Parsing file '"+file.getAbsolutePath()+"'");
        }
        List<Sequence> ret = new ArrayList<Sequence>();

        Reader in = null;
        //ArrayList localCache = new ArrayList();
        try {
            in = new FileReader(file);
            //          if (showContigs) {
            //              System.err.println("Processing contig " + contigName);
            //          }

            //logger.info("About to read file");
            SequenceIterator seqIt = SeqIOTools.readEmbl( new BufferedReader(in) ); // TODO - biojava hack
            //logger.info("Just read file");

            while ( seqIt.hasNext() ) {
                ret.add(seqIt.nextSequence());
            }


        } catch (FileNotFoundException exp) {
            System.err.println("Couldn't open input file: " + file);
            exp.printStackTrace();
            System.exit(-1);
        } catch (BioException exp) {
            System.err.println("Couldn't open input file: " + file);
            exp.printStackTrace();
            System.exit(-1);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Shouldn't happen!
                    e.printStackTrace();
                }
            }
        }
        if (ret.size() == 0) {
            logger.fatal("No sequences found in '"+file.getAbsolutePath()+"'");
            System.exit(-1);
        }
        if (ret.size()>1) {
            logger.warn("More than one ("+ret.size()+") sequence found in '"+file.getAbsolutePath()+"'. Not recommended");
        }
        //System.err.println("Returning ret='"+ret+"'");
        return ret;
    }

    private void postProcess() throws IOException {
    	File outFile = new File(organismCommonName+".orthologue.xml");
    	PrintWriter out = new PrintWriter(new FileWriter(outFile));
    	out.println("<relationships>");
    	
    	// Write curated orthologues
    	if (globalCuratedOrthologues.size()>0) {
    		out.println("<orthologues>");
    		for (Map.Entry<String, List<String>> entry : globalCuratedOrthologues.entrySet()) {
    			out.println("\t<orthologue id=\""+entry.getKey()+"\" >");
    			for (String other : entry.getValue()) {
    				out.println("\t\t<other id=\""+other+"\" />");
    			}
    			out.println("\t</orthologue>");
    		}
    		out.println("</orthologues>");
    	}
    	
    	// Write curated paralogues
    	if (globalCuratedParalogues.size()>0) {
    		out.println("<paralogues>");
    		for (Map.Entry<String, List<String>> entry : globalCuratedParalogues.entrySet()) {
    			out.println("\t<paralogue id=\""+entry.getKey()+"\">");
    			for (String id : entry.getValue()) {
    				out.println("\t\t<other id=\""+id               +"\" />");
    			}
    			out.println("\t</paralogue>");
    		}
    		out.println("</paralogues>");
    	}
    	
    	// Write curated paralogues
    	if (globalClusters.size()>0) {
    		out.println("<clusters>");
    		for (Map.Entry<String, List<String>> entry : globalClusters.entrySet()) {
    			out.println("\t<cluster id=\""+entry.getKey()+"\">");
    			for (String id : entry.getValue()) {
    				out.println("\t\t<other id=\""+id               +"\" />");
    			}
    			out.println("\t</cluster>");
    		}
    		out.println("</clusters>");
    	}
    	out.println("</relationships>");
    	out.close();
        //            writeReports(config.getBooleanProperty("mining.writeReports"), organism, outDir);

    }

    /**
     * The core processing loop. Read the config file to find out which EMBL files to read,
     * and which 'synthetic' features to create
     * @throws IOException 
     */
    private void process() throws IOException {
        long start = new Date().getTime();

        //loadCvTerms();
        // First process simple files ie simple EMBL files
        List<String> fileNames = this.runnerConfig.getFileNames();
        for (String fileName : fileNames) {
            final File file = new File(fileName);
            for (final Sequence seq : this.extractSequencesFromFile(file)) {
                //TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
                //tt.execute(
                 //       new TransactionCallbackWithoutResult() {
                 //           @Override
                 //           public void doInTransactionWithoutResult(TransactionStatus status) {
                                processSequence(file, seq, null, 0);
                 //           }
                 //       });

            }
        }

        
        this.postProcess();
        long duration = (new Date().getTime()-start)/1000;
        logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
    }


    /**
     * This method is called once for each sequence. First it examines the source features,
     * then CDSs, then other features
     *
     * @param seq The sequence to parse
     * @param parent The parent object, if reparenting is taking place, or null
     * @param offset The base offset, when reparenting is taking place
     */
    @SuppressWarnings("unchecked")
    private void processSequence(File file, Sequence seq, org.gmod.schema.sequence.Feature parent, int offset) {
    	
   // 	try {
            logger.info("Processing '"+file.getAbsolutePath()+"'");

            // Loop over all features, setting up feature processors and index them by ProcessingPhase
            // Deal with any ProcessingPhase.FIRST on this loop. Note any features we can't process

            Map<ProcessingPhase,List<Feature>> processingStagesFeatureMap =
                new HashMap<ProcessingPhase, List<Feature>>();
            
            
            List<Feature> toRemove = new ArrayList<Feature>();
            Iterator featureIterator = seq.features();
            while (featureIterator.hasNext()) {
                Feature feature = (Feature) featureIterator.next();
                //logger.info("Feature is '"+feature+"'");
                if (feature.getType().equals("CDS")) {
                    processCDS(feature);
                }
            }

//        } catch (BioException exp) {
//            // TODO Auto-generated catch block
//            exp.printStackTrace();
//        }
    }

    private void processCDS(Feature feature) {
		Annotation an = feature.getAnnotation();
		String id = findId(an);
    	List<String> curatedOrthologues = MiningUtils.getProperties("ortholog", an);
    	stripGeneDBPrefix(curatedOrthologues);
    	for (String otherId : curatedOrthologues) {
			GenePair pair = new GenePair(id, otherId);
        	CollectionUtils.addItemToMultiValuedMap(pair.getFirst(), pair.getSecond(), globalCuratedOrthologues);
		}
    	
    	List<String> curatedParalogues = MiningUtils.getProperties("paralog", an);
    	if (curatedParalogues.size()>0) {
    		stripGeneDBPrefix(curatedParalogues);
    		globalCuratedParalogues.put(id, curatedParalogues);
    	}
    		
    	List<String> clusters = MiningUtils.getProperties("cluster", an);
    	for (String string : clusters) {
    		String clusterId = string.substring(0, string.indexOf(' ')-1);
        	CollectionUtils.addItemToMultiValuedMap(clusterId, id, globalClusters);
			//System.err.println(clusterId);
		}

    	//stripGeneDBPrefix(clusters);
    	
    	
	}



	private String findId(Annotation an) {
    	String sysId = null;
        	
    	Names names = this.nomenclatureHandler.findNames(an);
    	sysId = names.getSystematicId();
    	//logger.debug("Looking at systematic id '" + sysId+"'");
    	// TODO Auto-generated method stub
		return sysId;
	}



	private void stripGeneDBPrefix(List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			String entry = list.get(i);
			if (entry.startsWith("GeneDB_")) {
				entry = entry.substring(entry.indexOf(":")+1);
				int semi = entry.indexOf(";");
				if (semi != -1) {
					entry = entry.substring(0, semi);
				}
				list.remove(i);
				list.add(i, entry);
			}
		}
	}



	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setOrganismCommonName(String organismCommonName) {
    	this.organismCommonName  = organismCommonName;
    }


    public void setRunnerConfigParser(RunnerConfigParser runnerConfigParser) {
        this.runnerConfigParser = runnerConfigParser;
    }


    /**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args organism_common_name, [conf file path]
     * @throws IOException 
     */
    public static void main (String[] args) throws IOException {

        String organismCommonName = null;
        String configFilePath = null;

        switch (args.length) {
            case 0:
                System.err.println("No organism common name specified\n"+usage);
                System.exit(0);
                break; // To prevent fall-through warning
            case 1:
                organismCommonName = args[0];
                break;
            case 2:
                organismCommonName = args[0];
                configFilePath = args[1];
                break;
            case 3:
                organismCommonName = args[0];
                configFilePath = args[1];
                break;
            default:
                System.err.println("Too many arguments\n"+usage);
            System.exit(0);
        }

        // Override properties in Spring config file (using a
        // BeanFactoryPostProcessor) based on command-line args
        Properties overrideProps = new Properties();
        overrideProps.setProperty("dataSource.username", "chado");
        overrideProps.setProperty("extractOrtho.organismCommonName", organismCommonName);
        overrideProps.setProperty("runnerConfigParser.organismCommonName", organismCommonName);

        if (configFilePath != null) {
            overrideProps.setProperty("runnerConfigParser.configFilePath", configFilePath);
        }


        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);


        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        ExtractOrthologueData runner = (ExtractOrthologueData) ctx.getBean("extractOrtho", ExtractOrthologueData.class);
        runner.process();

    }

}
