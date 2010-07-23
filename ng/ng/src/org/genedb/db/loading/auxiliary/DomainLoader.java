
package org.genedb.db.loading.auxiliary;

import org.genedb.db.loading.GoInstance;
import org.genedb.db.loading.ParsingException;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A generic loader for polypeptide domain results.
 * The input file is assumed to be in a format defined in a DomainRow class.
 * This loader has several options:
 * <dl>
 *     <dt><code>programVersion</code><dd>The version of pfam_scan/prosite etc used. Required.</dd></dt>
 *     <dt><code>notFoundNotFatal</code><dd>Boolean. Attempting to load domains for a gene or polypeptide that is
 *         missing from the database is not fatal.</dd></dt>
 *     <dt><code>key-type</code>, whose possible values are:
 *         <dl>
 *             <dt><code>gene</code></dt><dd>The keys in the input file are gene names. This is the default.</dd>
 *             <dt><code>polypeptide</code></dt><dd>The keys in the input file are polypeptide names
 *                 (possibly with colons converted to doubled dots).</dd>
 *         </dl>
 *     </dt>
 * </dl>
 *
 * @author rh11
 * @author te3
 */
public class DomainLoader extends Loader {
    private static final Logger logger = Logger.getLogger(DomainLoader.class);

    //Constants
    String analysisProgramVersion;
    String analysisProgram;
    boolean notFoundNotFatal = false;
    Pfam2GoFile pfam2GoFile;

    @Override
    protected Set<String> getOptionNames() {
    Set<String> options = new HashSet<String>();
    Collections.addAll(options, "key-type", "program-version", "not-found-not-fatal", "program");
        return options;
    }

    private static enum KeyType {GENE, POLYPEPTIDE};
    private KeyType keyType = KeyType.GENE;

    @Override
    protected boolean processOption(String optionName, String optionValue) {

        if (optionName.equals("program-version")) {
            analysisProgramVersion = optionValue;
            return true;
        }
        else if (optionName.equals("program")) {
            analysisProgram = optionValue;
            return true;
        }
        else if (optionName.equals("not-found-not-fatal")) {
            if (!optionValue.equals("true") && !optionValue.equals("false")) {
                return false;
            }
            notFoundNotFatal = Boolean.valueOf(optionValue);
            return true;
        }
        else if (optionName.equals("key-type")) {
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
        }
        return false;
    }

    @Override
    protected void doLoad (InputStream inputStream, Session session) throws IOException {

        loadDomainFile(new DomainFile(analysisProgram, inputStream), session);
    }

    private Analysis analysis;
    @Transactional
    protected void loadDomainFile(DomainFile domainFile, Session session) throws IOException {

        // Add analysis
        analysis = new Analysis();
        analysis.setProgram(analysisProgram);
        analysis.setProgramVersion(analysisProgramVersion);
        sequenceDao.persist(analysis);

        Collection<String> keys = domainFile.keys();
        int n=1;
        for (String key: keys) {
            logger.info(String.format("Processing key '%s' [%d/%d]", key, n++, keys.size()));
            loadKey(domainFile, key);
            /*
             * If the session isn't cleared out every so often, it
             * starts to get pretty slow after a while if we're loading
             * a large file.
             */
            if (n % 5 == 1) {
                session.flush();
                logger.info("Clearing session");
                session.clear();
            }
        }

        //remove redundant go terms
        try{
            DeleteRedundantGOTerms.deleteRedundantGOTerms(session);
        }catch (SQLException sqle){
            logger.debug(sqle.toString());
        }
    }

    private void loadKey(DomainFile domainFile, String key) throws IOException {

        Polypeptide polypeptide = getPolypeptideForKey(key);
        if (polypeptide == null) {

            if (notFoundNotFatal) {
                String errorMessage = String.format("Failed to find %s '%s'", keyType, key);
                logger.error(errorMessage);
            }
            else {
                throw new RuntimeException(String.format("Failed to find %s '%s'", keyType, key));
            }
        }
        else {

            for (DomainAcc acc: domainFile.accsForKey(key)) {
                logger.debug(String.format("Processing '%s'", acc.getId()));
                loadGroup(domainFile, key, acc, polypeptide);
            }
        }
    }

    private Polypeptide getPolypeptideForKey(String key) {
        switch (keyType) {
            case GENE: return getPolypeptideForGene(key);
            case POLYPEPTIDE: return getPolypeptideByMangledName(key);
            default: throw new RuntimeException("keyType does not take a legitimate value. This should be impossible.");
        }
    }

    private void loadGroup(DomainFile domainFile, String gene, DomainAcc acc, Polypeptide polypeptide ) throws IOException {
        logger.debug("In loadGroup()");
        DbXRef interProDbxref = null;
        if (acc != DomainAcc.NULL && analysis.getProgram().equals("iprscan")) {
            logger.debug(String.format("Creating InterPro dbxref for '%s' with description '%s'",
                acc.getId(), acc.getDescription()));
            interProDbxref = objectManager.getDbXRef("InterPro", acc.getId(), acc.getDescription());
        }


        int n = -1;
        for (DomainRow row: domainFile.rows(gene, acc)) {
            n++;
            logger.debug(row);

            // Insert polypeptide_domain
            DbXRef dbxref = objectManager.getDbXRef(row.db(), row.nativeAcc(), row.nativeDesc());
            if (dbxref == null) {
                throw new RuntimeException(String.format("Could not find database '%s' on line %d", row.db(), row.lineNumber()));
            }

            String domainUniqueName;
            String accessionNumber = acc.getId();
            if (accessionNumber == null) {
                accessionNumber = row.nativeAcc();
            }
            if (n == 0) {
                domainUniqueName = String.format("%s:%s:%s",
                    polypeptide.getUniqueName(), row.db(), accessionNumber);
            } else {
                domainUniqueName = String.format("%s:%s:%s:%d",
                    polypeptide.getUniqueName(), row.db(), accessionNumber, n);
            }


            PolypeptideDomain polypeptideDomain = sequenceDao.createPolypeptideDomain(domainUniqueName, polypeptide,
                                              row.score(),row.acc().getDescription(), row.fmin(), row.fmax(),
                                              dbxref, row.evalue(), analysis);

            //add GO terms
            addGoTerms(row.getGoTerms(), polypeptide, polypeptideDomain, row.getGoTermComment());

            // link to InterPro dbxref if applicable
            if (interProDbxref != null && analysis.getProgram().equals("iprscan")) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(interProDbxref, polypeptideDomain, true);
                sequenceDao.persist(featureDbXRef);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

        }
    }

    private void addGoTerms(Set<GoInstance> goTerms, Polypeptide polypeptide, PolypeptideDomain polypeptideDomain, String comment) {

        for (GoInstance goInstance: goTerms) {
            try {
                if (polypeptide.getGo().contains(goInstance.getId())) {
                    logger.info(String.format("The GO term '%s' has already been added to polypeptide '%s'",
                        goInstance.getId(), polypeptide));
                    continue;
                }

                logger.info(String.format("Creating %s GO term '%s' for domain '%s'",
                    comment, goInstance.getId(), polypeptideDomain.getUniqueName()));

                featureUtils.createGoEntries(polypeptide, goInstance,
                    comment, (DbXRef) null);
            } catch (ParsingException e) {
                logger.error(e);
            }
        }
    }

    @Transactional
    void clear(final String organismCommonName, final String analysisProgram) throws HibernateException, SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                new ClearDomains(connection, organismCommonName, analysisProgram).clear();
            }
        });
    }

}
