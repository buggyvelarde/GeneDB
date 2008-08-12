package org.genedb.db.loading.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.loading.GoInstance;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class InterProLoader extends Loader {
    private static final Logger logger = Logger.getLogger(InterProLoader.class);

    @Override
    protected Set<String> getOptionNames() {
        return Collections.singleton("key-type");
    }

    private static enum KeyType {GENE, POLYPEPTIDE};
    private KeyType keyType = KeyType.GENE;

    @Override
    protected boolean processOption(@SuppressWarnings("unused") String optionName, String optionValue) {
        if (optionValue == null)
            return false;
        if (optionValue.equals("gene")) {
            keyType = KeyType.GENE;
            return true;
        }
        if (optionValue.equals("polypeptide")) {
            keyType = KeyType.POLYPEPTIDE;
            return true;
        }
        return false;
    }

    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        loadInterProFile(new InterProFile(inputStream), session);
        try {
            DeleteRedundantGOTerms.deleteRedundantGOTerms(session);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInterProFile(InterProFile interProFile, Session session) {
        Transaction transaction = session.getTransaction();

        Collection<String> keys = interProFile.keys();
        int n=1;
        for (String key: keys) {
            transaction.begin();
            logger.info(String.format("Processing key '%s' [%d/%d]", key, n++, keys.size()));
            loadKey(interProFile, key);
            transaction.commit();
            /*
             * If the session isn't cleared out every so often, it
             * starts to get pretty slow after a while if we're loading
             * a large file. It's important that this come immediately
             * after a flush. (Commit will trigger a flush unless you've
             * set FlushMode.MANUAL, which we assume you haven't.)
             */
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }
    }

    private void loadKey(InterProFile interProFile, String key) {
        Set<String> goTermIds = new HashSet<String>();
        Polypeptide polypeptide = getPolypeptideForKey(key);
        if (polypeptide == null)
            return;
        for (InterProAcc acc: interProFile.accsForKey(key)) {
            logger.debug(String.format("Processing '%s'", acc.getId()));
            loadGroup(interProFile, key, acc, polypeptide, goTermIds);
        }
    }

    private Polypeptide getPolypeptideForKey(String key) {
        switch (keyType) {
        case GENE: return getPolypeptideForGene(key);
        case POLYPEPTIDE: return getPolypeptideByMangledName(key);
        default: throw new RuntimeException("keyType does not take a legitimate value. This should be impossible.");
        }
    }

    private void loadGroup(InterProFile interProFile, String gene, InterProAcc acc,
            Polypeptide polypeptide, Set<String> goTermIds) {
        logger.debug("In loadGroup()");
        DbXRef interproDbxref = null;
        if (acc != InterProAcc.NULL) {
            logger.debug(String.format("Creating InterPro dbxref for '%s' with description '%s'",
                acc.getId(), acc.getDescription()));
            interproDbxref = dbxrefManager.get("InterPro", acc.getId(), acc.getDescription());
        }

        int n = -1;
        for (InterProRow row: interProFile.rows(gene, acc)) {
            n++;
            logger.debug(row);

            // Insert polypeptide_domain
            DbXRef dbxref = dbxrefManager.get(row.db, row.nativeAcc, row.nativeDesc);
            if (dbxref == null) {
                logger.error(String.format("Could not find dbxref '%s'/'%s'", row.db, row.nativeAcc));
                continue;
            }

            String domainUniqueName;
            if (n == 0)
                domainUniqueName = String.format("%s:%s", polypeptide.getUniqueName(), row.nativeAcc);
            else
                domainUniqueName = String.format("%s:%s:%d",
                    polypeptide.getUniqueName(), row.nativeAcc, n);

            PolypeptideDomain polypeptideDomain = sequenceDao.createPolypeptideDomain(
                domainUniqueName, polypeptide, row.score, row.acc.getDescription(), row.fmin, row.fmax, dbxref);

            // link to Interpro dbxref if applicable
            if (interproDbxref != null) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(interproDbxref, polypeptideDomain, true);
                sequenceDao.persist(featureDbXRef);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

            // Insert GO terms, if there are any
            for (GoInstance goTerm: row.goTerms) {
                if (goTermIds.contains(goTerm.getId())) {
                    logger.debug(String.format("The GO term '%s' has already been added to gene '%s'",
                        goTerm.getId(), gene));
                    continue;
                }
                goTermIds.add(goTerm.getId());

                logger.debug(String.format("Creating GO term '%s' for domain '%s'",
                    goTerm.getId(), polypeptideDomain.getUniqueName()));

                featureUtils.createGoEntries(polypeptide, goTerm,
                    "From Interpro file", dbxrefManager.get(goTerm.getWithFrom()));
            }
        }
    }
}

