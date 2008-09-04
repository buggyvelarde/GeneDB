package org.genedb.db.loading.alternative;

import java.io.File;

/**
 * A parsing error, while parsing an EMBL file. There are two basic types
 * of error: a {@link SyntaxError} means that the file is syntactically invalid
 * in some low-level way; that it contains a line that we can't make sense of.
 * A {@link DataError} represents a higher-level structural anomaly: perhaps there
 * are two SQ sections, or the ID line is missing. In all cases, of course, the
 * error message should explain what the problem actually is. The message will also
 * indicate on which line of which file the problem was spotted, when that makes
 * sense. (If a required section is missing, say, we can't pinpoint a specific
 * line, so in that case no line number is indicated.)
 *
 * @author rh11
 *
 */
public class ParsingException extends Exception {
    public ParsingException(String message) {
        super(message);
    }
    public ParsingException(String inputFile, String message) {
        this(message);
        this.inputFile = inputFile;
    }

    private String inputFile = null;
    private int lineNumber = -1;
    void setLocation(String inputFile, int lineNumber) {
        this.inputFile = inputFile;
        this.lineNumber = lineNumber;
    }
    void setInputFile(File inputFile) {
        setLocation(inputFile.toString(), -1);
    }
    void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String getMessage() {
        if (inputFile == null && lineNumber == -1) {
            return super.getMessage();
        }
        if (lineNumber == -1) {
            return String.format("Data error in '%s': %s", inputFile, super.getMessage());
        }
        if (inputFile == null) {
            return String.format("Data error at line %d: %s", lineNumber, super.getMessage());
        }
        return String.format("Data error at '%s' line %d: %s", inputFile, lineNumber, super.getMessage());
    }
}

class SyntaxError extends ParsingException {
    SyntaxError(String message) {
        super(message);
    }
    SyntaxError(String inputFile, int lineNumber, String message) {
        this(message);
        setLocation(inputFile, lineNumber);
    }
}
class DataError extends ParsingException {
    public DataError(String inputFile, String message) {
        super(inputFile, message);
    }

    DataError(String message) {
        super(message);
    }
}
