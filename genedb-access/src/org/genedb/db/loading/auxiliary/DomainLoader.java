
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static String today;
    static {
    	DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
    	today = dFormat.format(new Date());
	  }
	    
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
            
            if (analysisProgram.equals("pfam_scan")) {
            	//parse the pfam2go file if not already done
            	if (pfam2GoFile == null) {
            		logger.info(String.format("Creating pfam2go mapping")); 
            		pfam2GoFile = new Pfam2GoFile();
            	}

            	addPfam2GoMapping(polypeptide, polypeptideDomain, accessionNumber);
            }
            
            // link to InterPro dbxref if applicable
	    	// this isn't get needed, but will be once InterProLoader is integrated into DomainLoader
            if (interProDbxref != null && analysis.getProgram().equals("InterPro")) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(interProDbxref, polypeptideDomain, true);
                sequenceDao.persist(featureDbXRef);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

        }
    }
    
    
    private void addPfam2GoMapping(Polypeptide polypeptide, PolypeptideDomain polypeptideDomain, 
    		String pfamAccession) {

		if (pfam2GoFile.getGoByPfam(pfamAccession) == null) {
			logger.debug(String.format("The domain '%s' has no mapped GO terms", pfamAccession));			
			return;
		}
		
    	for (String goAccession: pfam2GoFile.getGoByPfam(pfamAccession)) {
    		
    		try {
    			GoInstance goInstance = new GoInstance();
    			goInstance.setId(goAccession);
    			goInstance.setDate(today);
		
    			if (polypeptide.getGo().contains(goInstance.getId())) {
    				logger.info(String.format("The GO term '%s' has already been added to polypeptide '%s'",
    						goInstance.getId(), polypeptide));
    				continue;
    			}
 
    			logger.info(String.format("Creating pfam2go GO term '%s' for domain '%s'",
    					goInstance.getId(), polypeptideDomain.getUniqueName()));

        		featureUtils.createGoEntries(polypeptide, goInstance,
        				"From Pfam2GO mapping", (DbXRef) null);
        		
        		
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

/* 
 * Stores the pfam2go mappings in a Map<String, Set<String>>
 */
class Pfam2GoFile {
	
	Map<String, Set<String>> pfam2go;
    private static final Logger logger = Logger.getLogger(Pfam2GoFile.class);
    
	public Pfam2GoFile() throws IOException {
		
		InputStream inputStream = new FileInputStream("resources/pfam2go");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		pfam2go = new HashMap<String, Set<String>>();
		while (null != (line = reader.readLine())) { //While not end of file
			if(0 < line.length()){
				StringBuilder sb = new StringBuilder(line);
				sb.append('\n');
				//logger.info(sb);
				
				Pfam2GoLine pfam2GoLine = new Pfam2GoLine(line);
				if (!pfam2go.containsKey(pfam2GoLine.pfamAccession)) {
					pfam2go.put(pfam2GoLine.pfamAccession, new HashSet<String>());
				}
				pfam2go.get(pfam2GoLine.pfamAccession).add(pfam2GoLine.goAccession);
				logger.debug(String.format("adding pfam %s for go %s", pfam2GoLine.pfamAccession, pfam2GoLine.goAccession));
			}
		}		
	}
	
	public Set<String> getGoByPfam(String pfamAccession) {
		return(pfam2go.get(pfamAccession));
	}

}
/* 
 * Parses a single line of the pfam2go mapping file
 */
class Pfam2GoLine {
	
	String pfamAccession, goAccession;
	
    public Pfam2GoLine(String line) {
         //Sample line
        //Pfam:PF00001 7tm_1 > GO:G-protein coupled receptor protein signaling pathway ; GO:0007186
    	final Pattern LINE_PATTERN = Pattern.compile("Pfam:(\\S+)\\s+(.+>.+)\\s+;\\s+GO:(\\d+)"); 
        Matcher matcher = LINE_PATTERN.matcher(line);
        
        if (matcher.matches()) {
            this.pfamAccession = matcher.group(1);
            this.goAccession = matcher.group(3);
        }
    }
}


