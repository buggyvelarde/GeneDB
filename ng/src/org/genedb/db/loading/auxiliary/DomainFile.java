package org.genedb.db.loading.auxiliary;

import org.genedb.db.loading.GoEvidenceCode;
import org.genedb.db.loading.GoInstance;
import org.genedb.db.loading.ParsingException;
import org.genedb.util.TwoKeyMap;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The DomainFile class represents a an output file from a polypeptide domain prediction algorithm, such as pfam_scan or Prosite,
 * as a collection of @{link DomainRow}s.
 * The DomainRow class is an abstract class extended by specific classes such as PfamRow and PrositeRow which each represent a row of
 * the input file.
 *
 * @author art
 * @author rh11
 * @author te3
 *
 */
abstract class DomainRow {

    protected int lineNumber;
    protected DomainAcc acc = DomainAcc.NULL;
    protected Boolean comment;
    protected String key, nativeProg, db, nativeAcc, nativeDesc, score, evalue;
    protected int fmin, fmax;
    protected ISOFormatDate date;

    public String db() {
        return db;
    }
    public DomainAcc acc() {
        return acc;
    }
    public String key() {
        return key;
    }
    public String nativeAcc() {
        return nativeAcc;
    }
    public String nativeDesc() {
        return nativeDesc;
    }
    public String nativeProg() {
        return nativeProg;
    }
    public int lineNumber() {
        return lineNumber;
    }
    public int fmin() {
        return fmin;
    }
    public int fmax() {
        return fmax;
    }
    public String score() {
        return score;
    }
    public String evalue() {
        return evalue;
    }
    public Boolean comment() {
        return comment;
    }
    public String getDate() {
        return date.withDashes();
    }
    public abstract Set<GoInstance> getGoTerms();
    public abstract String getGoTermComment();
}


/**
 * Represents a single row of an Interpro output file.
 *
 * @author art
 * @author rh11
 * @author te3
 */

class InterProRow extends DomainRow {
    private static final Logger logger = Logger.getLogger(InterProRow.class);

    Set<GoInstance> goTerms = new HashSet<GoInstance>();

    // The columns we're interested in:
    private static final int COL_KEY         = 0;
    private static final int COL_NATIVE_PROG = 3;
    private static final int COL_NATIVE_ACC  = 4;
    private static final int COL_NATIVE_DESC = 5;
    private static final int COL_FMIN        = 6;
    private static final int COL_FMAX        = 7;
    private static final int COL_SCORE       = 8;
    private static final int COL_DATE        = 10;
    private static final int COL_ACC         = 11;
    private static final int COL_DESC        = 12;
    private static final int COL_GO          = 13;

    private static final HashMap<String, String> dbByProg = new HashMap<String, String>() {{
        put("HMMPfam", "Pfam");
        put("FPrintScan", "PRINTS");
        put("ProfileScan", "Prosite");
        put("ScanRegExp", "Prosite");
        put("HMMSmart", "SMART");
        put("BlastProDom", "ProDom");
        put("HMMTigr", "TIGR_TIGRFAMS");
        put("HMMPIR", "PIRSF");
        put("HMMPanther", "PANTHER");

        // These three have not been seen in the P. falciparum output, at least.
        // Are they still possible?
        put("Superfamily", "Superfamily");
        put("superfamily", "Superfamily");
        put("ScanProsite", "Prosite");
    }};

    /**
     * Convert a row of an Interpro output file to an InterproRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param rowFields a line of the input file
     */
    InterProRow(int lineNumber, String row) {
        this(lineNumber, row.split("\t"));
    }

    /**
     * Convert a row of an Interpro output file to an InterproRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param rowFields an array containing the fields in the file.
     *  In the actual file, fields are separated by tab characters.
     */
    InterProRow(int lineNumber, String[] rowFields) {

        this.comment = false;
        if (rowFields.length == 1 || rowFields[COL_KEY].substring(0, 1).equals("#")) { //blank line or comment
            this.comment = true;
        }

        this.lineNumber = lineNumber;
        this.key        = rowFields[COL_KEY];
        this.nativeProg = rowFields[COL_NATIVE_PROG];
        this.db         = dbByProg.get(nativeProg);
        this.nativeAcc  = rowFields[COL_NATIVE_ACC];
        this.nativeDesc = rowFields[COL_NATIVE_DESC];
        this.fmin       = Integer.parseInt(rowFields[COL_FMIN]) - 1; // -1 because we're converting to interbase
        this.fmax       = Integer.parseInt(rowFields[COL_FMAX]);
        this.score      = rowFields[COL_SCORE];
        this.date       = new ISOFormatDate(rowFields[COL_DATE]);

        if (rowFields.length > COL_ACC && !rowFields[COL_ACC].equals("NULL")) {
            this.acc = new DomainAcc(rowFields[COL_ACC], rowFields[COL_DESC]);
        }
        if (rowFields.length > COL_GO)
            parseGoString(lineNumber, rowFields[COL_GO]);
    }
    private static final Pattern goTermPattern
        = Pattern.compile("\\G(Cellular Component|Biological Process|Molecular Function): (.*?) \\(GO:(\\d{7})\\)(?:, |\\z)");

    private void parseGoString(int lineNumber, String goString) {
        Matcher matcher = goTermPattern.matcher(goString);
        while (matcher.find()) {
            String type = matcher.group(1);
            String description = matcher.group(2);
            String goId = matcher.group(3);

            logger.debug(String.format("Parsed GO term: %s/%s/%s", type, description, goId));

            GoInstance goTerm = new GoInstance();
            try {
                goTerm.setId(goId);
                goTerm.setEvidence(GoEvidenceCode.IEA);
                goTerm.setWithFrom("InterPro:" + this.acc.getId());
                goTerm.setRef("GOC:interpro2go");
                goTerm.setDate(this.date.withoutDashes());
                goTerm.setSubtype(type);
            } catch (ParsingException e) {
                throw new RuntimeException(e);
            }

            /*
             * We do not set the <code>geneName</code> field of the GoInstance,
             * because a) the key is not necessarily a gene name, and b) the
             * method FeatureUtils#createGoEntries does not use the geneName
             * field.
             */

            this.goTerms.add(goTerm);
        }
        if (!matcher.hitEnd())
            logger.error(String.format("Failed to completely parse GO terms '%s' on line %d", goString, lineNumber));
    }


    public Set<GoInstance> getGoTerms() {
        return this.goTerms;
    }
    public String evalue() {
        return null;
    }

    public String getGoTermComment() {
        return("From iprscan");
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s/%s: %s (%s), location %d-%d",
            key, acc.getId(), nativeDesc, nativeProg, fmin, fmax));
        for (GoInstance goTerm: goTerms) {
            sb.append(String.format("\n\t%s (GO:%s)", goTerm.getSubtype(), goTerm.getId()));
        }
        return sb.toString();
    }
}


class PfamRow extends DomainRow {

    Pfam2GoFile pfam2GoFile;
    private static String today;
    static {
        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
        today = dFormat.format(new Date());
    }

    private static final Logger logger = Logger.getLogger(PfamRow.class);

    // The columns we're interested in:
    private static final int COL_KEY         = 0;
    private static final int COL_NATIVE_ACC  = 5;
    private static final int COL_NATIVE_DESC = 6;
    private static final int COL_FMIN        = 1;
    private static final int COL_FMAX        = 2;
    private static final int COL_SCORE       = 11;
    private static final int COL_EVALUE      = 12;
    private static final int COL_SIG         = 13;

    /**
     * Convert a row of an Pfam output file to an PfamRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param pfam2GoFile
     * @param rowFields a line of the input file
     */
    public PfamRow(int lineNumber, String row, Pfam2GoFile pfam2GoFile) {
        this(lineNumber, row.split("\\s+"), pfam2GoFile);
    }

    /**
     * Convert a row of an Pfam output file to an PfamRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param rowFields an array containing the fields in the file.
     *  In the actual file, fields are separated by multiple space characters.
     * @param pfam2GoFile
     */
    public PfamRow(int lineNumber, String[] rowFields, Pfam2GoFile pfam2GoFile) {

        this.comment = false;
        if (rowFields.length == 1 || rowFields[COL_KEY].substring(0, 1).equals("#")) { //blank line or comment
            this.comment = true;
        }
        else if (rowFields.length == 15 && rowFields[COL_NATIVE_ACC].substring(0, 2).equals("PF") && rowFields[COL_SIG].equals("1")) {

    		this.lineNumber = lineNumber;
    		this.key        = rowFields[COL_KEY];

    		this.nativeAcc  = rowFields[COL_NATIVE_ACC];

    		this.nativeDesc = rowFields[COL_NATIVE_DESC];
    		this.nativeProg = "pfam_scan";
    		this.db         = "Pfam";
    		this.fmin       = Integer.parseInt(rowFields[COL_FMIN]) - 1; // -1 because we're converting to interbase
    		this.fmax       = Integer.parseInt(rowFields[COL_FMAX]);
    		this.score      = rowFields[COL_SCORE];
    		this.evalue     = rowFields[COL_EVALUE];

    		if (rowFields.length > COL_NATIVE_DESC && !rowFields[COL_NATIVE_ACC].equals("NULL")) {
    			this.acc = new DomainAcc(rowFields[COL_NATIVE_ACC], rowFields[COL_NATIVE_DESC]);
    		}
            this.pfam2GoFile = pfam2GoFile;
    	}
    }

    public Set<GoInstance> getGoTerms() {

        Set<GoInstance> goTerms = new HashSet<GoInstance>();
        String pfamAccession = this.nativeAcc;
        Pfam2GoFile pfam2GoFile = this.pfam2GoFile;
        if (pfam2GoFile.getGoByPfam(pfamAccession) == null) {
            logger.debug(String.format("The domain '%s' has no mapped GO terms", pfamAccession));
            return Collections.emptySet();
        }

        for (String goAccession: pfam2GoFile.getGoByPfam(pfamAccession)) {

            try {
                GoInstance goInstance = new GoInstance();
                goInstance.setId(goAccession);
                goInstance.setDate(today);
                GoEvidenceCode evidenceCode = GoEvidenceCode.parse("IEA");
                goInstance.setEvidence(evidenceCode);
                goTerms.add(goInstance);
            } catch (ParsingException e) {
                logger.error(e);
            }
        }
        return goTerms;
    }

    public String getGoTermComment() {
        return("From Pfam2GO mapping");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s/%s: %s (%s), location %d-%d",
            key, acc.getId(), nativeDesc, nativeProg, fmin, fmax));
        return sb.toString();
    }
}

class PrositeRow extends DomainRow {

    String nativeName;

    // The columns we're interested in:
    private static final int COL_KEY         = 0;
    private static final int COL_NATIVE_ACC  = 1;
    private static final int COL_NATIVE_NAME = 5;
    private static final int COL_NATIVE_DESC = 6;
    private static final int COL_FMIN        = 2;
    private static final int COL_FMAX        = 3;

    /**
     * Convert a row of a Prosite output file to a PrositeRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param rowFields a line of the input file
     */
    public PrositeRow(int lineNumber, String row) {
        this(lineNumber, row.split("\t"));
    }

    /**
     * Convert a row of a Prosite output file to a PrositeRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param rowFields an array containing the fields in the file.
     *  In the actual file, fields are separated by multiple space characters.
     */
    public PrositeRow(int lineNumber, String[] rowFields) {

        this.comment = false;
        if (rowFields.length == 1 || rowFields[COL_KEY].substring(0, 1).equals("#")) { //blank line or comment
            this.comment = true;
        }
        else if (rowFields.length == 7 && rowFields[COL_NATIVE_ACC].substring(0, 2).equals("PS")) {

    		this.lineNumber = lineNumber;
    		this.key        = rowFields[COL_KEY];

    		this.nativeAcc  = rowFields[COL_NATIVE_ACC];
    		this.nativeName = rowFields[COL_NATIVE_NAME];
    		this.nativeDesc = rowFields[COL_NATIVE_DESC];
    		this.nativeProg = "prosite";
    		this.db         = "PROSITE";
    		this.fmin       = Integer.parseInt(rowFields[COL_FMIN]) - 1; // -1 because we're converting to interbase
    		this.fmax       = Integer.parseInt(rowFields[COL_FMAX]);

    		if (rowFields.length > COL_NATIVE_DESC && !rowFields[COL_NATIVE_ACC].equals("NULL")) {
    			this.acc = new DomainAcc(rowFields[COL_NATIVE_ACC], rowFields[COL_NATIVE_DESC]);
    		}
    	}
    }

	public String score() {
		return null;
	}
	public String evalue() {
		return null;
	}
    public Set<GoInstance> getGoTerms() {
        return Collections.emptySet(); //Prosite currently has no GO mapping
    }
    public String getGoTermComment() {
        return null;//Prosite currently has no GO mapping
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s/%s: %s (%s), location %d-%d",
            key, acc.getId(), nativeDesc, nativeProg, fmin, fmax));
        return sb.toString();
    }
}

/**
 * Represents an InterPro/Pfam/Prosite accession identifier with description,
 * as found in the last two columns of an PfamScan raw output file.
 *
 * @author rh11
 */
class DomainAcc {
    private String id, description;
    public static final DomainAcc NULL = new DomainAcc(null, null);

    public DomainAcc(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /*
     * hashCode() and equals() are auto-generated by Eclipse.
     * We need them because we want to use DomainAcc objects
     * as keys in a map.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DomainAcc other = (DomainAcc) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}

/**
 * Represents a polypeptide domain prediction output file as a collection of {@link DomainRow}s
 * keyed by gene name (or mangled polypeptide name) and Domain accession number.
 *
 * @author rh11
 */
class DomainFile {
    private static final Logger logger = Logger.getLogger(DomainFile.class);

    private TwoKeyMap<String,DomainAcc,Set<DomainRow>> rowsByKeyAndAcc
        = new TwoKeyMap<String, DomainAcc, Set<DomainRow>>();
    Pfam2GoFile pfam2GoFile;

    public DomainFile(String analysisProgram, InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader( inputStream ) );

        String line;
        int lineNumber = 0;
        Set<String> unrecognisedProgs = new HashSet<String>();
        while (null != (line = br.readLine())) {
            lineNumber++;

            DomainRow row;
            if (analysisProgram.equals("pfam_scan")) {
                //parse the pfam2go file if not already done
                if (pfam2GoFile == null) {
                    logger.info(String.format("Creating pfam2go mapping"));
                    pfam2GoFile = new Pfam2GoFile();
                }
            	row = new PfamRow(lineNumber, line, pfam2GoFile);
            }
            else if (analysisProgram.equals("prosite")) {
            	row = new PrositeRow(lineNumber, line);
            }
            else if (analysisProgram.equals("iprscan")) {
            	row = new InterProRow(lineNumber, line);
            }
            else {
                throw new IllegalArgumentException(String.format("Loader for program '%s' has not been implemented", analysisProgram));
            }

            if (row.comment().equals(true)) { //skipping comment lines
                continue;
            }
            if (row.db() == null) {
                if (!unrecognisedProgs.contains(row.nativeProg())) {
                    logger.warn(String.format("Unrecognised program '%s', first encountered on line %d", row.nativeProg(), lineNumber));
                    unrecognisedProgs.add(row.nativeProg());
                }
                continue;
            }

            if (!rowsByKeyAndAcc.containsKey(row.key(), row.acc())) {
            	rowsByKeyAndAcc.put(row.key(), row.acc(), new HashSet<DomainRow>());
	    		rowsByKeyAndAcc.get(row.key(), row.acc()).add(row);
        	}
    	}
    }

    public Set<String> keys() {
        return rowsByKeyAndAcc.firstKeySet();
    }
    public Set<DomainAcc> accsForKey(String key) {
        if (!rowsByKeyAndAcc.containsFirstKey(key))
            throw new IllegalArgumentException(String.format("Key '%s' not found", key));
        return rowsByKeyAndAcc.getMap(key).keySet();
    }
    public Set<DomainRow> rows(String key, DomainAcc acc) {
        if (!rowsByKeyAndAcc.containsKey(key, acc))
            throw new IllegalArgumentException(
                String.format("Accession number '%s' not found for key '%s'", acc, key));

        return rowsByKeyAndAcc.get(key, acc);
    }
}

/**
* Convert dates from the format "24-Sep-1976" into ISO format
* (1976-09-24 or 19760924). For example:
* <pre>
*     new ISOFormatDate("24-Sep-1976").withDashes(); // Returns "1976-09-24"
* </pre>
* @author rh11
*/
class ISOFormatDate {
   private static final Map<String, String> months = new HashMap<String, String>(12) {{
       put("Jan", "01"); put("May", "05"); put("Sep", "09");
       put("Feb", "02"); put("Jun", "06"); put("Oct", "10");
       put("Mar", "03"); put("Jul", "07"); put("Nov", "11");
       put("Apr", "04"); put("Aug", "08"); put("Dec", "12");
   }};

   private static final Pattern datePattern = Pattern.compile("(\\d\\d)-([A-Z][a-z][a-z])-(\\d{4})");

   private String year, month, day;
   /**
    * Create a converter for the specified date.
    *
    * @param date The date in format dd-Mon-yyyy, e.g. 24-Sep-1976
    * @throws IllegalArgumentException if the date cannot be parsed
    */
   public ISOFormatDate(String date) {
       Matcher matcher = datePattern.matcher(date);
       if (!matcher.matches())
           throw new IllegalArgumentException(String.format(
               "Failed to parse date '%s'", date));
       String day   = matcher.group(1);
       String month = matcher.group(2);
       String year  = matcher.group(3);

       if (!months.containsKey(month))
           throw new IllegalArgumentException(String.format(
               "Unknown month '%s' while parsing date '%s'", month, date));

       this.year = year;
       this.month = months.get(month);
       this.day = day;
   }

   /**
    * Get the date in the format <code>yyyy-mm-dd</code>.
    *
    * @return the date in format <code>yyyy-mm-dd</code>
    */
   public String withDashes() {
       return String.format("%s-%s-%s", year, month, day);
   }
   /**
    * Get the date in the format <code>yyyymmdd</code>.
    *
    * @return the date in format <code>yyyymmdd</code>
    */
   public String withoutDashes() {
       return String.format("%s%s%s", year, month, day);
   }
}


/*
 * Stores the pfam2go mappings in a Map<String, Set<String>>
 */
class Pfam2GoFile {

    Map<String, Set<String>> pfam2go;
    private static final Logger logger = Logger.getLogger(Pfam2GoFile.class);

    public Pfam2GoFile() throws IOException {

        InputStream inputStream = getClass().getResourceAsStream("/pfam2go");
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



