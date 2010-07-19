package org.genedb.db.analyzers;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

public class AllNamesAnalyzer extends Analyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new AllNamesTokenizer(reader);
    }

    private class AllNamesTokenizer extends WhitespaceTokenizer {
        public AllNamesTokenizer(Reader in) {
            super(in);
        }
        @Override
        protected char normalize(char c) {
            return Character.toLowerCase(c);
          }
    }
}
