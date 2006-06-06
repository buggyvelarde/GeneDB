/*
 * Copyright (c) 2002 Genome Research Limited.
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

/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
package org.genedb.db.loading;

import org.genedb.db.dao.DaoFactory;
import org.genedb.db.dao.FeatureDao;
import org.genedb.db.hibernate.Featureloc;
import org.genedb.db.hibernate.Organism;

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

import java.beans.beancontext.BeanContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/**
 * This class is the main entry point for GeneDB data miners. It's designed to be called from
 * the command-line, or a Makefile.
 *
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
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
    
    private Map<String, Method> methodMap = new HashMap<String, Method>();
    
    private Set<String> noMethod = new HashSet<String>();
    
    private FeatureUtils featureUtils;
    
    public void afterPropertiesSet() {
        //logger.warn("Skipping organism set as not connected to db");
        organism = daoFactory.getOrganismDao().findByCommonName(runnerConfig.getOrganismCommonName()).get(0);
        featureHandler.setOrganism(organism);
        featureUtils = new FeatureUtils();
        featureUtils.setDaoFactory(daoFactory);
        featureHandler.setFeatureUtils(featureUtils);
        String nomenclatureHandlerName = runnerConfig.getNomenclatureHandlerName();
        if (nomenclatureHandlerName == null) {
            nomenclatureHandlerName = "standardNomenclatureHandler";
        }
        NomenclatureHandler nomenclatureHandler = (NomenclatureHandler)
            this.applicationContext.getBean(nomenclatureHandlerName, NomenclatureHandler.class);
        featureHandler.setNomenclatureHandler(nomenclatureHandler);
    }

    
    private void buildCaches() {
	// TODO Auto-generated method stub
	
    }




    private void despatchOnFeatureType(Feature f) {
	Method method = null;
	String mungedType = f.getType().replaceAll("'","_prime_");
	if (this.methodMap.containsKey(mungedType)) {
	    method = this.methodMap.get(mungedType);
	} else {
	    if (!this.noMethod.contains(mungedType)) {
		// Try and find a method
		try {
		    method = this.featureHandler.getClass().getMethod("process_"+mungedType, 
			    new Class[]{Feature.class});
		    if (!this.methodMap.containsKey(mungedType)) {
			this.methodMap.put(mungedType, method);
		    }
		}
		catch (NoSuchMethodException exp) {
		    this.noMethod.add(mungedType);
		    System.err.println("NOTE: No processor for qualifier "+f.getType());
		    return;
		}
	    }
	}
 
	if (method != null) {
	    try {
		// Now use method
		//System.err.println("Trying to dispatch for "+method);
		method.invoke(this, new Object[] {f});
	    } catch (IllegalArgumentException e) {
		e.printStackTrace();
		System.exit(-1);
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
		System.exit(-1);
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
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



    private void process() {
	long start = new Date().getTime();
        
        this.buildCaches();

        // First process simple files ie simple EMBL files
        List<String> fileNames = this.runnerConfig.gatherFileNames();
        for (String fileName : fileNames) {
            for (Sequence seq : this.extractSequencesFromFile(new File(fileName))) {
		this.processSequence(seq);
	    }
	}
        
        // Now process synthetics ie config is a mixture of real embl files and synthetic features
        List<Synthetic> synthetics = this.runnerConfig.getSynthetics();
        for (Synthetic synthetic : synthetics) {          
            org.genedb.db.hibernate.Feature top = featureUtils.createFeature(synthetic.getSoType(), synthetic.getName(), organism);
            daoFactory.persist(top);
            StringBuilder residues = new StringBuilder();
            
            for (Part part : synthetic.getParts()) {
        	System.err.println("Synthetic Part='"+synthetic+"'");
		if (part instanceof FeaturePart) {
		    FeaturePart fp = (FeaturePart) part;
		    org.genedb.db.hibernate.Feature f = 
			featureUtils.createFeature(fp.getSoType(), fp.getName(), organism);
		    Featureloc fl = featureUtils.createLocation(top, f, fp.getOffSet(), fp.getOffSet()+fp.getSize(), fp.getStrand());
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
		    // TODO Create some kind of new sequence to cope with translation
		    
		}
		
	    }
            top.setResidues(residues.toString());
            daoFactory.getHibernateTemplate().update(top);
        }
        
        this.postProcess();

        if (logger.isInfoEnabled()) {
            long duration = (new Date().getTime()-start)/1000;
            logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
        }
    }

    private CharSequence blankString(char c, int size) {
	StringBuilder buf = new StringBuilder(size);
	for (int i =0; i < size; i++) {
	    buf.append(c);
	}
	return buf;
    }

    private Organism organism;

    private ApplicationContext applicationContext;
    
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

//	    while (seqIt.hasNext()) {
//		seq = seqIt.nextSequence();
//		this.processSequence(seq, offSet, parent);
//	    }
        	
        
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

    private void processSequence(Sequence seq) {
	this.processSequence(seq, null, 0);
    }
    
    @SuppressWarnings("unchecked")
    private void processSequence(Sequence seq, org.genedb.db.hibernate.Feature parent, int offset) {
	// TODO Ignores new parent
	try {
	    org.genedb.db.hibernate.Feature topLevel = this.featureHandler.processSources(seq);
	    if (parent == null) {
		parent = topLevel;
	    }
	    this.featureHandler.processCDS(seq, parent, offset);
		
	    //parseFeature(seq);
		
	} catch (ChangeVetoException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	} catch (BioException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	}


    }
    
    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.featureHandler.setDaoFactory(this.daoFactory);
    }
    
    public void setOrganismCommonName(String organismCommonName) {
    }
	
    public static void main (String[] args) {
	
        if ( args.length == 0) {
            System.err.println("No organism common name specified\n"+usage);
            System.exit(0);
        }
        if (args.length > 2) {
            System.err.println("Too many arguments\n"+usage);
            System.exit(0);
        }
	String organismCommonName = args[0];
        
	// Override properties in Spring config file (using a 
	// BeanFactoryPostProcessor) based on command-line args
	Properties overrideProps = new Properties();
	overrideProps.setProperty("dataSource.username", organismCommonName);
	overrideProps.setProperty("runner.organismCommonName", organismCommonName);
	overrideProps.setProperty("runnerConfig.organismCommonName", organismCommonName);
	if (args.length > 1) {
	    overrideProps.setProperty("runnerConfig.configFilePath", args[1]);
	}      
	PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);
	
	
	ApplicationContext ctx = new ClassPathXmlApplicationContext(
	        new String[] {"NewRunner.xml"});
	
	NewRunner runner = (NewRunner) ctx.getBean("runner", NewRunner.class);
	runner.process();

    }


    public void setRunnerConfig(RunnerConfig runnerConfig) {
        this.runnerConfig = runnerConfig;
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	this.applicationContext = applicationContext;
    }

}
