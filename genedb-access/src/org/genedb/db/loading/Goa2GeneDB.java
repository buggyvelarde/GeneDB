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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.Properties;


import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.analysis.Analysis;
import org.gmod.schema.analysis.AnalysisFeature;
import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureCvTermProp;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.PairwiseOverlayRenderer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;



/**
 * This class is the loads up GO association data into GeneDB.
 *
 * Usage: Goa2GeneDB GOAssociation_file [GOAssociation_file ...]
 *
 *
 * @author Adrian Tivey (art)
 * @author Chinmay Patel (cp2)
 */
@Repository
@Transactional
public class Goa2GeneDB implements Goa2GeneDBI{

    private static String usage="Goa2GeneDB GOAssociation_file";

    protected static final Log logger = LogFactory.getLog(Goa2GeneDB.class);
    
    private FeatureUtils featureUtils;
	
    private SequenceDao sequenceDao;

    private OrganismDao organismDao;

    private CvDao cvDao;
    
    private PubDao pubDao;

    private GeneralDao generalDao;
    
    private HibernateTransactionManager hibernateTransactionManager;
    
    private Organism DUMMY_ORG;

	private Pattern PUBMED_PATTERN;
    
	protected CvTerm GO_KEY_EVIDENCE;
    protected CvTerm GO_KEY_DATE;
    protected CvTerm GO_KEY_QUALIFIER;
    
    private Session session;
        
    public void setHibernateTransactionManager(
			HibernateTransactionManager hibernateTransactionManager) {
		this.hibernateTransactionManager = hibernateTransactionManager;
	}

    	/**
         * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
         * based on a Properties file, based on the organism. This is passed in on
         * the command-line.
         *
         * @param args organism_common_name, [conf file path]
         * @throws XMLStreamException 
         * @throws FileNotFoundException 
         */
        public static void main (String[] args) throws FileNotFoundException {
    
            String[] filePaths = args;
    
            if (filePaths.length == 0) {
            	System.err.println("No input files specified");
            	System.exit(-1);
            }
            
            // Override properties in Spring config file (using a
            // BeanFactoryPostProcessor) based on command-line args
            Properties overrideProps = new Properties();
            overrideProps.setProperty("dataSource.username", "chado");
            //overrideProps.setProperty("runner.organismCommonName", organismCommonName);
            //overrideProps.setProperty("runnerConfigParser.organismCommonName", organismCommonName);
    
//            if (configFilePath != null) {
//                overrideProps.setProperty("runnerConfigParser.configFilePath", configFilePath);
//            }
    
    
            PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);
    
    
            ApplicationContext ctx = new ClassPathXmlApplicationContext(
                    new String[] {"NewRunner.xml"});
    
            Goa2GeneDBI application = (Goa2GeneDBI) ctx.getBean("goa2genedb", Goa2GeneDBI.class);
            application.afterPropertiesSet();
            File[] inputs = new File[filePaths.length];
            long start = new Date().getTime();
            for (int i = 0; i < filePaths.length; i++) {
            	inputs[i] = new File(filePaths[i]);
    		}
            
			application.process(inputs);
			long duration = (new Date().getTime()-start)/1000;
			logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
        }
        
        /**
         * Does a string look likes it's a PubMed reference
         * 
         * @param xref The string to examine
         * @return true if it looks like a PubMed reference
         */
        private boolean looksLikePub(String xref) {
        	boolean ret =  PUBMED_PATTERN.matcher(xref).lookingAt();
        	logger.warn("Returning '"+ret+"' for '"+xref+"' for looks like pubmed");
        	return ret;
        }
        
        /**
         * Create, or lookup a Pub object from a PMID:acc style input, although the 
         * prefix is ignored
         * 
         * @param ref the reference
         * @return the Pub object
         */
        protected Pub findOrCreatePubFromPMID(String ref) {
            logger.warn("Looking for '"+ref+"'");
            Db DB_PUBMED = generalDao.getDbByName("MEDLINE");
            int colon = ref.indexOf(":");
            String accession = ref;
            if (colon != -1) {
                accession = ref.substring(colon+1);
            }
            DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(DB_PUBMED, accession);
            Pub pub;
            if (dbXRef == null) {
                dbXRef = new DbXRef(DB_PUBMED, accession);
                generalDao.persist(dbXRef);
                CvTerm cvTerm = cvDao.getCvTermById(1); //TODO -Hack
                pub = new Pub("PMID:"+accession, cvTerm);
                generalDao.persist(pub);
                PubDbXRef pubDbXRef = new PubDbXRef(pub, dbXRef, true);
                generalDao.persist(pubDbXRef);
            } else {
                logger.warn("DbXRef wasn't null");
                pub = pubDao.getPubByDbXRef(dbXRef);
            }
            logger.warn("Returning pub='"+pub+"'");
            return pub;
        }

        public void writeToDb(List<GoInstance> goInstances) {
        	for (GoInstance go : goInstances) {
        		
        		Feature polypeptide = getPolypeptide(go.getGeneName());
        		
        		if(polypeptide != null) {
        		
        		String id = go.getId();
                //logger.debug("Investigating storing GO '"+id+"' on '"+polypeptide.getUniquename()+"'");

                CvTerm cvTerm = cvDao.getGoCvTermByAccViaDb(id);
                if (cvTerm == null) {
                    logger.warn("Unable to find a CvTerm for the GO id of '"+id+"'. Skipping");
                    continue;
                }

                Pub pub = pubDao.getPubByUniqueName("null");
                String ref = go.getRef();
                // Reference
                Pub refPub = pub;
                if (ref != null && looksLikePub(ref)) {
                    // The reference is a pubmed id - usual case
                    refPub = findOrCreatePubFromPMID(ref);
                    //FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
                    //sequenceDao.persist(fctp);
                }


//              logger.warn("pub is '"+pub+"'");

                boolean not = go.getQualifierList().contains("not"); // FIXME - Working?
                //logger.warn("gene is " + go.getGeneName() + " Polypeptide is " + polypeptide.getUniqueName());
                List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(polypeptide, cvTerm, not);
                int rank = 0;
                if (fcts.size() != 0) {
                    rank = RankableUtils.getNextRank(fcts);
                }
                //logger.warn("fcts size is '"+fcts.size()+"' and rank is '"+rank+"'");
                FeatureCvTerm fct = new FeatureCvTerm(cvTerm, polypeptide, refPub, not, rank);
                sequenceDao.persist(fct);

                // Reference
//              Pub refPub = null;
//              if (ref != null && ref.startsWith("PMID:")) {
//              // The reference is a pubmed id - usual case
//              refPub = findOrCreatePubFromPMID(ref);
//              FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
//              sequenceDao.persist(fctp);
//              }

                // Evidence
                FeatureCvTermProp fctp = new FeatureCvTermProp(GO_KEY_EVIDENCE , fct, go.getEvidence().getDescription(), 0);
                sequenceDao.persist(fctp);

                // Qualifiers
                int qualifierRank = 0;
                List<String> qualifiers = go.getQualifierList();
                for (String qualifier : qualifiers) {
                    fctp = new FeatureCvTermProp(GO_KEY_QUALIFIER , fct, qualifier, qualifierRank);
                    qualifierRank++;
                    sequenceDao.persist(fctp);
                }

                // With/From
                String xref = go.getWithFrom();
                if (xref != null) {
                    int index = xref.indexOf(':');
                    if (index == -1 ) {
                        logger.error("Got an apparent dbxref but can't parse");
                    } else {
                        List<DbXRef> dbXRefs= findOrCreateDbXRefsFromString(xref);
                        for (DbXRef dbXRef : dbXRefs) {
                            if (dbXRef != null) {
                                FeatureCvTermDbXRef fcvtdbx = new FeatureCvTermDbXRef(dbXRef, fct);
                                sequenceDao.persist(fcvtdbx);
                            }
                        }
                    }
                }
        		} else {
        			logger.error("Gene Name " + go.getGeneName() + " does not exist in database");
        		}
			}
    	}

        /**
         * Take a pipe-seperated string and split them up,  
         * then lookup or create them 
         * 
         * @param xref A list of pipe seperated dbxrefs strings
         * @return A list of DbXrefs
         */
        private List<DbXRef> findOrCreateDbXRefsFromString(String xref) {
            List<DbXRef> ret = new ArrayList<DbXRef>();
            StringTokenizer st = new StringTokenizer(xref, "|");
            while (st.hasMoreTokens()) {
                ret.add(findOrCreateDbXRefFromString(st.nextToken()));
            }
            return ret;
        }
        
        /**
         * Take a db reference and look it up, or create it if it doesn't exist
         * 
         * @param xref the reference ie db:id
         * @return the created or looked-up DbXref
         */
        private DbXRef findOrCreateDbXRefFromString(String xref) {
            int index = xref.indexOf(':');
            if (index == -1) {
                logger.error("Can't parse '"+xref+"' as a dbxref as no colon");
                return null;
            }
            String dbName = xref.substring(0, index);
            String accession = xref.substring(index+1);
            Db db = generalDao.getDbByName(dbName);
            if (db == null) {
                logger.error("Can't find database named '"+dbName+"'");
                return null;
            }
            DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db, accession);
            if (dbXRef == null) {
                dbXRef = new DbXRef(db, accession);
                sequenceDao.persist(dbXRef);
            }
            return dbXRef;
        }

        
        private Feature getPolypeptide(String geneName) {
        	geneName = geneName.concat(":pep");
        	Feature polypeptide = sequenceDao.getFeatureByUniqueName(geneName, "polypeptide");
        	if(polypeptide == null) {
        		return null;
        	}
        	logger.warn("polypeptide is " + polypeptide + "gene name is " + geneName);
        	int id = polypeptide.getFeatureId();
        	polypeptide = (Feature)session.load(Feature.class,new Integer(id));
        	return polypeptide;
		}

    	public void afterPropertiesSet() {
    		System.err.println("In aps cvDao='"+cvDao+"'");
    		session = hibernateTransactionManager.getSessionFactory().openSession();
    		PUBMED_PATTERN = Pattern.compile("PMID:|PUBMED:", Pattern.CASE_INSENSITIVE);
    		Cv CV_FEATURE_PROPERTY = cvDao.getCvByName("feature_property").get(0);
            Cv CV_GENEDB = cvDao.getCvByName("genedb_misc").get(0);
    		GO_KEY_EVIDENCE = cvDao.getCvTermByNameInCv("evidence", CV_GENEDB).get(0);
            GO_KEY_QUALIFIER = cvDao.getCvTermByNameInCv("qualifier", CV_GENEDB).get(0);
            GO_KEY_DATE = cvDao.getCvTermByNameInCv("unixdate", CV_FEATURE_PROPERTY).get(0);
        }


    	public void process(final File[] files) {
    		Transaction transaction = this.session.beginTransaction();
    		for (File file : files) {
    			System.err.println("Processing '"+file.getName()+"'");
        		List<GoInstance> goInstances = null;
    			Reader r = null;
    			try {
    				r = new FileReader(file);
    				goInstances = parseFile(r);
    				
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    				System.exit(-1);
    			}
    			writeToDb(goInstances);
    			transaction.commit();
    		}
    		
    		
    	}
        
    	private List<GoInstance> parseFile(Reader r) {
    		BufferedReader input = new BufferedReader(r);
    		String line = null;
    		List<GoInstance> goInstances = new ArrayList<GoInstance>();
			try {
				while((line = input.readLine()) != null) {

		    		String terms[] = line.split("\t");
		    		
		    		GoInstance goi = new GoInstance();
		    		
					goi.setGeneName(terms[1].trim());
					
					String qualifier = terms[3];
			        if ( qualifier != null && qualifier.length()>0) {
			            goi.addQualifier(qualifier);
			        }
			        
			        String id = terms[4];
			        if (!id.startsWith("GO:")) {
			            System.err.println("WARN: GO id doesn't start with GO: *"+id+"*");
			            return null;
			        }
			        goi.setId( terms[4].substring(3) );
			        
			        goi.setRef(terms[5]);
			        goi.setEvidence(GoEvidenceCode.valueOf(terms[6].trim()));
			        goi.setWithFrom(terms[7]);
			        
			        String aspect = terms[8].substring(0,1).toUpperCase();
		
			        if( "P".equals(aspect) ){
			            goi.setSubtype("process");
			        } else {
			            if( "C".equals(aspect) ){
			                goi.setSubtype("component");
			            } else {
			                if( "F".equals(aspect) ){
			                    goi.setSubtype("function");
			                } else {
			                    logger.warn("WARN: Unexpected aspect *"+goi.getAspect()+"* in GO association file");
			                    return null;
			                }
			            }
			        }
			        
			        goi.setDate(terms[13]);
			        
			        if (terms.length > 13) {
			        		goi.setAttribution(terms[14]);
			        }
			        
			        goInstances.add(goi);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return goInstances;
    	}

        public void setOrganismDao(OrganismDao organismDao) {
            this.organismDao = organismDao;
        }

        public void setSequenceDao(SequenceDao sequenceDao) {
            this.sequenceDao = sequenceDao;
        }

        public void setCvDao(CvDao cvDao) {
        	System.err.println("Changing cvDao to '"+cvDao+"'");
            this.cvDao = cvDao;
        }

        public void setGeneralDao(GeneralDao generalDao) {
            this.generalDao = generalDao;
        }

    	public void setPubDao(PubDao pubDao) {
    		this.pubDao = pubDao;
    	}
        
    }
