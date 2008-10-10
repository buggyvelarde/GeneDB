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

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.featureProcessors.CDS_Processor;

import org.gmod.schema.mapped.Organism;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.utils.ChangeVetoException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

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



/**
 * This class is the main entry point for the new GeneDB data miners.
 * It's designed to be called from the command-line. It looks for a
 * config. file which specifies which files to process.
 *
 * Usage: NewRunner common_nane [config_file]
 *
 *
 * @author Adrian Tivey (art)
 */
@Transactional
public class NewRunner3 implements NewRunner3I {

    private static String usage="NewRunner commonname [config file]";

    protected static final Log logger = LogFactory.getLog(NewRunner3.class);

    private FeatureHandler featureHandler;

    public void setFeatureHandler(FeatureHandler featureHandler) {
        this.featureHandler = featureHandler;
    }

    private RunnerConfig runnerConfig;

    //private RunnerConfigParser runnerConfigParser;

    private Set<String> noInstance = new HashSet<String>();

    private FeatureUtils featureUtils;

    private Organism organism;

    private SequenceDao sequenceDao;

    private OrganismDao organismDao;

    private CvDao cvDao;

    private PubDao pubDao;

    private GeneralDao generalDao;

    private Map<String, FeatureProcessor> qualifierHandlerMap;

    Map<String,String> cdsQualifiers = new HashMap<String,String>();

    private Set<String> handeledQualifiers = new HashSet<String>();

    private OrganismCommonNameHolder organismCommonNameHolder;

    private InputSources inputSources;

    private SessionFactory sessionFactory;

    //private OrthologueStorage orthologueStorage = new OrthologueStorage();

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * This is called once the ApplicationContext has set up all of this
     * beans properties. It fetches/creates beans which can't be injected
     * as they depend on command-line args
     */
    public void afterPropertiesSet() {
        //logger.warn("Skipping organism set as not connected to db");
        // runnerConfig; Inject, pull?
        organism = organismDao.getOrganismByCommonName(organismCommonNameHolder.getOrganismCommonName());

        logger.warn("Got organism");

        featureUtils = new FeatureUtils();
        featureUtils.setCvDao(cvDao);
        featureUtils.setSequenceDao(sequenceDao);
        featureUtils.setPubDao(pubDao);
        featureUtils.setGeneralDao(generalDao);
        //featureUtils.afterPropertiesSet();

        logger.warn("Set up featureUtils");

        //gp.setCvDao(cvDao);



        for (FeatureProcessor fp : qualifierHandlerMap.values()) {
            //fp.setOrganism(organism);
            //fp.setFeatureUtils(featureUtils);
            //fp.afterPropertiesSet();
        }
    }

    /**
     * Populate maps based on InterPro result files, GO association files etc
     */
    private void buildCaches() {
        handeledQualifiers.add("EC_number");
        handeledQualifiers.add("primary_name");
        handeledQualifiers.add("systematic_id");
        handeledQualifiers.add("previous_systematic_id");
        handeledQualifiers.add("product");
        handeledQualifiers.add("db_xref");
        handeledQualifiers.add("similarity");
        handeledQualifiers.add("temporary_systematic_id");
        handeledQualifiers.add("fasta_file");
        handeledQualifiers.add("blast_file");
        handeledQualifiers.add("blastn_file");
        handeledQualifiers.add("blastpgo_file");
        handeledQualifiers.add("blastp_file");
        handeledQualifiers.add("blastx_file");
        handeledQualifiers.add("fastax_file");
        handeledQualifiers.add("tblastn_file");
        handeledQualifiers.add("tblastx_file");
        handeledQualifiers.add("literature");
        handeledQualifiers.add("curation");
        handeledQualifiers.add("private");
        handeledQualifiers.add("pseudo");
        handeledQualifiers.add("psu_db_xref");
        handeledQualifiers.add("note");
    }



    /**
     * Call a process_* type method for this feature, based on its type
     *
     * @param f The feature to dispatch on
     */
    private void despatchOnFeatureType(final FeatureProcessor fp, final Feature f, final org.gmod.schema.mapped.Feature parent, final int offset) {
        fp.process(parent, f, offset);
    }


    private FeatureProcessor findFeatureProcessor(final Feature f) {
        String type = f.getType();
        FeatureProcessor instance = qualifierHandlerMap.get(type);
        if (instance == null) {
            if (!this.noInstance.contains(type)) {
                this.noInstance.add(type);
                // TODO Seriousness should be policy
                logger.warn("No processor for qualifier of type '"+type+"' configured.");
                //throw new RuntimeException("No processor for qualifier of type '"+type+"' configured.");
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
    public void process() {
        long start = new Date().getTime();

        System.err.println("*** Building caches");
        this.buildCaches();
        System.err.println("*** Done building caches");
        //loadCvTerms();
        // First process simple files ie simple EMBL files
        List<File> files = inputSources.inputFiles();
        for (File file : files) {
            System.err.println("*** Looking at '"+file.getAbsolutePath()+"'");
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
        reportUnhandledQualifiers();
        //reportUnknownRileyClass();
        long duration = (new Date().getTime()-start)/1000;
        logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
    }

//    private void reportUnknownRileyClass() {
//        logger.warn("Unknown Riley class are...");
//        List<String> rileys = qualifierHandlerMap.get("CDS").getUnknownRileyClass();
//        for (String riley : rileys) {
//            logger.warn(riley);
//        }
//    }

    private void reportUnhandledQualifiers() {
        Map<String, Boolean> merged = new HashMap<String, Boolean>();
        for (FeatureProcessor processor : qualifierHandlerMap.values()) {
            merged.putAll(processor.getQualifierHandlingStatus());
        }
        List<String> keys = new ArrayList<String>();
        keys.addAll(merged.keySet());
        Collections.sort(keys);

        StringBuilder out = new StringBuilder();
        out.append("\n\nHandled qualifiers\n");
        for (String key : keys) {
            if (merged.get(key) == Boolean.TRUE) {
                out.append(key);
                out.append('\n');
            }
        }
        out.append("\n\nUnhandled qualifiers\n");
        for (String key : keys) {
            if (merged.get(key) == Boolean.FALSE) {
                out.append(key);
                out.append('\n');
            }
        }
        logger.warn(out.toString());


//      Set temp = cdsQualifiers.keySet();
//        for (Object key : temp) {
//          System.out.println(key + " " + cdsQualifiers.get(key));
//      }
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
    @Transactional
    private void processSequence(File file, Sequence seq, org.gmod.schema.mapped.Feature parent, int offset) {
        //Session session = hibernateTransactionManager.getSessionFactory().openSession();

        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);

        try {
            org.gmod.schema.mapped.Feature topLevel = this.featureHandler.process(file, seq);
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
                if (feature.getType().equals("CDS")) {
                    Set keys = feature.getAnnotation().keys();
                    for (Object key : keys) {
                        if(handeledQualifiers.contains(key)){
                            cdsQualifiers.put((String) key, "Handeled");
                        } else {
                            cdsQualifiers.put((String) key, "Un-Handeled");
                        }
                    }
                }
                if (fp != null) {
                    ProcessingPhase pp = fp.getProcessingPhase();
                    if (pp == ProcessingPhase.FIRST) {
                        //this.despatchOnFeatureType(fp, feature, session, parent, offset);
                        this.despatchOnFeatureType(fp, feature, parent, offset);
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

            session.flush();

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
                        //this.despatchOnFeatureType(fp, feature, session, parent, offset);
                        this.despatchOnFeatureType(fp, feature, parent, offset);
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
        session.flush();


    }


    interface Args {
        @Option(shortName="c", description="Path to config file for this organism")
        String getConfigFilePath();
    }

    /**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args conf-file-path
     */
    public static void main (String[] args) {

        Cli<Args> cli = CliFactory.createCli(Args.class);
        Args arguments = null;
        try {
          arguments = cli.parseArguments(args);
        }
        catch(ArgumentValidationException exp) {
            System.err.println("Unable to run:");
            System.err.println(cli.getHelpMessage());
            return;
        }

        // Override properties in Spring config file (using a
        // BeanFactoryPostProcessor) based on command-line args
        Properties overrideProps = new Properties();
        //overrideProps.setProperty("dataSource.username", "chado");
        //overrideProps.setProperty("runner.organismCommonName", arguments.getOrganismCommonName());
        //overrideProps.setProperty("runnerConfigParser.organismCommonName", arguments.getOrganismCommonName());

        String[] configs = new String[] {"classpath:NewRunner3.xml"};

        if (arguments.getConfigFilePath() != null) {
            configs = new String[] {"classpath:NewRunner3.xml", "file://"+arguments.getConfigFilePath()};
        }

        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);

        ApplicationContext ctx = new FileSystemXmlApplicationContext(configs);

        logger.warn("*** About to retrieve Runner class");
        NewRunner3I runner = (NewRunner3I) ctx.getBean("runner", NewRunner3I.class);
        logger.warn("*** Retrieved Runner class");
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

    public void setOrganismCommonNameHolder(
            OrganismCommonNameHolder organismCommonNameHolder) {
        this.organismCommonNameHolder = organismCommonNameHolder;
    }

    public void setInputSources(InputSources inputSources) {
        this.inputSources = inputSources;
    }

    public Organism getOrganism() {
        return organism;
    }

}
