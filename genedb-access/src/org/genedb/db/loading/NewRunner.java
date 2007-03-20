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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
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

    private FeatureHandler featureHandler;

    private RunnerConfig runnerConfig;

    private RunnerConfigParser runnerConfigParser;

    private Set<String> noInstance = new HashSet<String>();

    private FeatureUtils featureUtils;

    private Organism organism;

    private ApplicationContext applicationContext;

    private SequenceDao sequenceDao;

    private OrganismDao organismDao;

    private CvDao cvDao;
    
    private PubDao pubDao;

    private GeneralDao generalDao;

    private Map<String, FeatureProcessor> qualifierHandlerMap;
    
    private HibernateTransactionManager hibernateTransactionManager;


    public void setHibernateTransactionManager(
			HibernateTransactionManager hibernateTransactionManager) {
		this.hibernateTransactionManager = hibernateTransactionManager;
	}

	/**
     * This is called once the ApplicationContext has set up all of this
     * beans properties. It fetches/creates beans which can't be injected
     * as they depend on command-line args
     */
    public void afterPropertiesSet() {
        //logger.warn("Skipping organism set as not connected to db");
        runnerConfig = runnerConfigParser.getConfig();
        organism = organismDao.getOrganismByCommonName(runnerConfig.getOrganismCommonName());

        Map<String, String> featureHandlerOptions = runnerConfig.getFeatureHandlerOptions();
        String featureHandlerName = featureHandlerOptions.get("beanName");
        if (featureHandlerName == null) {
            featureHandlerName = "fullLengthSourceFeatureHandler";
        }
        featureHandler = (FeatureHandler)
        this.applicationContext.getBean(featureHandlerName, FeatureHandler.class);
        featureHandler.setOptions(featureHandlerOptions);
        featureHandler.setOrganism(organism);
        featureHandler.setCvDao(cvDao);
        featureHandler.setGeneralDao(generalDao);
        featureHandler.setSequenceDao(sequenceDao);
        featureHandler.setPubDao(pubDao);


        featureHandler.afterPropertiesSet();
        
        
        featureUtils = new FeatureUtils();
        featureUtils.setCvDao(cvDao);
        featureUtils.setSequenceDao(sequenceDao);
        featureUtils.setPubDao(pubDao);
        featureUtils.afterPropertiesSet();


        featureHandler.setFeatureUtils(featureUtils);
        featureHandler.afterPropertiesSet();

        //gp.setCvDao(cvDao);

        Map<String, String> nomenclatureOptions = runnerConfig.getNomenclatureOptions();
        String nomenclatureHandlerName = nomenclatureOptions.get("beanName");
        if (nomenclatureHandlerName == null) {
            nomenclatureHandlerName = "standardNomenclatureHandler";
        }
        NomenclatureHandler nomenclatureHandler = (NomenclatureHandler)
        this.applicationContext.getBean(nomenclatureHandlerName, NomenclatureHandler.class);
        nomenclatureHandler.setOptions(nomenclatureOptions);

        CDS_Processor cdsProcessor =
            (CDS_Processor) this.applicationContext.getBean("cdsProcessor", CDS_Processor.class);
        cdsProcessor.setNomenclatureHandler(nomenclatureHandler);
        cdsProcessor.afterPropertiesSet();

        for (FeatureProcessor fp : qualifierHandlerMap.values()) {
            fp.setOrganism(organism);
            fp.setFeatureUtils(featureUtils);
            fp.afterPropertiesSet();
        }
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
    private void despatchOnFeatureType(final FeatureProcessor fp, final Feature f, Session session, final org.gmod.schema.sequence.Feature parent, final int offset) {
    //	TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
     //   tt.execute(
              //  new TransactionCallbackWithoutResult() {
                 //   @Override
                 //   public void doInTransactionWithoutResult(TransactionStatus status) {
    					Transaction transaction = session.beginTransaction();
                        fp.process(parent, f, offset);
                        transaction.commit();
                 //   }
               // });
    }


    private FeatureProcessor findFeatureProcessor(final Feature f) {
        String type = f.getType();
        FeatureProcessor instance = qualifierHandlerMap.get(type);
        if (instance == null) {
            if (!this.noInstance.contains(type)) {
                this.noInstance.add(type);
                // TODO Seriousness should be policy
                //logger.warn("No processor for qualifier of type '"+type+"' configured.");
                throw new RuntimeException("No processor for qualifier of type '"+type+"' configured.");
            }
        }
        return instance;
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

    private void postProcess() {
        //                 addAttribution(getBRNACache());
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

/*
        // Now process synthetics ie config is a mixture of real embl files and synthetic features
        List<Synthetic> synthetics = this.runnerConfig.getSynthetics();
        for (Synthetic synthetic : synthetics) {
            final org.gmod.schema.sequence.Feature top = featureUtils.createFeature(synthetic.getSoType(), synthetic.getName(), organism);
            sequenceDao.persist(top);
            StringBuilder residues = new StringBuilder();

            for (Part part : synthetic.getParts()) {
                //System.err.println("Synthetic Part='"+synthetic+"'");
                if (part instanceof FeaturePart) {
                    FeaturePart fp = (FeaturePart) part;
                    org.gmod.schema.sequence.Feature f =
                        featureUtils.createFeature(fp.getSoType(), fp.getName(), organism);
                    FeatureLoc fl = featureUtils.createLocation(top, f, fp.getOffSet(), fp.getOffSet()+fp.getSize(), fp.getStrand());
                    sequenceDao.persist(f);
                    sequenceDao.persist(fl);
                    residues.append(blankString('N', fp.getSize()));
                }

                if (part instanceof FilePart) {
                    final FilePart fp = (FilePart) part;
                    File tmp = new File(fp.getName());
                    List<Sequence> sequences = this.extractSequencesFromFile(tmp);
                    if (sequences.size()>1) {
                        logger.fatal("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
                        for (Sequence sequence : sequences) {
                            logger.fatal(sequence);
                        }
                        throw new RuntimeException("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
                    }
                    final Sequence seq = sequences.get(0);

//                  TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
//                  tt.execute(
//                  new TransactionCallbackWithoutResult() {
//                  @Override
//                  public void doInTransactionWithoutResult(TransactionStatus status) {
                    processSequence(tmp, seq, top, fp.getOffSet());
//                  }
//                  });

                    residues.append(seq.seqString());
                }

            }
            top.setResidues(residues.toString().getBytes());
            //sequenceDao.update(top); // FIXME - Change to merge, or is it OK anyway?
        }
*/

        
        /*
         * new code below this to try and persist the residues into the database
         */
        
		List<Synthetic> synthetics = this.runnerConfig.getSynthetics();
        
        for (Synthetic synthetic : synthetics) {
        	List <FeaturePart> featurePartList = new ArrayList<FeaturePart>();
        	List <FilePart> fpList = new ArrayList<FilePart>();
        	
        	final org.gmod.schema.sequence.Feature top = featureUtils.createFeature(synthetic.getSoType(), synthetic.getName(), organism);
            StringBuilder residues = new StringBuilder();

            for (Part part : synthetic.getParts()) {
                if (part instanceof FeaturePart) {
                    FeaturePart fp = (FeaturePart) part;
                    residues.append(blankString('N', fp.getSize()));
                    featurePartList.add(fp);
                }

                if (part instanceof FilePart) {
                    final FilePart fp = (FilePart) part;
                    File tmp = new File(fp.getName());
                    List<Sequence> sequences = this.extractSequencesFromFile(tmp);
                    if (sequences.size()>1) {
                        logger.fatal("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
                        for (Sequence sequence : sequences) {
                            logger.fatal(sequence);
                        }
                        throw new RuntimeException("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
                    }
                    final Sequence seq = sequences.get(0);
                    //processSequence(tmp, seq, top, fp.getOffSet());
                    residues.append(seq.seqString());
                    fpList.add(fp);
                }

            }
           
            top.setResidues(residues.toString().getBytes());
            sequenceDao.persist(top);
            
            for (FeaturePart fp : featurePartList) {
            	org.gmod.schema.sequence.Feature f = 
                    featureUtils.createFeature(fp.getSoType(), fp.getName(), organism);
                FeatureLoc fl = featureUtils.createLocation(top, f, fp.getOffSet(), fp.getOffSet()+fp.getSize(), fp.getStrand());
                sequenceDao.persist(f);
                sequenceDao.persist(fl);
			}
            
            for (final FilePart fp : fpList) {
            	final File tmp = new File(fp.getName());
                List<Sequence> sequences = this.extractSequencesFromFile(tmp);
                if (sequences.size()>1) {
                    logger.fatal("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
                    for (Sequence sequence : sequences) {
                        logger.fatal(sequence);
                    }
                    throw new RuntimeException("Can't use an EMBL stream '"+tmp.getAbsolutePath()+"' in a synthetic");
                }
                final Sequence seq = sequences.get(0);
            	//processSequence(tmp, seq, top, fp.getOffSet());
              //TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
              //tt.execute(
              //new TransactionCallbackWithoutResult() {
              //@Override
              //public void doInTransactionWithoutResult(TransactionStatus status) {
                processSequence(tmp, seq, top, fp.getOffSet());
              //}
              //});
			}
            
        }  
        /*
         * new code ends
         */
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
    	Session session = hibernateTransactionManager.getSessionFactory().openSession();
    	try {
            org.gmod.schema.sequence.Feature topLevel = this.featureHandler.process(file, seq);
            logger.info("Processing '"+file.getAbsolutePath()+"'");
            if (parent == null) {
                parent = topLevel;
                // Mark all top-level features
                this.featureUtils.markTopLevelFeature(topLevel);
            }

            // Loop over all features, setting up feature processors and index them by ProcessingPhase
            // Deal with any ProcessingPhase.FIRST on this loop. Note any features we can't process

            Map<ProcessingPhase,List<Feature>> processingStagesFeatureMap =
                new HashMap<ProcessingPhase, List<Feature>>();

            List<Feature> toRemove = new ArrayList<Feature>();
            Iterator featureIterator = seq.features();
            while (featureIterator.hasNext()) {
                Feature feature = (Feature) featureIterator.next();
                logger.info("Feature is '"+feature+"'");
                //System.err.println(feature);
                FeatureProcessor fp = findFeatureProcessor(feature);
                if (fp != null) {
                    ProcessingPhase pp = fp.getProcessingPhase();
                    if (pp == ProcessingPhase.FIRST) {
                        this.despatchOnFeatureType(fp, feature, session, parent, offset);
                    }
                    CollectionUtils.addItemToMultiValuedMap(pp, feature, processingStagesFeatureMap);
                } else {
                    toRemove.add(feature);
                }
            }

            // Remove features that we dealt with in first pass, or can't deal with
            // TODO Bother deleting as not looping thru' them - use index instead
            List<Feature> tmp = processingStagesFeatureMap.get(ProcessingPhase.FIRST);
            if (tmp != null) {
            	
                for (Feature feature : tmp) {
                        seq.removeFeature(feature);
                }
            }
            processingStagesFeatureMap.put(ProcessingPhase.FIRST, Collections.EMPTY_LIST); // TO Keep this even if remove above & below
        	logger.info("Removing features handled by featureProcessors at processing phase '"+ProcessingPhase.FIRST+"'");
            for (Feature feature : toRemove) {
                seq.removeFeature(feature);
            }


            // Loop through each processing phase, and use index to process features
            // then delete them
            for (ProcessingPhase pp : ProcessingPhase.values()) {
                if (pp == ProcessingPhase.FIRST) {
                    continue;
                }
                List<Feature> features = processingStagesFeatureMap.get(pp);
                if (features != null) {
                    for (Feature feature : features) {
                        FeatureProcessor fp = findFeatureProcessor(feature);
                        this.despatchOnFeatureType(fp, feature, session, parent, offset);
                    }
                    if (pp == ProcessingPhase.LAST) {
                        continue; // Rely on GC to tidy up
                    }
                	logger.info("Removing features handled by featureProcessors at processing phase '"+pp+"'");
                    for (Feature feature : features) {
                        seq.removeFeature(feature);
                    }
                }
                processingStagesFeatureMap.put(ProcessingPhase.FIRST, Collections.EMPTY_LIST);
            }

        } catch (ChangeVetoException exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        } catch (BioException exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }
        session.close();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public void setQualifierHandlerMap(
            Map<String, FeatureProcessor> qualifierHandlerMap) {
        this.qualifierHandlerMap = qualifierHandlerMap;
    }

	public void setPubDao(PubDao pubDao) {
		this.pubDao = pubDao;
	}


//	private void loadCvTerms() {
//        /* Load the cvterms from the file. 
//         * 
//         */
//        
//        try {
//	        BufferedReader in = new BufferedReader(new FileReader("/nfs/team81/cp2/Scripts/cvterms"));
//	        String str;
//	        while (((str = in.readLine()) != null)) {
//	        	String sections[] = str.split("\t");
//	        	CvTerm cvTerm = null;
//	        	cvTerm = this.cvDao.getCvTermByNameAndCvName(sections[1], "CC_%");
//	        	if (cvTerm == null) {
//		        	cvTerm = new CvTerm();
//		        	Db db = generalDao.getDbByName("CCGEN");
//		        	String accession = "CCGEN_" + sections[1];
//		        	DbXRef dbXRef = null;
//		        	dbXRef = generalDao.getDbXRefByDbAndAcc(db,accession);
//		        	if (dbXRef == null) { 
//		        		dbXRef = new DbXRef();
//		        		dbXRef.setDb(db);
//		        		dbXRef.setAccession(accession);
//		        		dbXRef.setVersion("1");
//		        		generalDao.persist(dbXRef);
//		        	}
//	    			Cv cv = null;
//	    			String name = "CC_" + sections[0];
//	    			cv = this.cvDao.getCvByName(name).get(0);
//	    			if (cv == null) {
//	    				cv = new Cv();
//	    				cv.setName(name);
//	    				this.cvDao.persist(cv);
//	    			} else {
//	    				cvTerm.setCv(cv);
//	    			}
//	    			cvTerm.setDbXRef(dbXRef);
//	    			cvTerm.setName(sections[1]);
//	    			cvTerm.setDefinition(sections[1]);
//	    			this.cvDao.persist(cvTerm);
//	        	}
//	        }
//	        in.close();
//	        
//	    } catch (IOException e) {
//	    }
//	    
//	    try {
//	        BufferedReader in = new BufferedReader(new FileReader("/nfs/pathdb/prod/data/input/linksManager/RILEY.dat"));
//	        String str;
//	        String parent = null;
//	        CvTerm parentId = null;
//	        String child = null;
//	        CvTerm childId = null;
//	        Cv CV_RELATION = cvDao.getCvByName("relationship").get(0);
//	        CvTerm REL_PART_OF = cvDao.getCvTermByNameInCv("part_of", CV_RELATION).get(0);
//	        while ((str = in.readLine()) != null) {
//	        	String sections[] = str.split("\t");
//	        	CvTerm cvTerm = null;
//	        	cvTerm = this.cvDao.getCvTermByNameAndCvName(sections[2], "RILEY");
//	        	if(cvTerm == null){
//		        	cvTerm = new CvTerm();
//	        		Db db = generalDao.getDbByName("RILEY");
//					DbXRef dbXRef = new DbXRef();
//					dbXRef.setDb(db);
//					String accession = sections[1];
//					dbXRef.setAccession(accession);
//					dbXRef.setVersion("1");
//	    			generalDao.persist(dbXRef);
//	    			Cv cv = null;
//	    			cv = this.cvDao.getCvByName("RILEY").get(0);
//					cvTerm.setCv(cv);
//	    			cvTerm.setDbXRef(dbXRef);
//	    			cvTerm.setName(sections[2]);
//	    			cvTerm.setDefinition(sections[2]);
//	    			this.cvDao.persist(cvTerm);
//	
//		        	if(!sections[1].startsWith("0.0")) {
//		        		String temp[] = sections[1].split("\\.");
//		        		if(parent != null){
//			        		if(parent.equals(temp[0])){
//			        			if (child.equals(temp[1])){
//			        				CvTermRelationship ctr = new CvTermRelationship(cvTerm,childId,REL_PART_OF);
//			        				this.cvDao.persist(ctr);
//			        			}else {
//			        				child = temp[1];
//			        				childId = cvTerm;
//			        				CvTermRelationship ctr = new CvTermRelationship(cvTerm,parentId,REL_PART_OF);
//			        				this.cvDao.persist(ctr);
//			        			}
//			        		} else {
//			        			parent = temp[0];
//			        			child = temp[1];
//			        			parentId = cvTerm;
//			        		}
//		        		} else {
//		        			parent = temp[0];
//		        			child = temp[1];
//		        			parentId = cvTerm;
//		        		}
//		        	}
//	        	}
//	        }
//	        in.close();
//	    } catch (IOException e) {
//	    	
//	    }
//	 }

}
