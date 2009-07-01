
package org.genedb.db.loading.auxiliary;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
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

    String analysisProgramVersion;
    String analysisProgram;
    boolean notFoundNotFatal = false;

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
    protected void loadDomainFile(DomainFile domainFile, Session session) {

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
    }

    private void loadKey(DomainFile domainFile, String key) {

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

    private void loadGroup(DomainFile domainFile, String gene, DomainAcc acc, Polypeptide polypeptide ) {
    	logger.debug("In loadGroup()");
        DbXRef interProDbxref = null;
        if (acc != DomainAcc.NULL && analysis.getProgram().equals("InterPro")) {
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
	    
            // link to InterPro dbxref if applicable
	    	// this isn't get needed, but will be once InterProLoader is integrated into DomainLoader
            if (interProDbxref != null && analysis.getProgram().equals("InterPro")) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(interProDbxref, polypeptideDomain, true);
                sequenceDao.persist(featureDbXRef);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

        }
    }
}


