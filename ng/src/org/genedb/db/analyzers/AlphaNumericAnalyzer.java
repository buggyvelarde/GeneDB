package org.genedb.db.analyzers;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * 
 * 
 * @author gv1
 *
 */
public class AlphaNumericAnalyzer extends Analyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        AlphaNumericTokenizer tokenizer = new AlphaNumericTokenizer(reader);
        return new LowerCaseFilter(tokenizer);
    }

}
