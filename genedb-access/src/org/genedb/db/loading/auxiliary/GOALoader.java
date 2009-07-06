package org.genedb.db.loading.auxiliary;

import org.genedb.db.loading.GoEvidenceCode;
import org.genedb.db.loading.GoInstance;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.mapped.DbXRef;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GOALoader extends Loader {
    private static final Logger logger = Logger.getLogger(GOALoader.class);
    
	Boolean goTermErrorsAreNotFatal = true;
	
	

    public void doLoad(InputStream inputStream, Session session) throws IOException {
 
	GOAssociationFile file = new GOAssociationFile(inputStream);

        int n=1;
        for (GOHit hit: file.hits()) {
            logger.info(String.format("[%d/%d] Processing GO for '%s'", n++, file.hits().size(), hit.getFeatureUniquename()));
            loadHit(hit);
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }
    }
    
    private void loadHit(GOHit hit) {

    	Polypeptide polypeptide;
    	if (hit.getFeatureType().equals("gene")) {
    		polypeptide = getPolypeptideForGene(hit.getFeatureUniquename());
    	}
    	else {
    		logger.error(String.format("Feature '%s' is of type %s not type gene", hit.getFeatureUniquename(), hit.getFeatureType()));
    		return;
    	}
    	
    	logger.debug(String.format("Processing feature of name '%s'", hit.getFeatureUniquename()));
    	
    	if (polypeptide == null) {
    		logger.error(String.format("Could not find polypeptide for key '%s'", hit.getFeatureUniquename()));
    		return;
    	}
        
    	//get existing GO annotations on this polypeptide
    	//String existingGO = polypeptide.getGo();
    	
    	//The processGO method takes all the essential information from a hit and creates the corresponding database entries
    	processGO(polypeptide, hit);
    }
    
    protected void processGO(Polypeptide polypeptide, GOHit hit)
			throws RuntimeException {
		try {
			GoInstance goInstance = new GoInstance();
			goInstance.setId(hit.getGoId());
			goInstance.setDate(hit.getDate());
			goInstance.setAttribution(hit.getCurator());
			goInstance.setWithFrom(hit.getWithFrom());

			try {
				goInstance.setEvidence(GoEvidenceCode.valueOf(hit.getEvCode()));
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(String.format(
						"Failed to parse GO evidence code '%s'", hit.getEvCode()));
			}
			goInstance.setRef(hit.getDbxref());

			String comment = "From GO association file";
			
			//if (hit.getWithFrom() == null)
			featureUtils.createGoEntries(polypeptide, goInstance, comment,
				Collections.<DbXRef> emptyList());
			/*
			 * } else {  
			 *  Db db = Db("GO");
			 *  DbXRef withFromDbxref = new DbXRef(db, withFrom);
			 * 	createGoEntries(polypeptide,
			 * 	goInstance, comment, Collections.<DbXRef>emptyList()); else
			 * 	createGoEntries(polypeptide, goInstance, comment,
			 * 	Collections.singletonList(withFromDbxref)); }
			 */			

		} catch (Exception e) {
			if (goTermErrorsAreNotFatal) {
				logger.error(String.format("Error loading GO term '%s'", e));
			} else {		
				throw new RuntimeException("Error loading GO term", e);
			}
		}
	}
}


/* Class corresponding to GO Association file */

class GOAssociationFile {
    private static final Logger logger = Logger.getLogger(GOAssociationFile.class);

    private List<GOHit> hits = new ArrayList<GOHit>();

    public GOAssociationFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String previousLine = null, line;
        while (null != (line = reader.readLine())) { //While not end of file
            if (line.startsWith("Feature: ")) {
                if (previousLine == null) {
                    throw new IllegalStateException();
                }
                StringBuilder sb = new StringBuilder(previousLine);
                while (0 < (line = reader.readLine()).length()) {
                    sb.append(line);
                    sb.append('\n');
                }
                logger.trace(sb);
                parseSummary(sb);
            }

            previousLine = line;
        }       
    }

    public Collection<GOHit> hits() {
        return hits;
    }

    private static final Pattern SUMMARY_PATTERN = Pattern.compile(
    		
    	//File format for GO association file
    	//	1	DB	 required	 1	 SGD
    	//	2	DB_Object_ID	 required	 1	 S000000296
    	//	3	 DB_Object_Symbol	 required	 1	 PHO3
    	//	4	 Qualifier	 optional	 0 or greater	 NOT
    	//	5	 GO ID	 required	 1	 GO:0003993
    	//	6	 DB:Reference (|DB:Reference)	 required	 1 or greater	 SGD_REF:S000047763|PMID:2676709
    	//	7	 Evidence code	 required	 1	IMP
    	//	8	 With (or) From	 optional	 0 or greater	 GO:0000346
    	//	9	 Aspect	 required	 1	 F
    	//	10	 DB_Object_Name	 optional	 0 or 1	 acid phosphatase
    	//	11	 DB_Object_Synonym (|Synonym)	 optional	 0 or greater	 YBR092C
    	//	12	 DB_Object_Type	 required	 1	 gene
    	//	13	 taxon(|taxon)	 required	 1 or 2	 taxon:4932
    	//	14	 Date	 required	 1	 20010118
    	//	15	 Assigned_by
    	
    	"(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\n"		
      
    );
    
    
    private void parseSummary(CharSequence summary) {
       
        Matcher matcher = SUMMARY_PATTERN.matcher(summary);
        if (matcher.matches()) {
            String featureDb  = matcher.group(1);
            String featureUniquename = matcher.group(2);
            String featureSymbol = matcher.group(3);
            String qualifier = matcher.group(4);
            String goId = matcher.group(5);
            String dbxref = matcher.group(6);
            String evCode  = matcher.group(7);
            String withFrom = matcher.group(8);
            String aspect = matcher.group(9);         
            String featureProduct = matcher.group(10);
            String featureSynonym = matcher.group(11);
            String featureType = matcher.group(12);
            String taxon = matcher.group(13);
            String date = matcher.group(14);
            String curator = matcher.group(15);          
            
            hits.add(new GOHit(featureDb, featureUniquename, featureSymbol, qualifier, goId, 
            			dbxref, evCode, withFrom, aspect, featureProduct, featureSynonym, 
            			featureType, taxon, date, curator));
          
        }
        else {
            logger.error("Failed to parse summary:\n" + summary);
        }
        
    }
}

/* Each 'hit' corresponds to a line in the .GO file */
class GOHit {
    
	//private static final Logger logger = Logger.getLogger(GOHit.class);
    private String featureDb, featureUniquename, featureSymbol, qualifier, goId, 
	dbxref, evCode, withFrom, aspect, featureProduct, featureSynonym, 
	featureType, taxon, date, curator;
  
    public GOHit(String featureDb, String featureUniquename, String featureSymbol, String qualifier, 
    		String goId, String dbxref, String evCode, String withFrom, String aspect, 
    		String featureProduct, String featureSynonym, String featureType, String taxon, 
    		String date, String curator) {
    	
    	this.featureDb = featureDb;
    	this.featureUniquename = featureUniquename;
    	this.featureSymbol = featureSymbol;
    	this.qualifier = qualifier;
    	this.goId = goId; 
    	this.dbxref = dbxref;
    	this.evCode = evCode; 
    	this.withFrom = withFrom;
    	this.aspect = aspect;
    	this.featureProduct = featureProduct;
    	this.featureSynonym = featureSynonym; 
    	this.featureType = featureType;
    	this.taxon = taxon;
    	this.date = date; 
    	this.curator = curator;
    }
    
    public String getFeatureDb() {
        return featureDb;
    }
   
    public String getFeatureUniquename() {
        return featureUniquename;
    }
    
    public String getFeatureSymbol() {
        return featureSymbol;
    }
    
    public String getFeatureProduct() {
        return featureProduct;
    }
  
    public String getFeatureSynonym() {
        return featureSynonym;
    }
    
    public String getFeatureType() {
        return featureType;
    }
    
    public String getQualifier() {
        return qualifier;
    }

    public String getGoId() {
        return goId;
    }

    public String getEvCode() {
        return evCode;
    }

    public String getDbxref() {
        return dbxref;
    }

    public String getWithFrom() {
        return withFrom;
    }
    
    public String getAspect() {
        return aspect;
    }
    
    public String getCurator() {
        return curator;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getTaxon() {
        return taxon;
    }

    
}
