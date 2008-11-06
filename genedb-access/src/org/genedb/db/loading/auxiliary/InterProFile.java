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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DateFormatConverter {
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
     */
    public DateFormatConverter(String date) {
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


/**
 * Represents a single row of an Interpro output file.
 *
 * @author art
 * @author rh11
 */
class InterProRow {
    private static final Logger logger = Logger.getLogger(InterProFile.class);

    String key, nativeProg, db, nativeAcc, nativeDesc, score;
    InterProAcc acc = InterProAcc.NULL;
    int fmin, fmax;
    private DateFormatConverter date;
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
        this.key       = rowFields[COL_KEY];
        this.nativeProg = rowFields[COL_NATIVE_PROG];
        this.db         = dbByProg.get(nativeProg);
        this.nativeAcc  = rowFields[COL_NATIVE_ACC];
        this.nativeDesc = rowFields[COL_NATIVE_DESC];
        this.fmin       = Integer.parseInt(rowFields[COL_FMIN]) - 1; // -1 because we're converting to interbase
        this.fmax       = Integer.parseInt(rowFields[COL_FMAX]);
        this.score      = rowFields[COL_SCORE];
        this.date       = new DateFormatConverter(rowFields[COL_DATE]);

        if (rowFields.length > COL_ACC && !rowFields[COL_ACC].equals("NULL")) {
            this.acc = new InterProAcc(rowFields[COL_ACC], rowFields[COL_DESC]);
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

    public String getDate() {
        return date.withDashes();
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

class InterProAcc {
    private String id, description;
    public static final InterProAcc NULL = new InterProAcc(null, null);

    public InterProAcc(String id, String description) {
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
     * We need them because we want to use InterProAcc objects
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
        final InterProAcc other = (InterProAcc) obj;
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
 * Represents an InterPro output file as a collection of {@link InterProRow}s
 * keyed by gene name (or mangled polypeptide name) and InterPro accession number.
 *
 * @author rh11
 */
public class InterProFile {
    private static final Logger logger = Logger.getLogger(InterProFile.class);

    private TwoKeyMap<String,InterProAcc,Set<InterProRow>> rowsByKeyAndAcc
        = new TwoKeyMap<String, InterProAcc, Set<InterProRow>>();

    public InterProFile(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader( inputStream ) );

        String line;
        int lineNumber = 0;
        Set<String> unrecognisedProgs = new HashSet<String>();
        while (null != (line = br.readLine())) {
            lineNumber++;
            InterProRow row = new InterProRow(lineNumber, line);

            if (row.db == null) {
                if (!unrecognisedProgs.contains(row.nativeProg)) {
                    logger.warn(String.format("Unrecognised program '%s', first encountered on line %d", row.nativeProg, lineNumber));
                    unrecognisedProgs.add(row.nativeProg);
                }
                continue;
            }

            if (!rowsByKeyAndAcc.containsKey(row.key, row.acc))
                rowsByKeyAndAcc.put(row.key, row.acc, new HashSet<InterProRow>());
            rowsByKeyAndAcc.get(row.key, row.acc).add(row);
        }
    }
    public Set<String> keys() {
        return rowsByKeyAndAcc.firstKeySet();
    }
    public Set<InterProAcc> accsForKey(String key) {
        if (!rowsByKeyAndAcc.containsFirstKey(key))
            throw new IllegalArgumentException(
                String.format("Key '%s' not found", key));
        return rowsByKeyAndAcc.getMap(key).keySet();
    }
    public Set<InterProRow> rows(String key, InterProAcc acc) {
        if (!rowsByKeyAndAcc.containsKey(key, acc))
            throw new IllegalArgumentException(
                String.format("Accession number '%s' not found for key '%s'", acc, key));

        return rowsByKeyAndAcc.get(key, acc);
    }
}
