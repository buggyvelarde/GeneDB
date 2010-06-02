package org.genedb.querying.tmpquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.genedb.querying.core.QueryException;
import org.genedb.web.mvc.model.PopulateLuceneDictionary;

/*
 * Uses the lucene spellchecker to suggest alternative words. To be used as a fallback when there are no quick search results.
 */
public class SuggestQuery extends OrganismLuceneQuery {
	
	private static final Logger logger = Logger.getLogger(SuggestQuery.class);
	
	private static SpellChecker spellChecker;
	private String searchText;
	private int max = 30;
	
	public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    public int getMax() {
    	return max;
    }
    
    public void setMax(int max) {
    	this.max = max;
    }
	
	@PostConstruct
	@Override
    public void afterPropertiesSet() {
		super.afterPropertiesSet();
		
		if (spellChecker != null) {
			return;
		}
		
		try {
			
			logger.info("Initialising spell checker");
			spellChecker = new SpellChecker(this.luceneIndex.getDirectory());
			
			for (String field : PopulateLuceneDictionary.fields) {
				logger.info(String.format("Initialising dictionary for field %s.", field));
	        	LuceneDictionary dict = new LuceneDictionary(this.luceneIndex.getReader(), field);
	        	spellChecker.indexDictionary(dict);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		
    }
	
	
	@Override
	protected String[] getParamNames() {
		return new String[] { "searchText", "max" };
	}

	@Override
	protected String getluceneIndexName() {
		return "org.gmod.schema.mapped.Feature";
	}

	@Override
	public String getQueryName() {
		return "Did you mean";
	}
	
	
	
    /**
     * Currently does not do any taxon-filtering.
     */
	public List getResults() throws QueryException {
		List results = new ArrayList();
		logger.info("Searching for " + searchText);
		try {
			String[] suggestions = spellChecker.suggestSimilar(searchText, max);
			results = Arrays.asList(suggestions);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return results;
	}

	@Override
	protected void getQueryTermsWithoutOrganisms(List<Query> queries) {
		// TODO Auto-generated method stub
	}

	
}
