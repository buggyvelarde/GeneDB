/*
 * Created on 08-Mar-2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.genedb.web.mvc.controller.analysis;

import org.gmod.schema.mapped.Organism;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet which provides a motif search using a regular expression against a protein database.
 *
 * @author Adrian Tivey (art)
 */
public class MotifSearcher {

    //private static final int MAX_RESULT_SIZE = 20000;

    private static final Map<Character, String> PROTEIN_GROUP_MAP;
    private static final Map<Character, String> NUCLEOTIDE_GROUP_MAP;
    private static Pattern BY_LINE_PATTERN;

    static {
        PROTEIN_GROUP_MAP = new HashMap<Character, String>();
        PROTEIN_GROUP_MAP.put('B', "[AGS]");          //tiny
        PROTEIN_GROUP_MAP.put('Z', "[ACDEGHKNQRST]"); //turnlike
        PROTEIN_GROUP_MAP.put('0', "[DE]");           //acidic
        PROTEIN_GROUP_MAP.put('1', "[ST]");           //alcohol
        PROTEIN_GROUP_MAP.put('2', "[ILV]");          //aliphatic
        PROTEIN_GROUP_MAP.put('3', "[FHWY]");         //aromatic
        PROTEIN_GROUP_MAP.put('4', "[HKR]");          //basic
        PROTEIN_GROUP_MAP.put('5', "[DEHKR]");        //charged
        PROTEIN_GROUP_MAP.put('6', "[AFILMVWY]");     //hydrophobic
        PROTEIN_GROUP_MAP.put('7', "[DEHKNQR]");      //hydrophilic
        PROTEIN_GROUP_MAP.put('8', "[CDEHKNQRST]");   //polar
        PROTEIN_GROUP_MAP.put('9', "[ACDGNPSTV]");    //small

        NUCLEOTIDE_GROUP_MAP = new HashMap<Character, String>();
        NUCLEOTIDE_GROUP_MAP.put('Y', "[ct]");  // Pyrimidine (C & T)
        NUCLEOTIDE_GROUP_MAP.put('R', "[ag]");  // Purine
        NUCLEOTIDE_GROUP_MAP.put('W', "[at]");  // weak
        NUCLEOTIDE_GROUP_MAP.put('S', "[gc]");  // strong
        NUCLEOTIDE_GROUP_MAP.put('K', "[tg]");  // keto
        NUCLEOTIDE_GROUP_MAP.put('M', "[ca]");  // amino
        NUCLEOTIDE_GROUP_MAP.put('D', "[agt]"); // not C
        NUCLEOTIDE_GROUP_MAP.put('V', "[agc]"); // not T
        NUCLEOTIDE_GROUP_MAP.put('H', "[act]"); // not G
        NUCLEOTIDE_GROUP_MAP.put('B', "[gct]"); // not A

        BY_LINE_PATTERN = Pattern.compile("^.*$", Pattern.MULTILINE);
    }


    private String buffer;
    private String lastReturned;

    /**
     * @param org
     * @param pattern2
     * @return
     * @throws IOException
     */
    private List<MotifMatch> runMainSearch(Organism org, String patternString, boolean protein, int start) throws IOException {
        return runMainSearch(org, patternString, protein, start, null, null, null);
    }

    /**
     * @param org
     * @param pattern2
     * @return
     * @throws IOException
     */
    private List<MotifMatch> runMainSearch(Organism org, String patternString, boolean protein, int start,
            String customGroup1, String customGroup2, String customGroup3) throws IOException {
        // Work out db given org

        String dbFileName = "/tmp/motifTest"; // FIXME

        CharSequence in = fromFile(dbFileName);


        // Validate custom groups
//      if (!validateCustomGroup(customGroup1)) {
//
//      }

//        Pattern pattern = manipulateRegExp(patternString, customGroup1, customGroup2, customGroup3);

        Pattern pattern = Pattern.compile("CAD");
        // Run search
        return runSearch(in, pattern, start);
    }





    /**
     * @return
     */
//    private static Pattern validatePattern(String patternString) {
//
////      # Check search
////      if ($syn !~ /^[A-Za-z0-9\.\+\?\{\}\,\[\]\*\^\$]+$/) {
////      print qq(Your query contained invalid characters. Please alter your query and try again.);
//
//        return Pattern.compile(patternString);
//    }


    private List<MotifMatch> runSearch(CharSequence in, Pattern pattern, int start) throws IllegalStateException {
        // Read in pairs of lines
        // Compile the pattern

        List<MotifMatch> results = new ArrayList<MotifMatch>();

        Matcher matcher = BY_LINE_PATTERN.matcher(in);

        int count = 0;
        boolean stillContent = true;
        while (stillContent) {
            String idLine = getLine(matcher);
            if (!idLine.startsWith(">")) {
                throw new IllegalStateException("db (flat-file) isn't correctly formatted. Expecting header but got:"+idLine);
            }
            count++;
            System.err.println("" + count + idLine);

            boolean inSequence = true;
            StringBuilder sequence = new StringBuilder();
            while (inSequence) {
                String line = getLine(matcher);
                if (line == null) {
                    inSequence = false;
                    stillContent = false;
                } else {
                    if (line.startsWith(">")) {
                        pushBackLine();
                        inSequence = false;
                    } else {
                        sequence.append(line);
                    }
                }
            }

            MotifMatch mm = runLineSearch(sequence.toString(), idLine, pattern);
            if (mm != null) {
                results.add(mm);
            }
//                throw new IllegalStateException("db (flat-file) isn't correctly formatted. Got no corresponding sequence to "+idLine);
        }
        return results;
    }



    private String getLine(Matcher matcher) {
        if (buffer != null) {
            lastReturned = buffer;
            buffer = null;
            return lastReturned;
        }
        if (!matcher.find()) {
            return null;
        }
        lastReturned = matcher.group(0);
        return lastReturned;
    }

    private void pushBackLine() {
       if (buffer != null) {
           throw new RuntimeException("Internal error - pushbackLine called when buffer not empty");
       }
       buffer = lastReturned;
    }



    /**
     * @param sequence
     * @param pattern
     * @return
     */
    private MotifMatch runLineSearch(String sequence, String idLine, Pattern pattern) {

        MotifMatch motifMatch = null;
        Matcher matcher = pattern.matcher(sequence);

        // Read the lines
        while (matcher.find()) {
            // Get the line without the line termination character sequence
            @SuppressWarnings("unused") String hit = matcher.group();
            if (motifMatch == null) {
                motifMatch = new MotifMatch(idLine, sequence);
            }
            motifMatch.addCoords(matcher.start(), matcher.end());
        }

        return motifMatch;
    }

//    private Pattern manipulateRegExp(String in, String cg1, String cg2, String cg3) {
//        StringBuffer pb = new StringBuffer();
////
//
//        int leftSquareBracket = -1;
//        int leftCurlyBracket = -1;
//
//
//        for (int i=0; i < in.length(); i++) {
//            char c = in.charAt(i);
//            switch (c) {
//            // Square brackets
//            case '[':
//                leftSquareBracket = i;
//                pb.append(c);
//                break;
//            case ']':
//                leftSquareBracket = -1;
//                pb.append(c);
//                break;
//
//                // Curly brackets
//            case '{':
//                leftCurlyBracket = i;
//                pb.append(c);
//                break;
//            case '}':
//                leftCurlyBracket = -1;
//                pb.append(c);
//                break;
//
//                // Special characters
//            case '.':
//            case '+':
//            case '?':
//            case ',':
//                pb.append(c);
//                break;
//
//                // Numbers
//            case '0':
//            case '1':
//            case '2':
//            case '3':
//            case '4':
//            case '5':
//            case '6':
//            case '7':
//            case '8':
//            case '9':
//                if (leftCurlyBracket != -1) {
//                    pb.append(c);
//                } else {
//                    pb.append(expandGroup(c));
//                }
//                break;
//
//            default:
//                pb.append(expandGroup(c));
//            }
//        }
////$syn =~ s|\{|_\{|g;
////$syn =~ s|\}|\}_|g;
////
////my $newExp = "";
////my @parts  = split("_",$syn);
////foreach my $cur (@parts) {
////    if ($cur !~ "^\{") {
////  foreach my $lup (keys %mappings) {
////      $cur =~ s/$lup/$mappings{$lup}/g;
////  }
////    }
////    $newExp .= $cur;
////}
////
////# $syn =~ m/(.*)/s;
////      $syn = $newExp;
////
//        return Pattern.compile(pb.toString());
//    }

    public String expandGroup(char c) {
        return ""; // FIXME
    }


    // Converts the contents of a file into a CharSequence
    // suitable for use by the regex package.
    private static CharSequence fromFile(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        FileChannel fc = fis.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }


    private class MotifMatch {

        private String idLine;
        private String sequence;
        private List<int[]> coords = new ArrayList<int[]>();


        /**
         * @param idLine
         * @param sequence
         */
        public MotifMatch(String idLine, String sequence) {
            this.idLine = idLine;
            this.sequence = sequence;
        }


        /**
         * @param i
         * @param j
         */
        public void addCoords(int i, int j) {
            int[] coordPair = new int[2];
            coordPair[0] = i;
            coordPair[1] = j;
            coords.add(coordPair);
        }



        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer(idLine);
            ret.append('\n');
            for (Iterator<int[]> it = coords.iterator(); it.hasNext();) {
                int[] pair = (int[]) it.next();
                ret.append("  ");
                ret.append(sequence.substring(pair[0], pair[1]));
                ret.append('\n');
            }
            return ret.toString();
        }
    }

    public static void main(String[] args) throws IOException {
      //String org=args[0];
      String pattern=args[1];

      MotifSearcher ms = new MotifSearcher();
      Organism org = null;
      List<MotifMatch> results = ms.runMainSearch(org, pattern, true, 0);
      System.err.println("Number of results: "+results.size());
      //for (MotifMatch match : results) {
          //System.err.println(match.idLine);
      //}
    }

}


