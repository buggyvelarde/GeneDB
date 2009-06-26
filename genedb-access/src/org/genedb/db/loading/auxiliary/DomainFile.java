package org.genedb.db.loading.auxiliary;

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


/**
 * The DomainFile class represents a an output file from a polypeptide domain prediction algorithm, such as pfam_scan or Prosite,
 * as a collection of @{link DomainRow}s.
 * The DomainRow class is an interface implemented by specific classes such as PfamRow and PrositeRow which each represent a row of 
 * the input file.
 * 
 * @author art
 * @author rh11
 * @author te3
 *
 */
interface DomainRow {

	public String db();
	public DomainAcc acc();
	public String key();	
	public String nativeAcc();	
	public String nativeDesc();	
	public String nativeProg();		
	public int lineNumber();
	public int fmin();
	public int fmax();
	public String score();
	public String evalue();
}


class PfamRow implements DomainRow {

    int lineNumber;
    String key, nativeProg, db, nativeAcc, nativeDesc, score, evalue, version;
    DomainAcc acc = DomainAcc.NULL;
    int fmin, fmax;
    
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
     * @param rowFields a line of the input file
     */
    public PfamRow(int lineNumber, String row) {
        this(lineNumber, row.split("\\s+"));
    }

    /**
     * Convert a row of an Pfam output file to an PfamRow object.
     *
     * @param lineNumber the line number of this line in the input file.
     *          Used to produce more helpful diagnostics if there's a
     *          problem decoding the line.
     * @param rowFields an array containing the fields in the file.
     *  In the actual file, fields are separated by multiple space characters.
     */
    public PfamRow(int lineNumber, String[] rowFields) {

    	if (rowFields.length == 15 && rowFields[COL_NATIVE_ACC].substring(0, 2).equals("PF") && rowFields[COL_SIG].equals("1")) { 

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
    	}
    }
    
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
	

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s/%s: %s (%s), location %d-%d",
            key, acc.getId(), nativeDesc, nativeProg, fmin, fmax));
        return sb.toString();
    }
}

class PrositeRow implements DomainRow {

    int lineNumber;
    String key, nativeProg, db, nativeAcc, nativeDesc, nativeName, version;
    DomainAcc acc = DomainAcc.NULL;
    int fmin, fmax;
    
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

    	if (rowFields.length == 7 && rowFields[COL_NATIVE_ACC].substring(0, 2).equals("PS")) { 

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
	public String name() {
		return nativeName;
	}		
	public String score() {
		return null;
	}	
	public String evalue() {
		return null;
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

    public DomainFile(String analysisProgram, InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader( inputStream ) );

        String line;
        int lineNumber = 0;
        Set<String> unrecognisedProgs = new HashSet<String>();
        while (null != (line = br.readLine())) {
            lineNumber++;
            
            DomainRow row;
            if (analysisProgram.equals("pfam_scan")) {
            	row = new PfamRow(lineNumber, line);
            }
            else if (analysisProgram.equals("prosite")) {
            	row = new PrositeRow(lineNumber, line);
            }
            else {
                throw new IllegalArgumentException(String.format("Loader for program '%s' has not been implemented", analysisProgram));
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
