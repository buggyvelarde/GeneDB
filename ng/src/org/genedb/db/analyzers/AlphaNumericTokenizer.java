package org.genedb.db.analyzers;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.CharTokenizer;

class AlphaNumericTokenizer extends CharTokenizer {

    protected Set<Character> tokens = new HashSet<Character>(Arrays.asList(new Character[] { ',' }));

    public AlphaNumericTokenizer(Reader in) {
        super(in);
    }
    
    @Override
    protected boolean isTokenChar(char c) {
        return Character.isLetterOrDigit(c);
    }

}