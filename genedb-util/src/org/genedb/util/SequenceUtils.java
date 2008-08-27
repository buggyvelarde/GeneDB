package org.genedb.util;

public class SequenceUtils {
    private static final char[] COMPLEMENT_FROM = "acgtmrwsykvhdbnx".toCharArray();
    private static final char[] COMPLEMENT_TO   = "tgcakywsrmbdhvnx".toCharArray();

    /**
     * Compute the reverse-complement of a DNA sequence
     * @param sequence the sequence, in the lower-case DNA alphabet
     * @return the reverse-complement of the sequence
     */
    public static String reverseComplement(String sequence) {
        StringBuilder sb = transliterate(sequence, COMPLEMENT_FROM, COMPLEMENT_TO);
        sb.reverse();
        return sb.toString();
    }

    private static StringBuilder transliterate(String string, char[] from, char[] to) {
        if (from.length != to.length)
            throw new IllegalArgumentException("Source and destination alphabets have different lengths");
        StringBuilder result = new StringBuilder();
        for (char c: string.toCharArray()) {
            boolean foundChar = false;
            for (int i=0; i < from.length; i++) {
                if (c == from[i]) {
                    result.append(to[i]);
                    foundChar = true;
                    break;
                }
            }
            if (!foundChar)
                throw new IllegalArgumentException(String.format("String contains character '%c' not in alphabet", c));
        }
        return result;
    }

}
