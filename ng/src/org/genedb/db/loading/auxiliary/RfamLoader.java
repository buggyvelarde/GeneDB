package org.genedb.db.loading.auxiliary;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.NcRNA;
import org.gmod.schema.feature.SnoRNA;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.TRNA;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.ObjectManager;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rfam results are stored in text files with 8 columns (as shown below):
 * 
    Neo_chrIa 1249803 1249678   RF00568       1      57     30.00   SNORA26
    Neo_chrIa 1249880 1249736   RF00002       1      60     20.21   5_8S_rRNA
    Neo_chrIa  757311  757240   RF00005       1      59     59.64   tRNA

 * These columns are:
    1.      Chromosome name (TopLevelFeature)
    2.      Start of gene & transcript (fmin relative to chromosome)
    3.      End of gene & transcript (fmax relative to chromosome)
    4.      RNA family (Dbxref of RNA feature)
    5.      Start of Rfam pattern (ignored)
    6.      End of Rfam pattern (ignored)
    7.      Score (Analysis feature of RNA)
    8.      Type of RNA (Type ID in RNA feature corresponding to a Cv term) 
 * 
 * The code below deals with loading Rfam results into a Chado database. We create an abstract gene and a transcript. 
 * The source feature of both the gene and the transcript is the chromosome/supercontig (which in itself is a feature). 
 * The uniquename for a gene is created manually using the chromosomename_rfam_num where num is a 4-digit number (e.g. 0011). 
 * If there is already a gene with that name, there will be an error and it is upto the user to investigate the problem as there shouldn't,
 * in theory, be a gene with that name in the database. The uniquename for the relevant transcript is that gene name with a ':[type]' 
 * added to the end. The type is determined as follows using information in column 8 of the rfam file:
 *          If it's tRNA, then type is tRNA
 *          If it's something beginning SNOR*, then type is snoRNA
 *          For all others, type is ncRNA
 * The Rfam results file has more specific RNA types which will be dbxref's (the accession is the RF000* element in column 4 and 
 * the description is the text in column 8 like 'SNORA26').
 * 
 * Some other notes:
 * - The values in column 2 and 3 are passed as the fmin and fmax values for both the gene & transcript.
 * - The dbxref and analysisfeature objects are added to the transcript (not the gene) as the transcript is the more important (in this 
 * context) of the two.
 * - If, at any point, the toplevelfeature for this is no longer the chromosome the constant 'topLevelFeatureClass' below has to be changed.
 * - Sometimes the min and max values are in the opposite order as the gene is on the opposite strand. When this happens, the fmin and fmax
 * values are set accordingly and strand is set -1 (for featureloc)
 * 
 * SAMPLE USAGE:
 * The ant target needs to be called as follows:
 * 
 *  ant load-rfam -Dconfig=databaseProperties -Dload.analysis.programVersion=2345 -Dfile=rfamResultsFile
 *  
 *  Probably best to use reload-rfam to delete any old rfam results before adding new ones

 * 
 * @author nds
 * 
 * 
 */
public class RfamLoader extends Loader{
    //Constants
    private static final Logger logger = Logger.getLogger(RfamLoader.class);
    private final String DBNAME = "RFAM";
    private Class<? extends TopLevelFeature> topLevelFeatureClass = Chromosome.class; //Change whenever it is decided that chromosome is no longer the sourcefeature for the gene & transcript
    
    // Configurable parameters
    private Organism organism;
    private String geneName = ""; //Is there a correct gene name?
    private int geneNumber = 0; // This will be incremented accordingly and used to create a unique gene name
    private Analysis analysis; 
    private String analysisProgramVersion;
          
    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "rfam-version");
        return options;
    }
    
    @Override
    protected boolean processOption(String optionName, String optionValue) {
       if (optionName.equals("rfam-version")) {
            analysisProgramVersion = optionValue;
            return true;
        }
        return false;
    }

    /**
     * Reads the Rfam file and parses the lines in the file
     *   
     * @param InputStream, Session
     * @throws IOError if a data problem is discovered
     */
    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException{
        // Add analysis 
        analysis = new Analysis();
        analysis.setProgram("rfam");
        analysis.setProgramVersion(analysisProgramVersion);
        System.out.println("version" + analysisProgramVersion);
        sequenceDao.persist(analysis);
       
        RfamFile file = new RfamFile(inputStream);

        int n=1;
        for (RfamHit hit: file.hits()) {
            logger.info(String.format("[%d/%d] Processing rfam result for chromosome %s (%d-%d) %d", n++, file.hits().size(), hit.getChromosomeName(), hit.getGeneMin(), hit.getGeneMax(), hit.getStrand()));
            loadHit(hit);
            /*
             * If the session isn't cleared out every so often, it starts to get pretty slow after a 
             * while if we're loading a large file.
             */
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }
    }
    
    /**
     * Processes each Rfam 'hit' (corresponding to a valid row in the Rfam results file)
     * 
     * @param Rfamhit
     * 
     */

    /*TODO: Remove @supress and hard-coded Chromosome type & make more generic  */
    @SuppressWarnings("unchecked") 
    private void loadHit(RfamHit hit)  {
        /* Get chromosome (source feature) and thereby, organism */
        Chromosome chromosome = sequenceDao.getFeatureByUniqueName(hit.getChromosomeName(), Chromosome.class);
        if(chromosome!=null){
            organism = chromosome.getOrganism();
        }
        
        /*Construct a unique gene name and then make a new gene. Will throw fatal error if gene with this name exists already - very unlikely.
         * If it does, it's a problem that needs to be solved but not by this bit of java.   
         * Then create relevant featureloc and add to gene*/
        geneNumber++;
        String geneNumberString = Integer.toString(geneNumber);
        while(geneNumberString.length() < 4){
            geneNumberString = "0".concat(geneNumberString);
        }
        String geneUniqueName = (hit.getChromosomeName().concat("_rfam_")).concat(geneNumberString);
               
        Gene gene = AbstractGene.make(Gene.class, organism, geneUniqueName, geneName);
        
        FeatureLoc rfamLoc = new FeatureLoc(chromosome, gene, hit.getGeneMin(), hit.getGeneMax(), hit.getStrand(), null); //null for phase - is this ok?
        gene.addFeatureLoc(rfamLoc);
        
        /* Determine transcriptType and construct unique name for the transcript */
        String transcriptType = hit.getRnaType();
        Class transcriptClass; //Check the usage of Class here
        if(transcriptType.length()>3 && (transcriptType.substring(0,3)).equalsIgnoreCase("snor")){ //If first 4 letters are SNOR
            transcriptType = "snoRNA";
            transcriptClass = SnoRNA.class;
        }else if(transcriptType.equalsIgnoreCase("trna")){
            transcriptType = "tRNA";
            transcriptClass = TRNA.class;
        }else{
            transcriptType = "ncRNA"; /* All other types called non-coding RNA. The specific RNA names will be stored as dbxrefs */
            transcriptClass = NcRNA.class;
        }
        
        String transcriptUniqueName = (geneUniqueName.concat(":")).concat(transcriptType);
        
        /*Create the transcript which also creates the corresponding featureloc (sourcefeature=chromosome) */
        Transcript transcript = gene.makeTranscript(transcriptClass, transcriptUniqueName, hit.getGeneMin(), hit.getGeneMax());
                  
        /* Score analysis feature */
        AnalysisFeature analysisFeature = transcript.createAnalysisFeature(analysis, hit.getScore(), null); 
        
        /* Create dbxref (db name = RFAM) */
        DbXRef dbxref = objectManager.getDbXRef(DBNAME, hit.getAccession(), hit.getRnaType());
        transcript.setDbXRef(dbxref);
        
        /* Commit everything to database */
        sequenceDao.persist(dbxref);
        sequenceDao.persist(gene);
        sequenceDao.persist(analysisFeature);
         
     
    }
    
    @Transactional
    public void clear(final String organismCommonName, final String analysisProgram) throws HibernateException, SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        session.doWork(new Work() {
               public void execute(Connection connection) throws SQLException {
                    new ClearRfam(connection, organismCommonName, analysisProgram).clear();
               }
        });
    }
    
    
   
  
}
    
    /**
     * This class represents an Rfam file. Each line in the file is read and compared to the expected pattern. If the pattern
     * matches, a 'hit' is added to the list of hits.
     * 
     * @author nds
     *
     */
    
    class RfamFile {
        private static final Logger logger = Logger.getLogger(RfamFile.class);
        private List<RfamHit> hits = new ArrayList<RfamHit>();

        public RfamFile(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while (null != (line = reader.readLine())) { //While not end of file
                if(0 < line.length()){
                    StringBuilder sb = new StringBuilder(line);
                    sb.append('\n');
                    logger.trace(sb);
                    parseLine(sb);
                }
            }       
        }

        public Collection<RfamHit> hits() {
            return hits;
        }
        
        private final Pattern LINE_PATTERN = Pattern.compile("(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\n"); 
              
        private void parseLine(CharSequence line) {
                
            Matcher matcher = LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                String  chromosomeName = matcher.group(1);
                int geneMin = Integer.parseInt(matcher.group(2));
                int geneMax = Integer.parseInt(matcher.group(3));
                String accession = matcher.group(4);
                int rnaMin = Integer.parseInt(matcher.group(5));
                int rnaMax = Integer.parseInt(matcher.group(6));
                String score = matcher.group(7);
                String rnaType = matcher.group(8);
                
                short strand = +1; //By default gene assumed to be on +ve strand
                
                if(geneMin > geneMax){ //Switching values since these are in opposite as the gene is on the opposite strand
                    int temp = geneMin;
                    geneMin = geneMax;
                    geneMax = temp;
                    strand = -1;
                }
   
                hits.add(new RfamHit(chromosomeName, geneMin, geneMax, accession, rnaMin, rnaMax, score, rnaType, strand));
              
            }
            else {
                logger.error("Failed to parse line:\n" + line);
                /* Probably should throw a parser error here and proceed only if the option is set to ignore errors */
            }
            
        }
    }

    /**
     *  This class corresponds to a line in the Rfam file
     *  
     */
    class RfamHit {
        
        private String  chromosomeName = null;
        private int geneMin = 0;
        private int geneMax = 0;
        private String accession = null;
        private int rnaMin = 0;
        private int rnaMax = 0;
        private String score = null;
        private String rnaType = null;
        private short strand = 0; 
      
        public RfamHit(String chromosomeName, int geneMin, int geneMax, String accession, int rnaMin, int rnaMax, String score, String rnaType, short strand) {
            this.chromosomeName = chromosomeName;
            this.geneMin = geneMin;
            this.geneMax = geneMax;
            this.accession = accession;
            this.rnaMin = rnaMin;
            this.rnaMax = rnaMax;
            this.score = score;
            this.rnaType = rnaType;
            this.strand = strand;
        }
        
        public String getChromosomeName() {
            return chromosomeName;
        }

        public int getGeneMin() {
            return geneMin;
        }

        public int getGeneMax() {
            return geneMax;
        }

        public String getAccession() {
            return accession;
        }

        public int getRnaMin() {
            return rnaMin;
        }
        
        public int getRnaMax() {
            return rnaMax;
        }

        public String getScore() {
            return score;
        }

        public String getRnaType() {
            return rnaType;
        }
        
        public short getStrand(){
            return strand;
        }

   }
    

