package org.genedb.db.loading;

import org.genedb.db.loading.EmblLoader.OverwriteExisting;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
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

    private boolean continueOnError = false;
    private String filePath;

    private OverwriteExisting overwriteExisting;

    private void dataError(DataError dataError) throws DataError {
        if (continueOnError) {
            logger.error("DataError", dataError);
        } else {
            throw dataError;
        }
    }

    private void syntaxError(SyntaxError syntaxError) throws SyntaxError {
        if (continueOnError) {
            logger.error("SyntaxError", syntaxError);
        } else {
            throw syntaxError;
        }
    }

    private void parsingException(ParsingException parsingException) throws ParsingException {
        if (continueOnError) {
            logger.error("ParsingException", parsingException);
        } else {
            throw parsingException;
        }
    }


    public EmblFile(File inputFile, Reader reader) throws IOException, ParsingException {
        this(inputFile, reader, false);
    }

    public EmblFile(File inputFile, Reader reader, boolean continueOnError, OverwriteExisting overwriteExisting) throws IOException, ParsingException {
        this (inputFile.toString(), new BufferedReader(reader), continueOnError, overwriteExisting);
    }
    public EmblFile(File inputFile, Reader reader, boolean continueOnError) throws IOException, ParsingException {
    	this (inputFile.toString(), new BufferedReader(reader), continueOnError, OverwriteExisting.NO);
    }

    public EmblFile(String inputFile, BufferedReader reader, boolean continueOnError, OverwriteExisting overwriteExisting) throws IOException, ParsingException {
        this.filePath = inputFile;
        this.continueOnError = continueOnError;
        this.overwriteExisting = overwriteExisting;

        String line;
        while (null != (line = reader.readLine())) {
            processLine(inputFile, line);
        }

        if (idSection == null) {
            dataError(new DataError(inputFile, "Found no ID line"));

            // We only get here if we're running in quickAndDirty mode, i.e. continueOnError is set.
            idSection = new IDSection();
            idSection.accession = inputFile;
        }
        if (sequenceSection == null && !overwriteExisting.toString().equals("MERGE")) {
            dataError(new DataError(inputFile, "Found no sequence data"));
        }
        if (sequenceSection != null && overwriteExisting.toString().equals("MERGE")) {
            dataError(new DataError(inputFile, "Found sequence data but running with overwriteExisting=MERGE"));
        }

        logger.info(String.format("Loaded '%s' from '%s'", getAccession(), inputFile));
    }

    private static final Pattern linePattern = Pattern.compile("(ID|AC|PR|DT|DE|KW|OS|OC|OG|RN|RC|RP|RX|RG|RA|RT|RL|DR|CC|AH|AS|FH|FT|XX|SQ|CO|  |//)(?:   (.*))?|(##.*)");

    private int lineNumber = 0;
    private String  currentSectionIdentifier = null;
    private Section currentSection = null;
    private List<Section> sections = new ArrayList<Section>();

    private void processLine(String inputFile, String line) throws ParsingException {
        ++ lineNumber;
        Matcher matcher = linePattern.matcher(line);
        if (!matcher.matches()) {
            syntaxError(new SyntaxError(inputFile, lineNumber, line));
            return;
        }

        // The EMBL format doesn't really allow arbitrary comments to be interspersed with the data.
        // But for sample files (synthetic.embl in particular) it's useful to be able to include remarks
        // in the feature table. Thus we have invented a compatible extension: any line beginning with
        // two hash marks is ignored, wherever it appears.
        if (matcher.group(3) != null) {
            return;
        }

        String identifier = matcher.group(1);
        String data = matcher.group(2);

        try {
            if (!identifier.equals(currentSectionIdentifier)) {
                if (currentSection != null) {
                    currentSection.finished();
                }
                currentSectionIdentifier = identifier;
                currentSection = createSection(identifier);
                sections.add(currentSection);
            }
            currentSection.addData(lineNumber, data);
        } catch (ParsingException e) {
            e.setLocation(inputFile, lineNumber);
            parsingException(e);
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
        put("DE", SilentlyIgnoredSection.class);
        put("KW", SilentlyIgnoredSection.class);
        put("//", SilentlyIgnoredSection.class);
    }};

    /*
     * A factory method to create Sections, so we can have
     * different subclasses for different types of section.
     */
    private Section createSection(String identifier) throws DataError {
        Class<? extends Section> sectionType = sectionTypeByIdentifier.get(identifier);
        if (sectionType == null) {
            return new UnknownSection(identifier);
        }

        Section section = null;
        try {
        	section = sectionType.getDeclaredConstructor(EmblFile.class).newInstance(this);

        	/*
        	 * For other section types, the section constructor leaves a reference to the
        	 * section in an instance variable - see IDSection, for example. Unlike the
        	 * others, FeatureTable is not an inner class: because it's complicated and
        	 * important enough to deserve its own file, and Java doesn't let you define
        	 * an inner class in a separate file. Since this is the only such case, it's
        	 * no great hardship to deal with it specially here.
        	 */
        	if (section instanceof FeatureTable) {
        		if (featureTable != null) {
        			dataError(new DataError("More than one feature table found"));
        		}
        		featureTable = (FeatureTable) section;
        	}

        	return section;
        } catch (InvocationTargetException e) {
        	// The invoked constructor threw an exception
        	Throwable targetException = e.getCause();
        	if (targetException instanceof DataError) {
        		dataError((DataError) targetException);
        		return section;
        	} else {
        		throw new RuntimeException(e);
        	}
        } catch (NoSuchMethodException e) {
        	throw new RuntimeException(e);
        } catch (InstantiationException e) {
        	throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
        	throw new RuntimeException(e);
        }
    }
    private FeatureTable featureTable = null;

    /*
     * A minor subtlety here: the abstract superclass Section is a top-level nested
     * class, though most of the concrete subclasses are inner classes. This allows
     * us to have the non-inner implementing class {@link FeatureTable}.
     */
    abstract static class Section {
        public abstract void addData(int lineNumber, String data) throws ParsingException;
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
                dataError(new DataError("Found more than one ID line"));
            }
            idSection = this;
        }

        String accession, topology, moleculeType, dataClass, taxonomicDivision;
        int version, sequenceLength;
        boolean alreadySeen = false;
        @Override
        public void addData(int lineNumber, String data) throws ParsingException {
            if (data == null) {
                // Ignore empty ID lines. They're technically illegal, but some of our files have them.
                return;
            }
            if (alreadySeen) {
                dataError(new DataError("Found more than one ID line"));
            }
            Matcher matcher = idPattern.matcher(data);
            if (!matcher.matches()) {
                logger.error("Failed to parse ID line: " + data);
                data = data.trim();
                accession = data.substring(0, data.indexOf(' '));
                if (accession.endsWith(";")) {
                    accession = accession.substring(0, accession.length() - 1);
                }
                logger.warn(String.format("Taking the sequence identifier to be '%s'", accession));
            } else {
                accession = matcher.group(1);
                version = Integer.parseInt(matcher.group(2));
                topology = matcher.group(3);
                moleculeType = matcher.group(4);
                dataClass = matcher.group(5);
                taxonomicDivision = matcher.group(6);
                sequenceLength = Integer.parseInt(matcher.group(7));
            }
        }
    }


    /* SQ line */

    private boolean seenSequenceHeader = false;;
    private class SequenceHeaderSection extends Section {
    	@SuppressWarnings("unused") // used by reflection
        SequenceHeaderSection() {
            // empty
        }
        @Override
        public void addData(int lineNumber, String data)
                throws ParsingException {
            if (seenSequenceHeader) {
                dataError(new DataError("Found more than one SQ line"));
            }
            seenSequenceHeader = true;
        }
    }


    /* CO section */

    private ContigSection contigSection = null;
    private class ContigSection extends Section {
    	@SuppressWarnings("unused") // used by reflection
        ContigSection() throws DataError {
            if (contigSection != null) {
                dataError(new DataError("More than one CO section found"));
            }
            contigSection = this;
        }
        private StringBuilder allData = new StringBuilder();
        @Override
        public void addData(int lineNumber, String data) throws ParsingException {
            allData.append(data);
        }
        @Override
        public void finished() throws ParsingException {
            EmblLocation locations = EmblLocation.parse(allData.toString());
            if (!(locations instanceof EmblLocation.Join)) {
                dataError(new DataError("The CO section is not a join(...) location"));
            }
            this.contigLocations = (EmblLocation.Join) locations;
        }
        private EmblLocation.Join contigLocations;
    }


    // The FT section is defined in a separate class, {@link FeatureTable}


    /* SQ section */

    private static final Pattern sequencePattern = Pattern.compile("((?:\\w{10} ){0,5}\\w{1,10})\\s+(\\d+)");
    private SequenceSection sequenceSection = null;
    private class SequenceSection extends Section {
    	@SuppressWarnings("unused") // used by reflection
        SequenceSection() throws DataError {
            if (sequenceSection != null) {
                dataError(new DataError("Found more than one sequence data section"));
            }
            sequenceSection = this;
        }
        StringBuilder sequence = new StringBuilder();
        @Override
        public void addData(int lineNumber, String data) throws ParsingException {
            Matcher matcher = sequencePattern.matcher(data);
            if (!matcher.matches()) {
                syntaxError(new SyntaxError("Failed to parse sequence data: " + data));
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
        public void addData(int lineNumber, String data) {
            logger.warn(String.format("Ignoring: %s   %s on line %d", identifier, data, lineNumber));
        }
    }

    private class SilentlyIgnoredSection extends Section {
    	@SuppressWarnings("unused") // used by reflection
        SilentlyIgnoredSection() {}
        @Override
        public void addData(int lineNumber, String data) {}
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
        if (idSection.taxonomicDivision != null) {
            return idSection.taxonomicDivision;
        } else {
            logger.warn("Taxonomic division unspecified (bad ID line). Assuming UNK.");
            return "UNK";
        }
    }
    public int getSequenceLength() {
        return idSection.sequenceLength;
    }

    public String getSequence() {
        if (sequenceSection == null) {
            // Only if the sequence section is missing and we're in quickAndDirty mode.
            return "";
        }
        return sequenceSection.getSequence();
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

    public String getFilePath() {
        return this.filePath;
    }
}