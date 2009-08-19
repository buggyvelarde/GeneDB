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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GOALoader extends Loader {
    private static final Logger logger = Logger.getLogger(GOALoader.class);
    
    Boolean goTermErrorsAreNotFatal = true;
    
    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "go-term-errors-are-not-fatal");
        return options;
    }
    @Override
    protected boolean processOption(String optionName, String optionValue) {

        if (optionName.equals("go-term-errors-are-not-fatal")) {
            if (optionValue == null) {
                goTermErrorsAreNotFatal = true;
            } else {
                goTermErrorsAreNotFatal = Boolean.valueOf(optionValue);
            }
            return true;
        }        
        return false;
    }
        
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
        else if (hit.getFeatureType().equals("protein")) {
            polypeptide = getPolypeptideByMangledName(hit.getFeatureUniquename());
        }        
        else {
            logger.error(String.format("Feature '%s' is of type %s not type gene or protein", hit.getFeatureUniquename(), hit.getFeatureType()));
            return;
        }
        
        if (polypeptide == null) {
            logger.error(String.format("Could not find polypeptide for key '%s'", hit.getFeatureUniquename()));
            return;
        }
        
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
            
            if (hit.getWithFrom() != null)
                goInstance.setWithFrom(hit.getWithFrom());
            if (hit.getQualifier() != null)
                goInstance.addQualifier(hit.getQualifier());
            
            try {
                goInstance.setEvidence(GoEvidenceCode.valueOf(hit.getEvCode()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(String.format(
                        "Failed to parse GO evidence code '%s'", hit.getEvCode()));
            }
            goInstance.setRef(hit.getDbxref());

            String comment = "From GO association file";
            
            DbXRef withFromDbxref = null;
            if (hit.getWithFrom() != null) {
                logger.debug(String.format("Adding withFrom '%s'", hit.getWithFrom()));
                //withFrom is in the format DB:accession
                withFromDbxref = objectManager.getDbXRef(hit.getWithFrom());
                if (withFromDbxref == null) {
                    throw new RuntimeException(String.format("Error loading GO term: Db is not found for withFrom DbXRef '%s'", hit.getWithFrom()));
                }
            }

            featureUtils.createGoEntries(polypeptide, goInstance, comment, withFromDbxref);    

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
        String line;
        
        int lineNumber = 0;
        while (null != (line = reader.readLine())) { //While not end of file
            if(0 < line.length()){
                lineNumber++;
                StringBuilder sb = new StringBuilder(line);
                sb.append('\n');
                logger.trace(sb);
                GOHit hit = new GOHit(lineNumber, line);
                hits.add(hit);
            }
        }          
    }

    public Collection<GOHit> hits() {
        return hits;
    }
}

/* Each 'hit' corresponds to a line in the .GO file */
class GOHit {
    
    //File format for GO association file
    //    1    DB     required     1     SGD
    //    2    DB_Object_ID     required     1     S000000296
    //    3     DB_Object_Symbol     required     1     PHO3
    //    4     Qualifier     optional     0 or greater     NOT
    //    5     GO ID     required     1     GO:0003993
    //    6     DB:Reference (|DB:Reference)     required     1 or greater     SGD_REF:S000047763|PMID:2676709
    //    7     Evidence code     required     1    IMP
    //    8     With (or) From     optional     0 or greater     GO:0000346
    //    9     Aspect     required     1     F
    //    10     DB_Object_Name     optional     0 or 1     acid phosphatase
    //    11     DB_Object_Synonym (|Synonym)     optional     0 or greater     YBR092C
    //    12     DB_Object_Type     required     1     gene
    //    13     taxon(|taxon)     required     1 or 2     taxon:4932
    //    14     Date     required     1     20010118
    //    15     Assigned_by     required     1     SGD
    
    // The columns we're interested in:
    private static final int DB                 = 0;
    private static final int DB_OBJECT_ID       = 1;
    private static final int DB_OBJECT_SYMBOL   = 2;
    private static final int QUALIFIER             = 3;
    private static final int GO_ID                 = 4;
    private static final int DBXREF             = 5;
    private static final int EVIDENCE_CODE      = 6;
    private static final int WITH_FROM             = 7;
    private static final int ASPECT             = 8;
    private static final int DB_OBJECT_NAME     = 9;
    private static final int DB_OBJECT_SYNONYM    = 10;
    private static final int DB_OBJECT_TYPE        = 11;
    private static final int TAXON                 = 12;
    private static final int DATE                 = 13;
    private static final int ASSIGNED_BY        = 14;   
    
    private String featureDb, featureUniquename, featureSymbol, qualifier, goId, 
    dbxref, evCode, withFrom, aspect, featureProduct, featureSynonym, 
    featureType, taxon, date, curator;
    private int lineNumber;
    
    public GOHit(int lineNumber, String row) {
        this(lineNumber, row.split("\t"));
    }
    
    public GOHit(int lineNumber, String[] rowFields) {
        
        this.lineNumber = lineNumber;
        this.featureDb = rowFields[DB];
        this.featureUniquename = rowFields[DB_OBJECT_ID];
        this.featureSymbol = rowFields[DB_OBJECT_SYMBOL];
        this.qualifier = rowFields[QUALIFIER];
        this.goId = rowFields[GO_ID]; 
        this.dbxref = rowFields[DBXREF];
        this.evCode = rowFields[EVIDENCE_CODE]; 
        this.withFrom = rowFields[WITH_FROM];
        this.aspect = rowFields[ASPECT];
        this.featureProduct = rowFields[DB_OBJECT_NAME];
        this.featureSynonym = rowFields[DB_OBJECT_SYNONYM]; 
        this.featureType = rowFields[DB_OBJECT_TYPE];
        this.taxon = rowFields[TAXON];
        this.date = rowFields[DATE]; 
        this.curator = rowFields[ASSIGNED_BY];     
    
    }
    
    public int getLineNumber() {
        return lineNumber;
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
        if (featureProduct.equals("")) {
            return null;
        }
        return featureProduct;
    }
  
    public String getFeatureSynonym() {
        if (featureSynonym.equals("")) {
            return null;
        }
        return featureSynonym;
    }
    
    public String getFeatureType() {
        return featureType;
    }
    
    public String getQualifier() {
        if (qualifier.equals("")) {
            return null;
        }
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
        if (withFrom.equals("")) {
            return null;
        }
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
