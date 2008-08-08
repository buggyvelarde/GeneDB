package org.genedb.db.loading.aux;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.FeatureUtils;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class Loader {

    private static final Logger logger = Logger.getLogger(Loader.class);

    protected SequenceDao sequenceDao;
    protected FeatureUtils featureUtils;
    protected HibernateTransactionManager hibernateTransactionManager;
    protected DbXRefManager dbxrefManager;

    /**
     * What options does this loader accept?
     * @return a set of option names.
     */
    protected Set<String> getOptionNames() {
        return Collections.emptySet();
    }

    /**
     * Does this loader expect input files?
     * @return <code>true</code> if so, <code>false</code> if not.
     *          The default implementation returns <code>true</code>.
     */
    protected boolean loadsFromFile() {
        return true;
    }

    protected boolean processOptionIfValid(String optionName, String optionValue) {
        if (getOptionNames().contains(optionName))
            return processOption(optionName, optionValue);
        else
            return false;
    }

    protected boolean processOption(@SuppressWarnings("unused") String optionName, @SuppressWarnings("unused") String optionValue) {
        throw new IllegalStateException("processOption() must be overridden if options are specified");
    }

    void load(InputStream inputStream) throws IOException {
        SessionFactory sessionFactory = hibernateTransactionManager.getSessionFactory();
        Session session = SessionFactoryUtils.getSession(sessionFactory, dbxrefManager, null);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

        doLoad(inputStream, session);

        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.releaseSession(session, sessionFactory);
    }

    /**
     * Load data. This method must be implemented by all implementing classes.
     * It will be called once for each input file.
     * @param inputStream a stream from which the input data may be read, or <code>null</code>
     *          if {@link #loadsFromFile()} returns <code>false</code>
     * @param session the Hibernate session
     * @throws IOException
     */
    protected abstract void doLoad(InputStream inputStream, Session session) throws IOException;

    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
    }

    public void setHibernateTransactionManager(HibernateTransactionManager hibernateTransactionManager) {
        this.hibernateTransactionManager = hibernateTransactionManager;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setDbxrefManager(DbXRefManager dbxrefManager) {
        this.dbxrefManager = dbxrefManager;
    }

    protected Polypeptide getPolypeptideForGene(String geneUniqueName) {
        AbstractGene gene = sequenceDao.getFeatureByUniqueName(geneUniqueName,
            AbstractGene.class);
        if (gene == null) {
            logger.error(String.format("Gene '%s' not found in database", geneUniqueName));
            return null;
        }

        Collection<Transcript> transcripts = gene.getTranscripts();
        if (transcripts.isEmpty()) {
            logger.error(String.format("Gene '%s' has no transcripts", geneUniqueName));
            return null;
        }

        // Select the coding transcript with the least feature_id,
        // logging an error if there's more than one.
        ProductiveTranscript selectedTranscript = null;
        int numberOfProductiveTranscripts = 0;
        for (Transcript transcript : transcripts)
            if (transcript instanceof ProductiveTranscript) {
                ++ numberOfProductiveTranscripts;
                if (selectedTranscript == null
                    || transcript.getFeatureId() < selectedTranscript.getFeatureId())
                {
                    selectedTranscript = (ProductiveTranscript) transcript;
                }
            }

        if (selectedTranscript == null) {
            logger.error("Gene '%s' has no coding transcripts.");
            return null;
        }
        if (numberOfProductiveTranscripts > 1)
            logger.error(String.format("The gene '%s' is alternatively spliced: " +
                "we don't know to which transcript the protein data applies.\n" +
                "We've selected '%s', the first coding transcript by loading" +
                "order: there's no reason to believe that is right!",
                geneUniqueName, selectedTranscript.getUniqueName()));

        return selectedTranscript.getProtein();
    }

    /**
     * If ever anything deserved an explanation, this does!
     *
     * InterPro doesn't allow the sequence names in its input FASTA
     * files to contain colons: it seems to treat the colon as a
     * separator of some sort. Therefore, when the polypeptide
     * sequences are extracted, colons in the polypeptide name
     * are transliterated to dots. Unfortunately it didn't occur
     * to me at the time that this transformation is not easily
     * reversible, given that the names can and do contain dots.
     *
     * What we do, therefore, is to successively change dots into
     * colons from right to left, until we find the thus-named
     * polypeptide in the database. One can imagine
     * plausible-looking pairs of names for which this procedure
     * would fail: for example, suppose we had genes called XY001023
     * and XY001023.1, which might be possible under some naming scheme.
     * If the former is alternatively spliced, the polypeptide produced
     * from its second transcript would be XY001023:1:pep; the polypeptide
     * produced from (the first transcript of) the latter would be XY001023.1:pep.
     *
     * To be on the safe side, in future we'll translate a colon into a
     * double dot (lying the colon on its side, as it were). To allow for
     * this, the very first thing we do is to check whether the supplied
     * mangled name contains a double dot. (If it does, we assume that
     * the double-dot translation has been used.)
     *
     * TODO When the present, complicated, logic is no longer needed, remove
     * it and this explanation, and just use the double-dot translation.
     *
     * @param mangledName
     * @return
     */
    protected Polypeptide getPolypeptideByMangledName(String mangledName) {
        if (mangledName.contains(".."))
            return sequenceDao.getFeatureByUniqueName(mangledName.replaceAll("\\.\\.", ":"), Polypeptide.class);

        StringBuilder name = new StringBuilder(mangledName);
        int lastDot;
        while (0 < (lastDot = name.lastIndexOf("."))) {
            name.setCharAt(lastDot, ':');
            logger.debug(String.format("Looking for polypeptide feature '%s'", name));
            Polypeptide polypeptide = sequenceDao.getFeatureByUniqueName(name.toString(), Polypeptide.class);
            if (polypeptide != null)
                return polypeptide;
        }
        return sequenceDao.getFeatureByUniqueName(mangledName, Polypeptide.class);
    }

}