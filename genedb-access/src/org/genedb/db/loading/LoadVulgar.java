package org.genedb.db.loading;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadVulgar extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadVulgar.class);

    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.vulgar(?:\\.gz)?");
        String matchType = getPropertyWithDefault("load.matchType", null);

        logger.info(String.format("Options: organismCommonName=%s, inputDirectory=%s, fileNamePattern=%s",
                   organismCommonName, inputDirectory, fileNamePattern));

        LoadVulgar loadVulgar = new LoadVulgar(organismCommonName, matchType);

        loadVulgar.processFileOrDirectory(inputDirectory, fileNamePattern);
    }

    private VulgarLoader loader;
    private LoadVulgar(String organismCommonName, String matchType) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = applicationContext.getBean("vulgarLoader", VulgarLoader.class);
        loader.setOrganismCommonName(organismCommonName);

        if (matchType != null) {
            loader.setMatchType(matchType);
        }
    }

    @Override
    protected void processFile(File inputFile, Reader reader) throws IOException, ParsingException {
        loader.load(new VulgarFile(inputFile, reader));
    }
}

class VulgarFileException extends RuntimeException {

    public VulgarFileException() {
        super();
    }

    public VulgarFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public VulgarFileException(String message) {
        super(message);
    }

    public VulgarFileException(Throwable cause) {
        super(cause);
    }
}

class VulgarFile implements Iterable<VulgarMapping> {
    private boolean alreadyGotIterator = false;
    private File file;
    private BufferedReader br;

    VulgarFile(File file, Reader reader) {
        this.file = file;
        this.br = new BufferedReader(reader);
    }

    public Iterator<VulgarMapping> iterator() {
        if (alreadyGotIterator) {
            throw new RuntimeException("You can only get one iterator from a VulgarFile, sorry!");
        }
        return new Iterator<VulgarMapping>() {

            private String nextLine = null;
            private int lineNumber = 0;

            public boolean hasNext() {
                if (br == null) {
                    return false;
                }
                if (nextLine == null) {
                    try {
                        nextLine = br.readLine();
                        if (nextLine == null) {
                            br.close();
                            br = null;
                            return false;
                        }
                    } catch (IOException e) {
                        throw new VulgarFileException(e);
                    }
                }
                return true;
            }

            public VulgarMapping next() {
                if (br == null) {
                    return null;
                }

                try {
                    lineNumber ++;
                    if (nextLine != null) {
                        VulgarMapping vulgarMapping = new VulgarMapping(nextLine);
                        nextLine = null;
                        return vulgarMapping;
                    }

                    String line = br.readLine();
                    if (line == null) {
                        br.close();
                        br = null;
                        return null;
                    }
                    return new VulgarMapping(line);
                } catch (ParsingException e) {
                    e.setInputFile(file);
                    e.setLineNumber(lineNumber);
                    throw new VulgarFileException(e);
                } catch (IOException e) {
                    throw new VulgarFileException(e);
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }
}

enum VulgarMatchType {
    MATCH('M'),
    CODON('C'),
    GAP('G'),
    NON_EQUIVALENCED_REGION('N'),
    FIVE_PRIME_SPLICE_SITE('5'),
    THREE_PRIME_SPLICE_SITE('3'),
    INTRON('I'),
    SPLIT_CODON('S'),
    FRAMESHIFT('F');

    private VulgarMatchType(@SuppressWarnings("unused") char c) {
        // empty
    }

    static VulgarMatchType fromChar(char c) throws ParsingException {
        switch(c) {
        case 'M': return MATCH;
        case 'C': return CODON;
        case 'G': return GAP;
        case 'N': return NON_EQUIVALENCED_REGION;
        case '5': return FIVE_PRIME_SPLICE_SITE;
        case '3': return THREE_PRIME_SPLICE_SITE;
        case 'I': return INTRON;
        case 'S': return SPLIT_CODON;
        case 'F': return FRAMESHIFT;
        default: throw new SyntaxError(String.format("Unknown match type '%c'", c));
        }
    }
}

/**
 * Represents an Exonerate mapping, as described by a single line in the Vulgar format.
 *
 * @author rh11
 *
 */
class VulgarMapping {
    private static final Logger logger = Logger.getLogger(VulgarMapping.class);
    private static final Pattern vulgarPattern = Pattern.compile(
        "vulgar: (\\S+) (\\d+) (\\d+) ([+-]) (\\S+) (\\d+) (\\d+) " +
        "([+-]) (\\d+)((?: [MCGN53ISF] \\d+ \\d+)+)?(?:\\tPROM=\\d+)?\\s*");

    // Note: we deliberately support the (presumably technically invalid)
    // case where no match parts are specified, so that we can use the same
    // code to load matches where the subdivision into parts is unknown.

    VulgarMapping(String line) throws ParsingException {
        if (!line.startsWith("vulgar: ")) {
            throw new SyntaxError("Line does not start with 'vulgar: '");
        }
        Matcher matcher = vulgarPattern.matcher(line);
        if (!matcher.matches()) {
            throw new SyntaxError("Could not parse line: " + line);
        }

        query = matcher.group(1);
        qStart = Integer.parseInt(matcher.group(2));
        qEnd = Integer.parseInt(matcher.group(3));
        qStrand = matcher.group(4).charAt(0);

        target = matcher.group(5);
        tStart = Integer.parseInt(matcher.group(6));
        tEnd = Integer.parseInt(matcher.group(7));
        tStrand = matcher.group(8).charAt(0);

        score = matcher.group(9);
        matches = parseMatches(matcher.group(10));
    }

    private List<Match> parseMatches(String string) throws ParsingException {
        if (string == null) {
            return Collections.emptyList();
        }

        if (!string.startsWith(" ")) {
            throw new RuntimeException("The string doesn't start with a space." +
                    "That should be impossible.");
        }

        List<Match> matches = new ArrayList<Match>();
        String[] fields = string.substring(1).split(" ");

        if (fields.length % 3 != 0) {
            throw new RuntimeException("The number of fields is not a multiple of three." +
                    "That should be impossible at this point.");
        }

        if (logger.isTraceEnabled()) {
            StringBuilder fieldsStr = new StringBuilder();
            boolean firstTime = true;
            for (String field: fields) {
                if (!firstTime) {
                    fieldsStr.append(", ");
                }
                fieldsStr.append(field);
                firstTime = false;
            }
            logger.trace(String.format("Fields = [%s]", fieldsStr));
        }

        for(int i=0; i < fields.length; i+=3) {
            matches.add(
                new Match(fields[i].charAt(0),
                    Integer.parseInt(fields[i+1]),
                    Integer.parseInt(fields[i+2]) ));
        }
        return matches;
    }

    /*
     * Note that Exonerate uses interbase coordinates, so we don't
     * need to translate them!
     */

    private String query;
    private int qStart, qEnd;
    private char qStrand;

    private String target;
    private int tStart, tEnd;
    private char tStrand;

    private String score;

    private List<Match> matches;

    String getQuery() {
        return query;
    }

    int getQMin() {
        switch (qStrand) {
        case '+': return qStart;
        case '-': return qEnd;
        default: throw new IllegalStateException(String.format("Invalid qStrand '%c'", qStrand));
        }
    }

    int getQMax() {
        switch (qStrand) {
        case '+': return qEnd;
        case '-': return qStart;
        default: throw new IllegalStateException(String.format("Invalid qStrand '%c'", qStrand));
        }
    }

    int getQStrand() {
        switch(qStrand) {
        case '+': return +1;
        case '-': return -1;
        default: throw new IllegalStateException(String.format("Invalid qStrand '%c'", qStrand));
        }
    }

    String getTarget() {
        return target;
    }

    int getTMin() {
        switch (tStrand) {
        case '+': return tStart;
        case '-': return tEnd;
        default: throw new IllegalStateException(String.format("Invalid tStrand '%c'", tStrand));
        }
    }

    int getTMax() {
        switch (tStrand) {
        case '+': return tEnd;
        case '-': return tStart;
        default: throw new IllegalStateException(String.format("Invalid tStrand '%c'", tStrand));
        }
    }

    int getTStrand() {
        switch(tStrand) {
        case '+': return +1;
        case '-': return -1;
        default: throw new IllegalStateException(String.format("Invalid tStrand '%c'", tStrand));
        }
    }

    String getScore() {
        return score;
    }

    List<Match> getMatches() {
        return matches;
    }

    class Match {
        private VulgarMatchType type;
        private int queryLength;
        private int targetLength;

        public Match(char typeChar, int queryLength, int targetLength) throws ParsingException {
            this.type = VulgarMatchType.fromChar(typeChar);
            this.queryLength = queryLength;
            this.targetLength = targetLength;
        }

        VulgarMatchType getType() {
            return type;
        }

        int getQueryLength() {
            return queryLength;
        }

        int getTargetLength() {
            return targetLength;
        }
    }
}