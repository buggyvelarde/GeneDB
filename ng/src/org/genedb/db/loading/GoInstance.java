package org.genedb.db.loading;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a specific GO entry.
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
public class GoInstance {

    private List<String> qualifiers = new ArrayList<String>(0);
    private String id;
    private String ref;
    private String withFrom;
    private GoEvidenceCode evidence = GoEvidenceCode.NR;
    private GoAspect subtype;
    private String name;
    private String date;
    private String attribution;
    private String residue;

    protected static final Logger logger = Logger.getLogger(GoInstance.class);

    private static String today;
    static {
        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
        today = dFormat.format(new Date());
    }

    /**
     * Get the value of date.
     *
     * @return value of date.
     */
    public String getDate() {
        if (date == null) {
            return today;
        }
        return date;
    }

    /**
     * Set the date.
     *
     * @param date The date, in ISO format without dashes: for example, "<code>19760924</code>".
     */
    public void setDate(String date) {
        if (date == null) {
            throw new NullPointerException("Date is null");
        }
        if (!date.matches("\\d{8}")) {
            throw new IllegalArgumentException("Date format is invalid (" + date + "); should be eight digits");
        }
        
        this.date = date;
    }

    /**
     * Get the subtype.
     *
     * @return the GO subtype.
     */
    public GoAspect getSubtype() {
        return subtype;
    }

    private enum GoAspect {PROCESS, FUNCTION, COMPONENT};
    private static final Map<String, GoAspect> aspectsByUcString = new HashMap<String, GoAspect>();
    static {
        aspectsByUcString.put("P", GoAspect.PROCESS);
        aspectsByUcString.put("F", GoAspect.FUNCTION);
        aspectsByUcString.put("C", GoAspect.COMPONENT);

        aspectsByUcString.put("PROCESS", GoAspect.PROCESS);
        aspectsByUcString.put("FUNCTION", GoAspect.FUNCTION);
        aspectsByUcString.put("COMPONENT", GoAspect.COMPONENT);

        aspectsByUcString.put("BIOLOGICAL PROCESS", GoAspect.PROCESS);
        aspectsByUcString.put("MOLECULAR FUNCTION", GoAspect.FUNCTION);
        aspectsByUcString.put("CELLULAR COMPONENT", GoAspect.COMPONENT);
}
    /**
     * Set the subtype.
     *
     * @param subtype The GO subtype.
     *                May be specified as:<ul>
     *                <li> A single character: 'P', 'C' or 'F';
     *                <li> A single word: "process", "component" or "function";
     *                <li> The full phrase: "biological process", "cellular component", or "molecular function".
     *                </ul>
     *                Capitalisation is ignored: the string is handled case-insensitively.
     */
    public void setSubtype(String subtype) {
        String ucSubtype = subtype.toUpperCase();
        if (!aspectsByUcString.containsKey(ucSubtype)) {
            throw new IllegalArgumentException(String.format("Unrecognised GO subtype '%s'", subtype)); 
        }
        this.subtype = aspectsByUcString.get(ucSubtype);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the list of qualifiers.
     *
     * @return List of qualifiers.
     */
    public List<String> getQualifierList() {
        if (qualifiers == null) {
            return Collections.unmodifiableList(new ArrayList<String>(0));
        }
        return qualifiers;
    }

    /**
     * Add qualifiers.
     *
     * @param qualifiers A pipe-separated list of qualifiers
     */
    public void addQualifier(String qualifiers) {
        for (String qualifier: qualifiers.split("\\|")) {
            this.qualifiers.add(qualifier);
        }
    }

    private static Set<String> validQualifiers = new HashSet<String>();
    static { Collections.addAll(validQualifiers, "NOT", "contributes_to", "colocalizes_with"); }

    private boolean isQualifierValid(String qualifier) {
        return validQualifiers.contains(qualifier);
    }

    public String getQualifierDisplay(boolean officialOnly, String def, String separator,
            boolean replaceUnderscore) {
        StringBuffer ret = new StringBuffer();
        List<String> allQualifier = getQualifierList();
        if (allQualifier == null || allQualifier.size() == 0) {
            return def;
        }
        List<String> filteredList = null;
        if (!officialOnly) {
            filteredList = allQualifier;
        } else {
            filteredList = new ArrayList<String>();
            for (String qualifier : allQualifier) {
                if (isQualifierValid(qualifier)) {
                    filteredList.add(qualifier);
                }
            }
        }
        if (filteredList.isEmpty()) {
            return def;
        }
        for (String qualifier : filteredList) {
            if (ret.length() != 0) {
                ret.append(separator);
            }
            if (replaceUnderscore) {
                ret.append(qualifier.replaceAll("_", " "));
            } else {
                ret.append(qualifier);
            }
        }
        return ret.toString();
    }

    public void setWithFrom(String withFrom, GoEvidenceCode evidence) {
        switch (evidence) {
        case IC:
        case IGI:
        case IPI:
        case IEA:
        case ISS:
            setWithFrom(withFrom);
            break;
        default:
            logger.warn("Attempting to set with/from for evidence code of '"
                    + evidence.getDescription() + "'");
            break;
        }
    }

    public void setWithFrom(String withFrom) {
        this.withFrom = withFrom;
    }

    /**
     * Get the accession number of the Classification.
     *
     * @return The accession number
     */
    public String getId() {
        return id;
    }

    private static final Pattern GO_ID_PATTERN = Pattern.compile("(?:GO:)?(\\d{7})");
    /**
     * Set the accession number of the entry
     *
     * @param v The accession number
     */
    public void setId(String id) throws DataError {
        Matcher matcher = GO_ID_PATTERN.matcher(id);
        if (!matcher.matches()) {
            throw new DataError("GO id doesn't look right '" + id + "'");
        }
        this.id = matcher.group(1);
    }

    /**
     * Get the value of ref.
     *
     * @return Value of ref.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Set the value of ref.
     *
     * @param v Value to assign to ref.
     */
    public void setRef(String v) {
        this.ref = v;
    }

    /**
     * Get the value of evidence.
     *
     * @return Value of evidence.
     */
    public GoEvidenceCode getEvidence() {
        return evidence;
    }

    /**
     * Set the value of evidence.
     *
     * @param v Value to assign to evidence.
     */
    public void setEvidence(GoEvidenceCode evidence) {
        this.evidence = evidence;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getWithFrom() {
        return this.withFrom;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }
}
