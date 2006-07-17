/*
 * Copyright (c) 2006 Genome Research Limited.
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

import org.genedb.db.dao.DaoFactory;
import org.genedb.db.hibernate3gen.FeatureLoc;
import org.genedb.db.hibernate3gen.Organism;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.utils.ChangeVetoException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/**
 * This class is the main entry point for the new GeneDB data miners. It's designed to be 
 * called from the command-line. It looks for a config. file which specifies which files 
 * to process.
 *
 * Usage: NewRunner common_nane [config_file]
 *
 *
 * @author Adrian Tivey (art)
 */
public class NewRunner implements ApplicationContextAware {

    private static String usage="NewRunner commonname [config file]";
    
    protected static final Log logger = LogFactory.getLog(NewRunner.class);
    
    private FeatureHandler featureHandler = new StandardFeatureHandler();
    
    private DaoFactory daoFactory;
    
    private RunnerConfig runnerConfig;
    
    private RunnerConfigParser runnerConfigParser;
    
    private Map<String, FeatureProcessor> instanceMap = new HashMap<String, FeatureProcessor>();
    
    private Set<String> noInstance = new HashSet<String>();
    
    private FeatureUtils featureUtils;
    
    private Organism organism;

    
    private ApplicationContext applicationContext;

    


    /**
     * This is called once the ApplicationContext has set up all of this 
     * beans properties. It fetches/creates beans which can't be injected 
     * as they depend on command-line args
     */
    public void afterPropertiesSet() {
        //logger.warn("Skipping organism set as not connected to db");
        runnerConfig = runnerConfigParser.getConfig();
        organism = daoFactory.getOrganismDao().findByCommonName(runnerConfig.getOrganismCommonName()).get(0);
        featureHandler.setOrganism(organism);
        featureUtils = new FeatureUtils();
        featureUtils.setDaoFactory(daoFactory);
        featureHandler.setFeatureUtils(featureUtils);
        GoParser gp = new GoParser();
        gp.setDaoFactory(daoFactory);
        featureHandler.setGoParser(gp);
        
        Map<String, String> nomenclatureOptions = runnerConfig.getNomenclatureOptions();
        String nomenclatureHandlerName = nomenclatureOptions.get("beanName");
        if (nomenclatureHandlerName == null) {
            nomenclatureHandlerName = "standardNomenclatureHandler";
        }
        NomenclatureHandler nomenclatureHandler = (NomenclatureHandler)
            this.applicationContext.getBean(nomenclatureHandlerName, NomenclatureHandler.class);
        nomenclatureHandler.setOptions(nomenclatureOptions);
        
        featureHandler.setNomenclatureHandler(nomenclatureHandler);
        
        
        featureHandler.afterPropertiesSet();
    }

    private CharSequence blankString(char c, int size) {
	StringBuilder buf = new StringBuilder(size);
	for (int i =0; i < size; i++) {
	    buf.append(c);
	}
	return buf;
    }



    /**
     * Populate maps based on InterPro result files, GO association files etc
     */
    private void buildCaches() {
	// TODO Auto-generated method stub
	
    }

    /**
     * Call a process_* type method for this feature, based on its type
     * 
     * @param f The feature to dispatch on
     */
    private void despatchOnFeatureType(final Feature f, final org.genedb.db.jpa.Feature parent) {
        FeatureProcessor instance = null;
        String mungedType = f.getType().replaceAll("'","_Prime_");
        mungedType = mungedType.replaceAll("3", "Three");
        mungedType = mungedType.replaceAll("5", "Five");
        // TODO Make 1st letter upper case
        mungedType = mungedType.substring(0,1).toUpperCase()+mungedType.substring(1);
        if (this.instanceMap.containsKey(mungedType)) {
            instance = this.instanceMap.get(mungedType);
        } else {
            if (this.noInstance.contains(mungedType)) {
                return;
            }
            // Try and find a class
            String className = "org.genedb.db.loading.featureProcessors."+mungedType+"_Processor";
            try {
                instance = (FeatureProcessor) Class.forName(className).newInstance();
                instance.setDaoFactory(daoFactory);
                instance.setFeatureUtils(featureUtils);
                instance.setOrganism(organism);
                instance.afterPropertiesSet();
            }
            catch (Exception exp) {
                this.noInstance.add(mungedType);
                logger.warn("No processor for qualifier of type '"+f.getType()+"' (Looked for '"+className+"') Reason '"+exp.getClass().getSimpleName()+"'");
                return;
            }
        }
        instance.process(parent, f);
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
	    //        	if (showContigs) {
	    //        	    System.err.println("Processing contig " + contigName);
	    //        	}
        	
        	
	    SequenceIterator seqIt = SeqIOTools.readEmbl( new BufferedReader(in) ); // TODO - biojava hack

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
	return ret;
    }

    private void postProcess() {
	// 	           addAttribution(getBRNACache());
	//            addUnconditionalLinks(getBRNACache());
	//            writeSPTRLinks(config, outDir, getBRNACache());
	//
	//            getInterPro(getBRNACache(), topDir.getAbsolutePath() + "/interpro", -1);
	//
	//            parseExtSignalP(config);
	//            parseExtTMM(config);
	//            parseExtGPI(config);
	//            parseGOFile(topDir.getAbsolutePath() + "/goAssociation", false, null);
	//            parseSWLinks(outDir, getBRNACache());
	//
	//            setDescriptions(getBRNACache());
	//            generatePfams(topDir, outDir);
	//
	//            getProteinStats(config, outDir);
	//            writeReports(config.getBooleanProperty("mining.writeReports"), organism, outDir);
	// 
	//            finishUp();
	//sessionFactory.close();
    }
    
    /**
     * The core processing loop. Read the config file to find out which EMBL files to read, 
     * and which 'synthetic' features to create
     */
    private void process() {
	long start = new Date().getTime();
        
        this.buildCaches();

        // First process simple files ie simple EMBL files
        List<String> fileNames = this.runnerConfig.getFileNames();
        for (String fileName : fileNames) {
            for (final Sequence seq : this.extractSequencesFromFile(new File(fileName))) {
                
                TransactionTemplate tt = new TransactionTemplate(daoFactory.getTransactionManager());
                tt.execute(
                        new TransactionCallbackWithoutResult() {
                            @Override
                            public void doInTransactionWithoutResult(TransactionStatus status) {
                                processSequence(seq, null, 0);
                            }
                        });

	    }
	}
        
        // Now process synthetics ie config is a mixture of real embl files and synthetic features
        List<Synthetic> synthetics = this.runnerConfig.getSynthetics();
        for (Synthetic synthetic : synthetics) {          
            org.genedb.db.jpa.Feature top = featureUtils.createFeature(synthetic.getSoType(), synthetic.getName(), organism);
            daoFactory.persist(top);
            StringBuilder residues = new StringBuilder();
            
            for (Part part : synthetic.getParts()) {
        	//System.err.println("Synthetic Part='"+synthetic+"'");
		if (part instanceof FeaturePart) {
		    FeaturePart fp = (FeaturePart) part;
		    org.genedb.db.jpa.Feature f = 
			featureUtils.createFeature(fp.getSoType(), fp.getName(), organism);
		    FeatureLoc fl = featureUtils.createLocation(top, f, fp.getOffSet(), fp.getOffSet()+fp.getSize(), fp.getStrand());
		    daoFactory.persist(f);
		    daoFactory.persist(fl);
		    residues.append(blankString('N', fp.getSize()));
		}
		
		if (part instanceof FilePart) {
		    FilePart fp = (FilePart) part;
		    File tmp = new File(fp.getName());
		    List<Sequence> sequences = this.extractSequencesFromFile(tmp);
		    if (sequences.size()>1) {
			logger.fatal("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
			for (Sequence sequence : sequences) {
			    logger.fatal(sequence);
			}
			throw new RuntimeException("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
		    }
		    Sequence seq = sequences.get(0);
		    this.processSequence(seq, top, fp.getOffSet());
		    residues.append(seq.seqString());	    
		}
		
	    }
            top.setResidues(residues.toString());
            daoFactory.getHibernateTemplate().update(top);
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
    private void processSequence(Sequence seq, org.genedb.db.jpa.Feature parent, int offset) {
	try {
	    org.genedb.db.jpa.Feature topLevel = this.featureHandler.processSources(seq);
	    if (parent == null) {
		parent = topLevel;
	    }
	    this.featureHandler.processCDS(seq, parent, offset);
		
	    Iterator featureIterator = seq.features();
	    while (featureIterator.hasNext()) {
		Feature feature = (Feature) featureIterator.next();
		this.despatchOnFeatureType(feature, parent);
	    //parseFeature(seq);
	    }
		
	} catch (ChangeVetoException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	} catch (BioException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	}


    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	this.applicationContext = applicationContext;
    }
    
    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.featureHandler.setDaoFactory(this.daoFactory);
    }
	
    public void setOrganismCommonName(String organismCommonName) {
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
     */
    public static void main (String[] args) {
	
        String organismCommonName = null;
        String loginName = null;
        String configFilePath = null; 
            
        switch (args.length) {
            case 0:
                System.err.println("No organism common name specified\n"+usage);
                System.exit(0);
                break; // To prevent fall-through warning
            case 1:
                organismCommonName = args[0];
                loginName = organismCommonName;
                break;
            case 2:
                organismCommonName = args[0];
                loginName = organismCommonName;
                configFilePath = args[1];
                break;
            case 3:
                organismCommonName = args[0];
                configFilePath = args[1];
                loginName = args[2];
                break;
            default:
                System.err.println("Too many arguments\n"+usage);
            System.exit(0);
        }
        
	// Override properties in Spring config file (using a 
	// BeanFactoryPostProcessor) based on command-line args
	Properties overrideProps = new Properties();
	overrideProps.setProperty("dataSource.username", loginName);
	overrideProps.setProperty("runner.organismCommonName", organismCommonName);
	overrideProps.setProperty("runnerConfigParser.organismCommonName", organismCommonName);
    
    if (configFilePath != null) {
        overrideProps.setProperty("runnerConfigParser.configFilePath", configFilePath);
    }
    
    
	PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);
	
	
	ApplicationContext ctx = new ClassPathXmlApplicationContext(
	        new String[] {"NewRunner.xml"});
	
	NewRunner runner = (NewRunner) ctx.getBean("runner", NewRunner.class);
	runner.process();

    }

}
