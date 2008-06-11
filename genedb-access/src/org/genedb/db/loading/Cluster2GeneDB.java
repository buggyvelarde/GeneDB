package org.genedb.db.loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

public class Cluster2GeneDB {

    protected static final Log logger = LogFactory.getLog(Cluster2GeneDB.class);

    private SequenceDao sequenceDao;

    private GeneralDao generalDao;

    private HibernateTransactionManager hibernateTransactionManager;

    private Session session;

    protected Db ORTHOMCLDB;

    public static void main(String[] args) {

        String[] filePaths = args;

        if (filePaths.length == 0) {
            System.err.println("No input files specified");
            System.exit(-1);
        }

        Properties overrideProps = new Properties();
        overrideProps.setProperty("dataSource.username", "pathdb");

        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "NewRunner.xml" });

        Cluster2GeneDB application = (Cluster2GeneDB) ctx.getBean("cluster2genedb",
            Cluster2GeneDB.class);
        application.afterPropertiesSet();
        File[] inputs = new File[filePaths.length];
        long start = new Date().getTime();
        for (int i = 0; i < filePaths.length; i++) {
            inputs[i] = new File(filePaths[i]);
        }
        application.process(inputs);
        long duration = (new Date().getTime() - start) / 1000;
        logger.info("Processing completed: " + duration / 60 + " min " + duration % 60 + " sec.");
    }

    public void afterPropertiesSet() {
        session = hibernateTransactionManager.getSessionFactory().openSession();
        ORTHOMCLDB = generalDao.getDbByName("ORTHOMCLDB");
    }

    public void process(final File[] files) {
        Transaction transaction = session.beginTransaction();
        for (File file : files) {
            System.err.println("Processing '" + file.getName() + "'");
            Map<String, String> map = null;
            Reader r = null;
            try {
                r = new FileReader(file);
                map = parseFile(r);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            writeToDb(map);
            transaction.commit();
        }
    }

    private void writeToDb(Map<String, String> map) {

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value != null && value.length() > 0) {
                Feature polypeptide = getPolypeptide(key);
                if (polypeptide == null) {
                    logger.warn("Can't find gene for '" + key + "'");
                } else {

                    DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(ORTHOMCLDB, value);
                    if (dbXRef == null) {
                        dbXRef = new DbXRef(ORTHOMCLDB, key);
                        generalDao.persist(dbXRef);
                    }

                    FeatureDbXRef fDbXRef = new FeatureDbXRef(dbXRef, polypeptide, true);
                    sequenceDao.persist(fDbXRef);
                }
            }
        }

    }

    private Feature getPolypeptide(String geneName) {
        geneName = geneName.concat(":pep");
        Feature polypeptide = sequenceDao.getFeatureByUniqueName(geneName, "polypeptide");
        if (polypeptide == null) {
            return null;
        }
        logger.warn("polypeptide is " + polypeptide + ", gene name is " + geneName);
        int id = polypeptide.getFeatureId();
        polypeptide = (Feature) session.load(Feature.class, new Integer(id));
        return polypeptide;
    }

    private Map<String, String> parseFile(Reader r) {
        BufferedReader input = new BufferedReader(r);
        String line = null;
        Map<String, String> map = new HashMap<String, String>();
        try {
            while ((line = input.readLine()) != null) {

                String terms[] = line.split("\t");
                map.put(terms[0], terms[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public GeneralDao getGeneralDao() {
        return generalDao;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public HibernateTransactionManager getHibernateTransactionManager() {
        return hibernateTransactionManager;
    }

    public void setHibernateTransactionManager(
            HibernateTransactionManager hibernateTransactionManager) {
        this.hibernateTransactionManager = hibernateTransactionManager;
    }

    public SequenceDao getSequenceDao() {
        return sequenceDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

}
