package org.genedb.web.utils;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class Grep {

    // Charset and decoder for ISO-8859-15
    private Charset charset = Charset.forName("ISO-8859-15");
    private CharsetDecoder decoder = charset.newDecoder();

    // Pattern used to parse lines
    private Pattern linePattern = Pattern.compile(".*\r?\n");

    // The input pattern that we're looking for
    public Pattern pattern;

    // The file to search in
    public String file;

    // Compile the pattern from the command line
    //
    public void compile(String pat) {
        try {
            pattern = Pattern.compile(pat);
        } catch (PatternSyntaxException x) {
            System.err.println(x.getMessage());
            System.exit(1);
        }
    }

    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    //
    private List<String> grep(CharBuffer cb) {
        Matcher lm = linePattern.matcher(cb); // Line matcher
        Matcher pm = null; // Pattern matcher
        int lines = 0;
        List<String> out = new ArrayList<String>();
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group(); // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            if (pm.find())
                out.add(cs.toString());
            if (lm.end() == cb.limit())
                break;
        }
        return out;
    }

    // Search for occurrences of the input pattern in the given file
    //
    public List<String> grep() throws IOException {

        // Open the file and then get a channel from the stream
        File f = new File(file);
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

        // Decode the file into a char buffer
        CharBuffer cb = decoder.decode(bb);

        // Perform the search
        List<String> out = null;
        out = grep(cb);

        // Close the channel and the stream
        fc.close();
        return out;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

}
