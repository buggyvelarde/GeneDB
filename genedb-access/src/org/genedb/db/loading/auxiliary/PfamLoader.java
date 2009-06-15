package org.genedb.db.loading.auxiliary;

import org.genedb.db.loading.ParsingException;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.gmod.schema.mapped.Analysis;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A loader for PfamScan results. The input file is assumed to be in Pfam raw format.
 * This loader has several options: 
 * <dl> 
 *     <dt><code>programVersion</code><dd>The version of pfam_scan used. Required.</dd></dt>
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
 */
public class PfamLoader extends Loader {
    private static final Logger logger = Logger.getLogger(PfamLoader.class);

    String analysisProgramVersion;
    boolean notFoundNotFatal = false;

    @Override
    protected Set<String> getOptionNames() {
	Set<String> options = new HashSet<String>();
	Collections.addAll(options, "key-type", "pfamscan-version", "not-found-not-fatal");
        return options;
    }

    private static enum KeyType {GENE, POLYPEPTIDE};
    private KeyType keyType = KeyType.GENE;
    
    @Override
    protected boolean processOption(String optionName, String optionValue) {

	if (optionName.equals("pfamscan-version")) {
	    analysisProgramVersion = optionValue;
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
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        loadPfamFile(new PfamFile(inputStream), session);
    }

    private Analysis analysis;
    @Transactional
    private void loadPfamFile(PfamFile pfamFile, Session session) {


	// Add analysis 
	analysis = new Analysis();
	analysis.setProgram("pfam_scan");
	analysis.setProgramVersion(analysisProgramVersion);
	sequenceDao.persist(analysis);
	

        Collection<String> keys = pfamFile.keys();
        int n=1;
        for (String key: keys) {
            logger.info(String.format("Processing key '%s' [%d/%d]", key, n++, keys.size()));
            loadKey(pfamFile, key);
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
    }

    private void loadKey(PfamFile pfamFile, String key) {

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

	    for (PfamAcc acc: pfamFile.accsForKey(key)) {
		logger.debug(String.format("Processing '%s'", acc.getId()));
		loadGroup(pfamFile, key, acc, polypeptide);
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

    private void loadGroup(PfamFile pfamFile, String gene, PfamAcc acc,
            Polypeptide polypeptide ) {
        logger.debug("In loadGroup()");
        DbXRef pfamDbxref = null;
        if (acc != PfamAcc.NULL) {
            logger.debug(String.format("Creating Pfam dbxref for '%s' with description '%s'",
                acc.getId(), acc.getDescription()));
            pfamDbxref = objectManager.getDbXRef("Pfam", acc.getId(), acc.getDescription());
        }


        int n = -1;
        for (PfamRow row: pfamFile.rows(gene, acc)) {
            n++;
            logger.debug(row);

            // Insert polypeptide_domain
            DbXRef dbxref = objectManager.getDbXRef(row.db, row.nativeAcc, row.nativeDesc);
            if (dbxref == null) {
                throw new RuntimeException(String.format("Could not find database '%s' on line %d", row.db, row.lineNumber));
            }

            String domainUniqueName;
            String accessionNumber = acc.getId();
            if (accessionNumber == null) {
                accessionNumber = row.nativeAcc;
            }
            if (n == 0) {
                domainUniqueName = String.format("%s:Pfam:%s",
                    polypeptide.getUniqueName(), accessionNumber);
            } else {
                domainUniqueName = String.format("%s:Pfam:%s:%d",
                    polypeptide.getUniqueName(), accessionNumber, n);
            }


	    PolypeptideDomain polypeptideDomain = sequenceDao.createPolypeptideDomain(domainUniqueName, polypeptide, 
										      row.score,row.acc.getDescription(), row.fmin, row.fmax, 
										      dbxref, row.evalue, analysis);
	    
            // link to Pfam dbxref if applicable
            if (pfamDbxref != null) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(pfamDbxref, polypeptideDomain, true);
                sequenceDao.persist(featureDbXRef);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

        }
    }

    @Transactional
    @SuppressWarnings("deprecation") // When we're using Hibernate 3.3 or later, change
                                     // this to use Session#doWork(Work).
    void clear(String organismCommonName) throws HibernateException, SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        new ClearPfam(session.connection(), organismCommonName).clear();
    }
}

