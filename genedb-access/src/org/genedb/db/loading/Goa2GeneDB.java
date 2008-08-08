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
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureCvTermDbXRef;
import org.gmod.schema.mapped.FeatureCvTermProp;
import org.gmod.schema.mapped.Pub;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
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

    protected static final Log logger = LogFactory.getLog(Goa2GeneDB.class);

    private FeatureUtils featureUtils;

    private SequenceDao sequenceDao;

    private CvDao cvDao;

    private PubDao pubDao;

    private HibernateTransactionManager hibernateTransactionManager;

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

            Properties overrideProps = new Properties();
            overrideProps.setProperty("dataSource.username", "chado");
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

        public void writeToDb(List<GoInstance> goInstances) {
            for (GoInstance go : goInstances) {

                Feature polypeptide = getPolypeptide(go.getGeneName());

                if(polypeptide != null) {

                String id = go.getId();

                CvTerm cvTerm = cvDao.getGoCvTermByAcc(id);
                if (cvTerm == null) {
                    logger.warn("Unable to find a CvTerm for the GO id of '"+id+"'. Skipping");
                    continue;
                }

                Pub pub = pubDao.getPubByUniqueName("null");
                String ref = go.getRef();

                Pub refPub = pub;
                if (ref != null && looksLikePub(ref)) {
                    refPub = featureUtils.findOrCreatePubFromPMID(ref);
                }

                boolean not = go.getQualifierList().contains("not"); // FIXME - Working?
                List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(polypeptide, cvTerm, not);
                int rank = 0;
                if (fcts.size() != 0) {
                    rank = RankableUtils.getNextRank(fcts);
                }
                FeatureCvTerm fct = new FeatureCvTerm(cvTerm, polypeptide, refPub, not, rank);
                sequenceDao.persist(fct);

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
                        List<DbXRef> dbXRefs= featureUtils.findOrCreateDbXRefsFromString(xref);
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
            Cv CV_FEATURE_PROPERTY = cvDao.getCvByName("feature_property");
            Cv CV_GENEDB = cvDao.getCvByName("genedb_misc");
            GO_KEY_EVIDENCE = cvDao.getCvTermByNameInCv("evidence", CV_GENEDB).get(0);
            GO_KEY_QUALIFIER = cvDao.getCvTermByNameInCv("qualifier", CV_GENEDB).get(0);
            GO_KEY_DATE = cvDao.getCvTermByNameInCv("date", CV_FEATURE_PROPERTY).get(0);
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

        public void setSequenceDao(SequenceDao sequenceDao) {
            this.sequenceDao = sequenceDao;
        }

        public void setCvDao(CvDao cvDao) {
            System.err.println("Changing cvDao to '"+cvDao+"'");
            this.cvDao = cvDao;
        }

        public void setPubDao(PubDao pubDao) {
            this.pubDao = pubDao;
        }

    }
