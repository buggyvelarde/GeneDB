package org.genedb.db.analyzers;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

public class AllNamesAnalyzer extends Analyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new AllNamesTokenizer(reader);
    }

    private class AllNamesTokenizer extends WhitespaceTokenizer {

        protected Set<Character> tokens = new HashSet<Character>(Arrays.asList(new Character[] { ',' }));

        public AllNamesTokenizer(Reader in) {
            super(in);
        }

        @Override
        protected char normalize(char c) {
            return Character.toLowerCase(c);
        }
        
        @Override
        protected boolean isTokenChar(char c) {
            if (super.isTokenChar(c)) 
                return true;
            return tokens.contains(c);
        }

    }
}
