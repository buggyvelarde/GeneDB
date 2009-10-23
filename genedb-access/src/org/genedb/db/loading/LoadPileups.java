package org.genedb.db.loading;


import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;




/**
 * Load pileup data from a simple space-separated file.
 * The first line contains meta data about the analysis seperated by '##'
 * eg ANALYSIS_NAME=ssaha##ANALYSIS_PARAMS= -blah -blah##ANALYSIS_TIMESTAMP=##ANALYSIS_PROGRAM_VERSION=1.0##CHADO_ORGANISM_ID=91
 *
 * The second line is the list of headers for the data that is to follow seperated by spaces.
 * eg ssaha_name_of_chromosome_reference ssaha_overall_confidence_score
 *
 * The rest of the lines contain data seperated by spaces
 *
 * In order to load the Pileup files specify the following properties:
 * <dl>
 * <dt><code>load.inputDirectory</code></dt><dd>The path of the directory that contains valid Pileup files, e.g. "/Users/rn2/variationDB" (Required)</dd>
 * <dt><code>load.fileNamePattern</code></dt><dd>The name of the file / pattern (default is *.pileup) eg: ssaha.pileup</dd>
 *
 * @author rn2
 *
 */
public class LoadPileups extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadPileups.class);
    private static String dbSchema;
    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.pileup");
        dbSchema=getRequiredProperty("load.dbSchema");
        LoadPileups loadPileups = new LoadPileups();
        loadPileups.processFileOrDirectory(inputDirectory, fileNamePattern);
    }

    private final PileupsLoader loader;
    private LoadPileups() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});
        this.loader = applicationContext.getBean("pileupsLoader", PileupsLoader.class);
    }

    @Override
    protected void processFile(File inputFile, Reader reader) throws IOException, ParsingException {
        PileupFile pileupFile = new PileupFile(inputFile, reader);
        loader.load(pileupFile,dbSchema);
    }
}
class PileupFile {
    // MetaDataLine is the list of name=value pairs seperated by '##'
    // ANALYSIS_NAME=MAQ##ANALYSIS_PARAMS= pileup#ANALYSIS_TIMESTAMP=1010101010##FIELD_SEPERATOR="\t"
    class MetaDataLine {
        int numberOfFields;
        private String analysisName;
        private String analysisParams;
        private String analysisTimestamp;
        private String analysisProgramVersion;
        private int chado_organism_id;
        private String strain_id;
        private String strain_sample_id;

        

        private MetaDataLine(String line) throws ParsingException {
        String[] namevalpairsStr = line.split("##");
        numberOfFields=namevalpairsStr.length;
        if (numberOfFields != 7) {
            throw new SyntaxError(file, 1,
            String.format("Wrong number of MetaData , expected 7 found %d", numberOfFields));
        }
        for ( String nameval : namevalpairsStr){
            String[] pairs=nameval.split("=");
            if(pairs[0].contains("ANALYSIS_NAME")) analysisName=pairs[1];
            if(pairs[0].contains("ANALYSIS_PARAMS")) analysisParams=pairs[1];
            if(pairs[0].contains("ANALYSIS_TIMESTAMP")) analysisTimestamp=(pairs.length==2)?pairs[1]:"";
            if(pairs[0].contains("ANALYSIS_PROGRAM_VERSION")) analysisProgramVersion=pairs[1];
            if(pairs[0].contains("CHADO_ORGANISM_ID")) chado_organism_id=Integer.parseInt(pairs[1]);
            if(pairs[0].contains("STRAIN_ID")) strain_id=pairs[1];
            if(pairs[0].contains("STRAIN_SAMPLE_ID")) strain_sample_id=pairs[1];

        }

    }
    String getAnalysisName() {
        return analysisName;
    }
    String getAnalysisParams() {
        return analysisParams;
    }
    String getAnalysisTimestamp() {
        return analysisTimestamp;
    }
    String getAnalysisProgramVersion() {
        return analysisProgramVersion;
    }
    int get_Chado_Organism_ID() {
        return chado_organism_id;
    }
    String get_Strain_ID() {
        return strain_id;
    }
    String get_Strain_Sample_ID() {
        return strain_sample_id;
    }
}

    class Line {
        int lineNumber;
        int numberOfFields;
        String fields[];

        private Line(int lineNumber, String line) throws ParsingException {
            this.lineNumber = lineNumber;
            fields = line.split("\\s+"); // "\\t"
        }
        int getNumberOfFields(){
            return numberOfFields;
        }
        String[] getFields(){
            return fields;
        }

    }
    private File file;
    private MetaDataLine metaline;
    private Line headerline;
    private List<Line> lines = new ArrayList<Line>();

    public PileupFile(File file, Reader reader) throws IOException, ParsingException {
        this.file = file;
        BufferedReader br = new BufferedReader(reader);
        String line;
        int lineNumber = 0;
        // First line is the MetaDataLine
        line = br.readLine();
        metaline = new MetaDataLine(line);
        // Second line is the Headers
        line = br.readLine();
        headerline=new Line(0,line);

        while (null != (line = br.readLine())) {
            lineNumber ++;
            lines.add(new Line(lineNumber, line));
        }
    }

    public File file() {
        return file;
    }

    public MetaDataLine metaline() {
        return metaline;
    }

    public List<Line> datalines() {
        return lines;
    }

    public Line headerline() {
        return headerline;
    }

    public int numberOfDataLines() {
        return lines.size();
    }

}

@Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
class PileupsLoader {
    private static final Logger logger = Logger.getLogger(PileupsLoader.class);

    private SimpleJdbcTemplate simpleJdbcTemplate;

    
  
    public void load(PileupFile pileupFile,String dbSchema) throws DataError {

        // Insert into variation.Analysis table
        Date analysistimestamp;
        logger.info(String.format("ANALYSIS_NAME ->%s, ANALYSIS_PARAMS ->%s, ANALYSIS_TIMESTAMP ->%s, STRAIN_ID ->%s,STRAIN_SAMPLE_ID ->%s,NO OF DATA LINES  ->%d",pileupFile.metaline().getAnalysisName(),pileupFile.metaline().getAnalysisParams(),pileupFile.metaline().getAnalysisTimestamp(),pileupFile.metaline().get_Strain_ID(),pileupFile.metaline().get_Strain_Sample_ID(),pileupFile.numberOfDataLines()));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        try{
        if(pileupFile.metaline().getAnalysisTimestamp().length()==0){
        analysistimestamp = new Date();
        }
        else{
        analysistimestamp  = df.parse(pileupFile.metaline().getAnalysisTimestamp());
        }
        }catch(java.text.ParseException e){
            String errorMessage = String.format("ANALYSIS_TIMESTAMP %s not in proper format yyyy-MM-dd-HH-mm-ss",pileupFile.metaline().getAnalysisTimestamp());
        throw new DataError(errorMessage);
        }
        int n=simpleJdbcTemplate.update(
            "insert into "+dbSchema+".Analysis("+
            "program,programversion,timeexecuted"+
            ") values ("+
            "?,?,?"+
            ")",
            pileupFile.metaline().getAnalysisName(), pileupFile.metaline().getAnalysisProgramVersion(),analysistimestamp);
        if (n != 1) {
            throw new DataError(String.format("Unable to insert Analysis '%s'", pileupFile.metaline().getAnalysisName()));
        }

        long analysisId = (Long) simpleJdbcTemplate.queryForMap(
            "select currval('"+dbSchema+".analysis_analysis_id_seq'::regclass) as analysis_id")
            .get("analysis_id");

        // Now insert into "+dbSchema+".AnalysisProp
        
        int cvId = (Integer) simpleJdbcTemplate.queryForMap(
            "select cv_id from cv where name='genedb_misc'")
            .get("cv_id");

        List<Object[]> batch = new ArrayList<Object[]>();
        Object[] commandlineprop = new Object[] {analysisId,"commandline_str",cvId,pileupFile.metaline().getAnalysisParams()};
        batch.add(commandlineprop);
        Object[] strain_id_prop = new Object[] {analysisId,"strain_id",cvId,pileupFile.metaline().get_Strain_ID()};
        batch.add(strain_id_prop);
        Object[] strain_sample_id_prop = new Object[] {analysisId,"strain_sample_id",cvId,pileupFile.metaline().get_Strain_Sample_ID()};
        batch.add(strain_sample_id_prop);


        simpleJdbcTemplate.batchUpdate(
            "insert into "+dbSchema+".AnalysisProp("+
            " analysis_id, type_id, value"+
            ") values ("+
            "?,"+
            "(select cvterm_id as type_id from cvterm where name =? and cv_id=?),"+
            "?"+
            ")"
            ,batch);
            

        // first line is the header and the rest are all data
        PileupFile.Line headers = pileupFile.headerline();
        String headersStr = new String();
        ArrayList <String> headerList = new ArrayList <String>();
        for ( String header : headers.getFields()){
            headersStr+=","+header;
            headerList.add(header);
        }

        logger.info(String.format("HEADERS->%s",headersStr));

        // process data
        // each line is a Pileup so create a feature record for every line
        // each field within a line is featureprop of the Pileup (feature)

        for (PileupFile.Line line: pileupFile.datalines()) {

            // Insert in to feature table
            String pileupFeatureUniqueName = "Pileup_"+pileupFile.metaline().get_Strain_ID()+"_"+pileupFile.metaline().get_Strain_Sample_ID()+"_"+analysisId+"_"+line.lineNumber;

            n = simpleJdbcTemplate.update(
                "insert into "+dbSchema+".Feature("+
                " organism_id,uniquename,type_id,is_analysis,is_obsolete,timeaccessioned,timelastmodified"+
                ") values ("+
                "?,?,(select cvterm_id as type_id from cvterm where name='variation_call'),true,false,?,?"+
                ")"
                ,pileupFile.metaline().get_Chado_Organism_ID(),pileupFeatureUniqueName,new java.sql.Timestamp(analysistimestamp.getTime()),new java.sql.Timestamp(analysistimestamp.getTime()));
            if (n != 1) {
                throw new DataError(String.format("Unable to insert Pileup '%s'",pileupFeatureUniqueName));
            }

            long featureId = (Long) simpleJdbcTemplate.queryForMap(
            "select currval('"+dbSchema+".feature_feature_id_seq'::regclass) as feature_id")
            .get("feature_id");

            // Insert in to featureloc

            String chromosomeReference=line.getFields()[0];
            
            String offset=line.getFields()[1];

            // Convert offset-->interbase coordinates
            Integer fmin = Integer.parseInt(offset);
            fmin = fmin-1;
            Integer fmax = fmin+1;

            n = simpleJdbcTemplate.update(
                "insert into "+dbSchema+".FeatureLoc("+
                "feature_id,srcfeature_id,fmin,is_fmin_partial,fmax,is_fmax_partial,strand,phase,locgroup,rank"+
                ") values ("+
                "?,(select feature_id from feature where uniquename=?),?,?,?,?,?,?,?,?"+
                ")"
                ,featureId,chromosomeReference,fmin,false,fmax,false,0,0,0,0);

            if (n != 1) {
                throw new DataError(String.format("Unable to insert featureLoc for Pileup '%s'",pileupFeatureUniqueName));
            }

            String dataStr = new String();
            Iterator <String> headerIterator = headerList.iterator();
            for ( String datafield : line.getFields()){
                String headerfield=headerIterator.next();
                dataStr+="("+headerfield+","+datafield+")";
                n = simpleJdbcTemplate.update(
                    "insert into "+dbSchema+".FeatureProp("+
                    "feature_id,type_id,value,rank"+
                    ") values ("+
                    "?,(select cvterm_id from cvterm where name=? and cv_id=?),?,?"+
                    ")"
                    ,featureId,headerfield,cvId,datafield,0);

                if (n != 1) {
                    throw new DataError(String.format("Unable to insert featureprop for Pileup '%s'",pileupFeatureUniqueName));
                }

            }
         // now associate this feature with the Analysis using AnalysisFeature record

            n = simpleJdbcTemplate.update(
                "insert into "+dbSchema+".AnalysisFeature("+
                "feature_id,analysis_id"+
                ") values ("+
                "?,?"+
                ")"
                ,featureId,analysisId);

            if (n != 1) {
                throw new DataError(String.format("Unable to insert record into AnalysisFeature for Pileup '%s'",pileupFeatureUniqueName));
            }
        logger.trace(String.format("line %d DATA->%s",line.lineNumber,dataStr));
        
        }



       
    }

   
    /* Spring setters */
    public void setsimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
        this.simpleJdbcTemplate = simpleJdbcTemplate;
    }
    
}

