package org.genedb.db.loading.alternative;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an EMBL file.
 *
 * The parser is reasonably forgiving on the macroscopic level - it doesn't
 * care what order the sections are in, for example - but generally quite strict
 * about the syntax of individual constructs.
 *
 * @author rh11
 */
public class EmblFile {
    private static final Logger logger = Logger.getLogger(EmblFile.class);

    public EmblFile(File inputFile) throws IOException, ParsingException {
        this (inputFile.toString(), new BufferedReader(new FileReader(inputFile)));
    }

    public EmblFile(String inputFile, BufferedReader reader) throws IOException, ParsingException {
        String line;
        while (null != (line = reader.readLine())) {
            processLine(inputFile, line);
        }

        if (idSection == null) {
            throw new DataError(inputFile, "Found no ID line");
        }
        if (sequenceSection == null) {
            throw new DataError(inputFile, "Found no sequence data");
        }

        logger.info(String.format("Loaded '%s' from '%s'", getAccession(), inputFile));
    }

    private static final Pattern linePattern = Pattern.compile("(ID|AC|PR|DT|DE|KW|OS|OC|OG|RN|RC|RP|RX|RG|RA|RT|RL|DR|CC|AH|AS|FH|FT|XX|SQ|CO|  |//)(?:   (.*))?");

    private int lineNumber = 0;
    private String  currentSectionIdentifier = null;
    private Section currentSection = null;
    private List<Section> sections = new ArrayList<Section>();

    private void processLine(String inputFile, String line) throws ParsingException {
        ++ lineNumber;
        Matcher matcher = linePattern.matcher(line);
        if (!matcher.matches()) {
            throw new SyntaxError(inputFile, lineNumber, line);
        }
        String identifier = matcher.group(1);
        String data = matcher.group(2);

        if (!identifier.equals(currentSectionIdentifier)) {
            if (currentSection != null) {
                currentSection.finished();
            }
            currentSectionIdentifier = identifier;
            currentSection = createSection(identifier);
            sections.add(currentSection);
        }
        try {
            currentSection.addData(data);
        } catch (ParsingException e) {
            e.setLocation(inputFile, lineNumber);
            throw e;
        }
    }

    private static final Map<String,Class<? extends Section>> sectionTypeByIdentifier = new HashMap<String,Class<? extends Section>>() {{
        put("ID", IDSection.class);
        put("FH", SilentlyIgnoredSection.class);
        put("FT", FeatureTable.class);
        put("CO", ContigSection.class);
        put("SQ", SequenceHeaderSection.class);
        put("  ", SequenceSection.class);
        put("XX", SilentlyIgnoredSection.class);
        put("//", SilentlyIgnoredSection.class);
    }};
    /*
     * Use a factory method to create Sections, so we can use
     * different subclasses for different types of section.
     */
    private Section createSection(String identifier) {
        Class<? extends Section> sectionType = sectionTypeByIdentifier.get(identifier);
        if (sectionType == null) {
            return new UnknownSection(identifier);
        } else {
            try {
                return sectionType.getDeclaredConstructor(EmblFile.class).newInstance(this);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private abstract class Section {
        public abstract void addData(String data) throws ParsingException;
        @SuppressWarnings("unused")
        public void finished() throws ParsingException {}
    }



    /* ID section */

    private static final Pattern idPattern = Pattern.compile("([^;]+); SV (\\d+); (circular|linear); ([^;]+);" +
                " (CON|ANN|PAT|EST|GSS|HTC|HTG|MGA|WGS|TPA|STS|STD);" +
                " (PHG|ENV|FUN|HUM|INV|MAM|VRT|MUS|PLN|PRO|ROD|SYN|TGN|UNC|VRL); (\\d+) BP\\.");
    private IDSection idSection = null;
    private class IDSection extends Section {
        IDSection() throws DataError {
            if (idSection != null) {
                throw new DataError("Found more than one ID line");
            }
            idSection = this;
        }

        String accession, topology, moleculeType, dataClass, taxonomicDivision;
        int version, sequenceLength;
        boolean alreadySeen = false;
        @Override
        public void addData(String data) throws ParsingException {
            if (data == null) {
                // Ignore empty ID lines. They're technically illegal, but some of our files have them.
                return;
            }
            if (alreadySeen) {
                throw new DataError("Found more than one ID line");
            }
            Matcher matcher = idPattern.matcher(data);
            if (!matcher.matches()) {
                throw new SyntaxError("Failed to parse ID line: " + data);
            }
            accession = matcher.group(1);
            version = Integer.parseInt(matcher.group(2));
            topology = matcher.group(3);
            moleculeType = matcher.group(4);
            dataClass = matcher.group(5);
            taxonomicDivision = matcher.group(6);
            sequenceLength = Integer.parseInt(matcher.group(7));
        }
    }


    /* SQ line */

    private boolean seenSequenceHeader = false;;
    private class SequenceHeaderSection extends Section {
        SequenceHeaderSection() {
            // empty
        }
        @Override
        public void addData(@SuppressWarnings("unused") String data) throws ParsingException {
            if (seenSequenceHeader) {
                throw new DataError("Found more than one SQ line");
            }
            seenSequenceHeader = true;
        }
    }


    /* CO section */

    private ContigSection contigSection = null;
    private class ContigSection extends Section {
        ContigSection() throws DataError {
            if (contigSection != null) {
                throw new DataError("More than one CO section found");
            }
            contigSection = this;
        }
        private StringBuilder allData = new StringBuilder();
        @Override
        public void addData(String data) throws ParsingException {
            allData.append(data);
        }
        @Override
        public void finished() throws ParsingException {
            EmblLocation locations = EmblLocation.parse(allData.toString());
            if (!(locations instanceof EmblLocation.Join)) {
                throw new DataError("The CO section is not a join(...) location");
            }
            this.contigLocations = (EmblLocation.Join) locations;
        }
        private EmblLocation.Join contigLocations;
    }


    /* FT section */

    /**
     * Does the string contain an even number of double-quotes?
     * @param string
     * @return <code>true</code> if string has an even number of double-quotes,
     *          or <code>false</code> if it has an odd number.
     */
    private static boolean quotesMatch(String string) {
        boolean even = true;
        for (char c: string.toCharArray()) {
            if (c == '"') {
                even = !even;
            }
        }
        return even;
    }

    private static final String symbolPattern = "[\\w'*-]*[A-Za-z][\\w'*-]*";
    private static final Pattern qualifierPattern = Pattern.compile("/(" + symbolPattern + ")(?:=(.*))?");
    private static final Pattern quotedStringPattern = Pattern.compile("\"((?:[^\"]|\"\")*)\"");
    private FeatureTable featureTable = null;
    class FeatureTable extends Section {
        class Feature {
            String type;
            EmblLocation location;
            List<Qualifier> qualifiers = new ArrayList<Qualifier>();

            /**
             * Get the values of the named qualifier. If the qualifier does not appear at all,
             * an empty list is returned. If it appears multiple times, the values are in order
             * of appearance.
             * @param key the name of the qualifier
             * @return a list of values
             */
            public List<String> getQualifierValues(String key) {
                List<String> ret = new ArrayList<String>();
                for (Qualifier qualifier: qualifiers) {
                    if (qualifier.name.equals(key)) {
                        ret.add(qualifier.value);
                    }
                }
                return ret;
            }

            public boolean hasQualifier(String key) {
                return !getQualifierValues(key).isEmpty();
            }

            public String getQualifierValue(String key) throws DataError {
                List<String> values = getQualifierValues(key);
                if (values.isEmpty()) {
                    return null;
                }
                if (values.size() > 1) {
                    throw new DataError(String.format("The qualifier '%s' appears more than once in feature '%s'",
                        key, type));
                }
                return values.get(0);
            }

            public String getUniqueName() throws DataError {
                String temporarySystematicId = this.getQualifierValue("temporary_systematic_id");
                String systematicId = this.getQualifierValue("systematic_id");

                if (temporarySystematicId != null && systematicId != null) {
                    throw new DataError(String.format("%s feature has both /systematic_id and /temporary_systematic_id", this.type));
                }
                else if (temporarySystematicId != null) {
                    return temporarySystematicId;
                } else if (systematicId != null) {
                    return systematicId;
                } else {
                    throw new DataError(String.format("%s feature has neither /systematic_id nor /temporary_systematic_id", this.type));
                }
            }


            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                for (Qualifier qualifier: qualifiers) {
                    if (sb.length() > 0) {
                        sb.append("; ");
                    }
                    sb.append(qualifier);
                }
                return String.format("%s at %s: %s", type, location, sb);
            }
        }
        private class Qualifier {
            String name, value;
            boolean valueIsQuoted;
            public Qualifier(String name, String value, boolean valueIsQuoted) {
                this.name = name;
                this.value = value;
                this.valueIsQuoted = valueIsQuoted;
            }

            public Qualifier(String name) {
                this(name, null, false);
            }

            @Override
            public String toString() {
                String format;
                if (valueIsQuoted) {
                    format = "/%s=\"%s\"";
                } else if (value == null) {
                    format = "/%s";
                } else {
                    format = "/%s=%s";
                }
                return String.format(format, name, value);
            }
        }

        FeatureTable() throws DataError {
            if (featureTable != null) {
                throw new DataError("Found more than one FT section");
            }
            featureTable = this;
        }
        private List<Feature> features = new ArrayList<Feature>();
        public Iterable<Feature> getFeatures() {
            return features;
        }

        private Feature currentFeature = null;
        private StringBuilder currentLocation = null;
        @Override
        public void addData(String data) throws ParsingException {
            String featureType = data.substring(0, 16).trim();
            String featureData = data.substring(16);
            if ("".equals(featureType)) {
                // continuation of current feature
                if (currentLocation != null) {
                    parseLocationLine(featureData);
                } else {
                    parseQualifierLine(featureData);
                }
            } else {
                if (currentLocation != null) {
                    throw new SyntaxError("Feature found while location incomplete");
                }
                if (currentQualifier != null) {
                    throw new SyntaxError("Feature found while qualifier incomplete");
                }

                finished();
                currentFeature = new Feature();
                currentFeature.type = featureType;
                if (featureData.endsWith(",")) {
                    // Location is split over multiple lines
                    currentLocation = new StringBuilder(featureData);
                } else {
                    currentFeature.location = EmblLocation.parse(featureData);
                }
            }
        }

        @Override
        public void finished() {
            if (currentFeature != null) {
                features.add(currentFeature);
            }
        }

        private void parseLocationLine(String line) throws ParsingException {
            currentLocation.append(line);
            if (! line.endsWith(",")) {
                currentFeature.location = EmblLocation.parse(currentLocation.toString());
                currentLocation = null;
            }
        }

        private String currentQualifier = null;
        private StringBuilder currentString = null;
        private void parseQualifierLine(String data) throws ParsingException {
            if (currentString != null) {
                // There's a quoted string on a previous line that hasn't been closed
                currentString.append(' ');
                if (quotesMatch(data)) {
                    // The string continues on the next line
                    currentString.append(data.replaceAll("\"\"", "\""));
                }
                else {
                    // This is the last line of the string
                    if (! data.endsWith("\"")) {
                        throw new ParsingException("Failed to parse string data: unbalanced quotes");
                    }
                    currentString.append(data.substring(0, data.length() - 1).replaceAll("\"\"", "\""));
                    currentFeature.qualifiers.add(new Qualifier(currentQualifier, currentString.toString(), true));

                    currentQualifier = null;
                    currentString = null;
                }
            } else {
                // We are not in the middle of a quoted string, so expect a qualifier
                Matcher qualifierMatcher = qualifierPattern.matcher(data);
                if (!qualifierMatcher.matches()) {
                    throw new ParsingException(String.format("Expected a qualifier, found '%s'", data));
                }
                String qualifierName = qualifierMatcher.group(1);
                String qualifierData = qualifierMatcher.group(2);

                if (qualifierData == null) {
                    // e.g. /pseudo
                    currentFeature.qualifiers.add(new Qualifier(qualifierName));
                }
                else {
                    Matcher quotedStringMatcher = quotedStringPattern.matcher(qualifierData);
                    if (quotedStringMatcher.matches()) {
                        // Quoted string all on this line, like /foo="bar"
                        currentFeature.qualifiers.add(new Qualifier(qualifierName, quotedStringMatcher.group(1).replaceAll("\"\"", "\""), true));
                    } else if (qualifierData.startsWith("\"")) {
                        // Quoted string that continues on the next line, e.g. /foo="bar "" baz ...\n
                        if (quotesMatch(qualifierData)) {
                            throw new ParsingException("Failed to parse string data: unbalanced quotes");
                        }
                        currentQualifier = qualifierName;
                        currentString = new StringBuilder(qualifierData.substring(1).replaceAll("\"\"", "\""));
                    } else {
                        // Not a quoted string. Treat the qualifier value as a simple identifier.
                        currentFeature.qualifiers.add(new Qualifier(qualifierName, qualifierData, false));
                    }
                }
            }
        }
    }


    /* Sequence data */

    private static final Pattern sequencePattern = Pattern.compile("((?:\\w{10} ){0,5}\\w{1,10})\\s+(\\d+)");
    private SequenceSection sequenceSection = null;
    private class SequenceSection extends Section {
        SequenceSection() throws DataError {
            if (sequenceSection != null) {
                throw new DataError("Found more than one sequence data section");
            }
            sequenceSection = this;
        }
        StringBuilder sequence = new StringBuilder();
        @Override
        public void addData(String data) throws ParsingException {
            Matcher matcher = sequencePattern.matcher(data);
            if (!matcher.matches()) {
                throw new SyntaxError("Failed to parse sequence data: " + data);
            }
            sequence.append(matcher.group(1).replaceAll("\\s", ""));
        }

        public String getSequence() {
            return sequence.toString();
        }
    }


    /* Other sections */

    private class UnknownSection extends Section {
        String identifier;
        UnknownSection(String identifier) {
            this.identifier = identifier;
        }
        @Override
        public void addData(String data) {
            logger.warn(String.format("Ignoring: %s   %s", identifier, data));
        }
    }

    private class SilentlyIgnoredSection extends Section {
        SilentlyIgnoredSection() {}
        @Override
        @SuppressWarnings("unused")
        public void addData(String data) {}
    }


    /* Accessors */

    public String getAccession() {
        return idSection.accession;
    }
    public int getSequenceVersion() {
        return idSection.version;
    }
    public String getTopology() {
        return idSection.topology;
    }
    public String getMoleculeType() {
        return idSection.moleculeType;
    }
    public String getDataClass() {
        return idSection.dataClass;
    }
    public String getTaxonomicDivision() {
        return idSection.taxonomicDivision;
    }
    public int getSequenceLength() {
        return idSection.sequenceLength;
    }

    public String getSequence() {
        return sequenceSection.getSequence().toString();
    }

    public FeatureTable getFeatureTable() {
        return featureTable;
    }

    public EmblLocation.Join getContigLocations() {
        if (contigSection == null) {
            return null;
        }
        return contigSection.contigLocations;
    }
}