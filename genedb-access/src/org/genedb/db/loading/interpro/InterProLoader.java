package org.genedb.db.loading.interpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.FeatureUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class InterProLoader {
    private static final Logger logger = Logger.getLogger(InterProLoader.class);

    private SequenceDao sequenceDao;
    private FeatureUtils featureUtils;
    private HibernateTransactionManager hibernateTransactionManager;

    private static HashMap<String, String> dbs = new HashMap<String, String>();
    static {
        dbs.put("HMMPfam", "Pfam");
        dbs.put("ScanProsite", "PROSITE");
        dbs.put("FPrintScan", "PRINTS");
        dbs.put("ProfileScan", "PROSITE");
        dbs.put("ScanRegExp", "PROSITE");
        dbs.put("HMMSmart", "SMART");
        dbs.put("BlastProDom", "ProDom");
        dbs.put("Superfamily", "Superfamily");
        dbs.put("superfamily", "Superfamily");
    }

    private static FilenameFilter doesNotEndWithTilde = new FilenameFilter() {
        public boolean accept(@SuppressWarnings("unused") File dir, String name) {
            return !name.endsWith("~");
        }
    };

    public void load(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            logger.error("No such file/directory: "+filename);
            return;
        }

        if ( file.isDirectory() ) {
            for (String filteredFilename: file.list(doesNotEndWithTilde))
                load(filename+"/"+filteredFilename);
            return;
        }

        logger.info(String.format("Reading interpro from '%s'", filename));

        InputStream inputStream = new FileInputStream(file);
        if (filename.endsWith(".gz")) {
            logger.info("Treating as a GZIP file");
            inputStream = new GZIPInputStream(inputStream);
        }

        InterProFile interProFile = new InterProFile(inputStream);

        SessionFactory sessionFactory = hibernateTransactionManager.getSessionFactory();
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        Transaction transaction = session.beginTransaction();

        load(interProFile);

        transaction.rollback();
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(session);
    }

    public void load(InterProFile interProFile) {
        for (String gene: interProFile.genes()) {
            logger.debug(String.format("Processing gene '%s'", gene));
            loadGene(interProFile, gene);
        }
    }

    private void loadGene(InterProFile interProFile, String gene) {
        for (String acc: interProFile.accsForGene(gene)) {
            logger.debug(String.format("Processing '%s'", acc));
            loadIndividualHits(interProFile, gene, acc);
        }
    }

    private void loadIndividualHits(InterProFile interProFile, String gene, String acc) {
        for (InterProRow row: interProFile.rows(gene, acc)) {
            logger.debug(row);
        }
    }


    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
    }

    public void setHibernateTransactionManager(
            HibernateTransactionManager hibernateTransactionManager) {
        this.hibernateTransactionManager = hibernateTransactionManager;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}

