package org.genedb.querying.tmpquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.genedb.db.taxon.TaxonNode;
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
	public List<String> getResults() throws QueryException {

		List<String> results = new ArrayList<String>();
		logger.info("Searching for " + searchText);
		
		if (searchText.length() == 0) {
			return results;
		}

		try {
			String[] suggestions = spellChecker.suggestSimilar(searchText.toLowerCase(), 50);

			if ( (taxons == null ) || (taxons.getNodeCount() == 1 && taxons.getNodes().get(0).getLabel().equals("Root")) ) {

				for (int i =0; i<= max; i++) {
					if (suggestions.length > i) {
						results.add(suggestions[i]);
					}
				}

			} else {

				BooleanQuery organismFilter = new BooleanQuery();

				List<String> currentTaxonNames = taxonNodeManager.getNamesListForTaxons(taxons);
	            for (String currentTaxonName : currentTaxonNames) {
	            	TermQuery organismQuery = new TermQuery(new Term("organism.commonName", currentTaxonName));
	            	organismFilter.add(organismQuery, Occur.SHOULD);
	            	// logger.info("organism.commonName : " + currentTaxonName);
	            }

	            int count = 0;
				for (String suggestion : suggestions) {
					// logger.info(suggestion);
					if (searchForSuggestion(suggestion, organismFilter) == true) {
						results.add(suggestion);
						count++;
						// logger.info(count);
					}
					if (count > max) {
						break;
					}
				}
			}


			//results = Arrays.asList(suggestions);
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


	private boolean searchForSuggestion(String suggestion, Query prequery) throws IOException {

		BooleanQuery bq = new BooleanQuery();

        bq.add(prequery, Occur.MUST);

        BooleanQuery fieldQueries = new BooleanQuery();
		for (String field : PopulateLuceneDictionary.fields) {
			TermQuery fieldQuery = new TermQuery(new Term(field, suggestion));
			fieldQueries.add(fieldQuery, Occur.SHOULD);
			// logger.info(field + " : " + suggestion);
		}

		bq.add(fieldQueries, Occur.MUST);

		IndexSearcher searcher = new IndexSearcher(luceneIndex.getReader());
		TopDocs docs = searcher.search(bq, 1);

		// logger.info(docs);
		// logger.info(docs.totalHits);
		// logger.info(docs.totalHits > 0);

		if (docs.totalHits > 0) {
			return true;
		}
		return false;
	}


}
