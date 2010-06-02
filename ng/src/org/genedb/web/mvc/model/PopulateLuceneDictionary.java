package org.genedb.web.mvc.model;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;

/*
 * Will create dictionaries for a few fields in the Feature indices. Required for the SpellChecker used in the <SuggestQuery>.
 */
public class PopulateLuceneDictionary {
	
	/**
	 * The fields that will be dictionary-ed.
	 */
	public static final String[] fields = new String[] { "expandedProduct", "allNames", "synonym" };
	
    private static final Logger logger = Logger.getLogger(PopulateLuceneDictionary.class);
    private static final String indexName = "org.gmod.schema.mapped.Feature";
    
    public PopulateLuceneDictionary(String indexDirectoryName) throws CorruptIndexException, IOException {
        
        String indexFilename = indexDirectoryName + File.separatorChar + indexName;
        logger.info(String.format("Opening Lucene index at '%s'.", indexFilename));
        IndexReader indexReader = IndexReader.open(indexFilename);
        
        logger.info(indexReader.toString());
        Directory directory = indexReader.directory();
        
        logger.info("Initialising spell checker");
        SpellChecker spellchecker = new SpellChecker(directory);
        
        for (String field : fields) {
            logger.info(String.format("Initialising dictionary for field %s.", field));
            LuceneDictionary dict = new LuceneDictionary(indexReader, field);
            spellchecker.indexDictionary(dict);
        }
    }
    
    /**
     * @param args
     * @throws IOException 
     * @throws CorruptIndexException 
     * @throws Exception 
     */
    public static void main(String[] args) throws CorruptIndexException, IOException, Exception {
    	
    	// logger.setLevel(Level.DEBUG);
		
    	if (args.length < 1) {
    		throw new Exception("No lucene index path supplied.");
    	}
    	
		logger.debug("Using user supplied lucene index path " + args[0]);
		new PopulateLuceneDictionary(args[0]); 
		
    }

}
