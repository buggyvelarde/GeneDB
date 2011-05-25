package org.genedb.querying.tmpquery;

import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryParam;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class QuickSearchQuery extends OrganismLuceneQuery {

	private static final long serialVersionUID = -3007330180211992013L;

	private transient Logger logger = Logger.getLogger(QuickSearchQuery.class);

	private String searchText;

	@QueryParam(order = 1, title = "Search gene products?")
	private boolean product;

	@QueryParam(order = 2, title = "Search gene names and synonyms?")
	private boolean allNames;

	@QueryParam(order = 3, title = "Include pseudogenes")
	private boolean pseudogenes;

	@Override
	protected String getluceneIndexName() {
		return "org.gmod.schema.mapped.Feature";
	}

	@Override
	public String getQueryDescription() {
		return "Allows you to quickly search for genes by name (including synonyms) or function";
	}

	@Override
	protected String[] getParamNames() {
		return new String[] { "searchText", "product", "allNames",
				"pseudogenes" };
	}
	

	@Override
	protected void getQueryTermsWithoutOrganisms(
			List<org.apache.lucene.search.Query> queries) {
		BooleanQuery bq = new BooleanQuery();

		if (searchText.startsWith("*") || searchText.startsWith("?")) {
			searchText = searchText.substring(1);
		}

		String tokens[] = searchText.trim().split("\\s");

		if (allNames) {

			if (tokens.length > 1) {
				logger.info("phrase query");
				PhraseQuery pq = new PhraseQuery();
				for (String token : tokens) {
					pq.add(new Term("allNames", token.toLowerCase()));
				}
				bq.add(pq, Occur.SHOULD);
			} else {
				logger.info("wildcard query");
				bq.add(new WildcardQuery(new Term("allNames", tokens[0]
						.toLowerCase())), Occur.SHOULD);
			}

		}

		if (product) {
			if (tokens.length > 1) {
				PhraseQuery pq = new PhraseQuery();
				for (String token : tokens) {
					pq.add(new Term("expandedProduct", token.toLowerCase()));
				}
				bq.add(pq, Occur.SHOULD);
			} else {
				bq.add(new WildcardQuery(new Term("expandedProduct", tokens[0]
						.toLowerCase())), Occur.SHOULD);
			}
		}
		queries.add(bq);

		// Add type restrictions
		if (pseudogenes) {
			queries.add(productiveTranscriptQuery);
		} else {
			queries.add(mRNAQuery);
		}
		// queries.add(isCurrentQuery);

		// logger.info(queries);
	}

//	@Override
//	protected void getQueryTerms(List<Query> queries) {
//		getQueryTermsWithoutOrganisms(queries);
//	}

//	@Override
//	public String getParseableDescription() {
//		// TODO Auto-generated method stub
//		return null;
//	}



	/**
	 * Get all the results for the quick query
	 * 
	 * @return
	 * @throws QueryException
	 */
//	public QuickSearchQueryResults getReallyQuickSearchQueryResults(
//			int maxResults) throws QueryException {
//
//		QuickSearchQueryResults quickSearchQueryResults = new QuickSearchQueryResults();
//		quickSearchQueryResults.setTotalHits(0);
//		List<GeneSummary> geneSummaries = quickSearchQueryResults.getResults();
//
//		if (searchText.length() == 0) {
//			return quickSearchQueryResults;
//		}
//
//		try {
//			// taxn name
//			List<String> currentTaxonNames = null;
//			if (taxons != null && taxons.getNodeCount() > 0) {
//				currentTaxonNames = taxonNodeManager
//						.getNamesListForTaxons(taxons);
//			}
//
//			TopDocs topDocs = lookupInLucene(maxResults);
//			// int currentResult = 0;
//			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
//
//				Document document = fetchDocument(scoreDoc.doc);
//
//				// Get the current taxon name from document
//				// String taxonName = document.get("organism.commonName");
//
//				// boolean isNoTaxonMatch = currentTaxonNames != null &&
//				// !currentTaxonNames.contains(taxonName);
//				//
//				// if (isNoTaxonMatch) {
//				// continue;
//				// }
//
//				// only populate if we are under the max
//				// if (currentResult < maxResults) {
//				populateGeneSummaries(geneSummaries, document);
//				// }
//				//
//				// we want the total number of hits, even if we don't return
//				// them all
//				// currentResult++;
//
//			}
//			Collections.sort(geneSummaries);
//
//			logger.info("Total returned hits :" + geneSummaries.size());
//			logger.info("Total unreturned hits :" + topDocs.totalHits);
//
//			quickSearchQueryResults.setTotalHits(topDocs.totalHits);
//
//			if (luceneIndex.getMaxResults() == geneSummaries.size()) {
//				isActualResultSizeSameAsMax = true;
//			}
//
//			if (currentTaxonNames == null && geneSummaries.size() > 1) {
//				quickSearchQueryResults
//						.setQuickResultType(QuickResultType.ALL_ORGANISMS_IN_ALL_TAXONS);
//
//			} else if (geneSummaries.size() == 1) {
//				quickSearchQueryResults
//						.setQuickResultType(QuickResultType.SINGLE_RESULT_IN_CURRENT_TAXON);
//
//			} else if (geneSummaries.size() > 1) {
//				quickSearchQueryResults
//						.setQuickResultType(QuickResultType.MULTIPLE_RESULTS_IN_CURRENT_TAXON);
//
//			} else {
//				quickSearchQueryResults
//						.setQuickResultType(QuickResultType.NO_EXACT_MATCH_IN_CURRENT_TAXON);
//			}
//
//		} catch (CorruptIndexException exp) {
//			throw new QueryException(exp);
//		} catch (IOException exp) {
//			throw new QueryException(exp);
//		}
//		return quickSearchQueryResults;
//	}
	
	/**
	 * This is an unpaged query to work out all the taxon matches.
	 */
	public QuickSearchQueryResults getQuickSearchQueryResults(int page, int length) throws QueryException {
		QuickSearchQueryResults quickSearchQueryResults = new QuickSearchQueryResults();
		
		// first get the paged gene summaries list
//		List<GeneSummary> geneSummaries = geneSummaryPager.getResults(page, length);
//		quickSearchQueryResults.setResults(geneSummaries);
		
		TreeMap<String, Integer> taxonGroup = quickSearchQueryResults.getTaxonGroup();
		TreeMap<String, Integer> tempTaxonGroup = new TreeMap<String, Integer>();
		
		// now we get the entire results set to work out taxon grouping
		try {
			
			// taxn name
			List<String> currentTaxonNames = null;
			if (taxons != null && taxons.getNodeCount() > 0) {
				currentTaxonNames = taxonNodeManager.getNamesListForTaxons(taxons);
			}

			TopDocs topDocs = lookupInLucene();
			
			int size = 0;
			
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

				Document document = fetchDocument(scoreDoc.doc);
				String taxonName = document.get("organism.commonName");

				boolean isNoTaxonMatch = currentTaxonNames != null && !currentTaxonNames.contains(taxonName);

				if (isNoTaxonMatch) {
					populateTaxonGroup(tempTaxonGroup, taxonName);
				} else {
					// Categrise the taxons into size of hits
					populateTaxonGroup(taxonGroup, taxonName);
					size++;
				}

			}
			
			// Collections.sort(geneSummaries);
			
			logger.info("Total matched hits :" + size);
			quickSearchQueryResults.setTotalHits(size);

			if (luceneIndex.getMaxResults() == size) {
				isActualResultSizeSameAsMax = true;
			}

			// If no matches are found for current taxon, display all other
			// taxons with a match
			if (size == 0 && taxonGroup.size() == 0 && tempTaxonGroup.size() > 0) {
				taxonGroup.putAll(tempTaxonGroup);
			}

			if (currentTaxonNames == null && size > 1) {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.ALL_ORGANISMS_IN_ALL_TAXONS);

			} else if (size == 1) {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.SINGLE_RESULT_IN_CURRENT_TAXON);

			} else if (size > 1) {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.MULTIPLE_RESULTS_IN_CURRENT_TAXON);

			} else {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.NO_EXACT_MATCH_IN_CURRENT_TAXON);
			}

		} catch (CorruptIndexException exp) {
			throw new QueryException(exp);
		} catch (IOException exp) {
			throw new QueryException(exp);
		}
		
		return quickSearchQueryResults;
	}

	/**
	 * Get all the results for the quick query
	 * 
	 * @return
	 * @throws QueryException
	 */
	/*public List getResults() throws QueryException {
			

		quickSearchQueryResults = new QuickSearchQueryResults();
		List<GeneSummary> geneSummaries = quickSearchQueryResults.getResults();

		if (searchText.length() == 0) {
			return geneSummaries;
		}

		TreeMap<String, Integer> taxonGroup = quickSearchQueryResults.getTaxonGroup();
		TreeMap<String, Integer> tempTaxonGroup = new TreeMap<String, Integer>();
		
		try {
			// taxn name
			List<String> currentTaxonNames = null;
			if (taxons != null && taxons.getNodeCount() > 0) {
				currentTaxonNames = taxonNodeManager
						.getNamesListForTaxons(taxons);
			}

			TopDocs topDocs = lookupInLucene();

			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

				Document document = fetchDocument(scoreDoc.doc);

				// Get the current taxon name from document
				String taxonName = document.get("organism.commonName");

				boolean isNoTaxonMatch = currentTaxonNames != null
						&& !currentTaxonNames.contains(taxonName);

				if (taxons == null) {
					// Categorise the taxons into size of hits
					populateTaxonGroup(taxonGroup, taxonName);

					populateGeneSummaries(geneSummaries, document);

				} else if (isNoTaxonMatch) {
					populateTaxonGroup(tempTaxonGroup, taxonName);

				} else {
					populateGeneSummaries(geneSummaries, document);

					// Categrise the taxons into size of hits
					populateTaxonGroup(taxonGroup, taxonName);
				}

			}
			Collections.sort(geneSummaries);

			logger.info("Total geneSummaries hits :" + geneSummaries.size());
			quickSearchQueryResults.setTotalHits(geneSummaries.size());

			if (luceneIndex.getMaxResults() == geneSummaries.size()) {
				isActualResultSizeSameAsMax = true;
			}

			// If no matches are found for current taxon, display all other
			// taxons with a match
			if (geneSummaries.size() == 0 && taxonGroup.size() == 0 && tempTaxonGroup.size() > 0) {
				taxonGroup.putAll(tempTaxonGroup);
			}

			if (currentTaxonNames == null && geneSummaries.size() > 1) {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.ALL_ORGANISMS_IN_ALL_TAXONS);

			} else if (geneSummaries.size() == 1) {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.SINGLE_RESULT_IN_CURRENT_TAXON);

			} else if (geneSummaries.size() > 1) {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.MULTIPLE_RESULTS_IN_CURRENT_TAXON);

			} else {
				quickSearchQueryResults
						.setQuickResultType(QuickResultType.NO_EXACT_MATCH_IN_CURRENT_TAXON);
			}

		} catch (CorruptIndexException exp) {
			throw new QueryException(exp);
		} catch (IOException exp) {
			throw new QueryException(exp);
		}
		return geneSummaries;
	} */

	/**
	 * Categrise the taxons into size of hits
	 * 
	 * @param taxonGroup
	 * @param taxonName
	 */
	private void populateTaxonGroup(TreeMap<String, Integer> taxonGroup,
			String taxonName) {
		Integer currentTaxonHitCount = taxonGroup.get(taxonName);
		if (currentTaxonHitCount == null) {
			taxonGroup.put(taxonName, 1);
		} else {
			taxonGroup.put(taxonName, ++currentTaxonHitCount);
		}
	}

//	private void populateGeneSummaries(List<GeneSummary> geneSummaries,
//			Document document) {
//		// logger.debug(StringUtils.collectionToCommaDelimitedString(document.getFields()));
//		GeneSummary gs = convertDocumentToReturnType(document);
//		geneSummaries.add(gs);
//	}

	@Override
	public Map<String, Object> prepareModelData() {
		return Collections.emptyMap();
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate(Object arg0, Errors arg1) {
		// TODO Auto-generated method stub

	}

	public class QuickSearchQueryResults {
		//private List<GeneSummary> results = new ArrayList<GeneSummary>();
		private TreeMap<String, Integer> taxonGroup = new TreeMap<String, Integer>();
		private QuickResultType quickResultType;
		private String singleResultInTaxonGeneId;
		private int totalHits;

		public QuickResultType getQuickResultType() {
			return quickResultType;
		}

		public void setQuickResultType(QuickResultType quickResultType) {
			this.quickResultType = quickResultType;
		}

//		public List<GeneSummary> getResults() {
//			return results;
//		}
//
//		public void setResults(List<GeneSummary> results) {
//			this.results = results;
//		}

		public String getSingleResultInTaxonGeneId() {
			return singleResultInTaxonGeneId;
		}

		public void setSingleResultInTaxonGeneId(
				String singleResultInTaxonGeneId) {
			this.singleResultInTaxonGeneId = singleResultInTaxonGeneId;
		}

		public TreeMap<String, Integer> getTaxonGroup() {
			return taxonGroup;
		}

		public void setTaxonGroup(TreeMap<String, Integer> taxonGroup) {
			this.taxonGroup = taxonGroup;
		}

		public void setTotalHits(int totalHits) {
			this.totalHits = totalHits;
		}

		public int getTotalHits() {
			return this.totalHits;
		}
	}

	public enum QuickResultType {
		ALL_ORGANISMS_IN_ALL_TAXONS, NO_EXACT_MATCH_IN_CURRENT_TAXON, SINGLE_RESULT_IN_CURRENT_TAXON, MULTIPLE_RESULTS_IN_CURRENT_TAXON
	}

	public String getSearchText() {
		if (StringUtils.hasLength(searchText)) {
			searchText = searchText.trim();
		}
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public boolean isProduct() {
		return product;
	}

	public void setProduct(boolean product) {
		this.product = product;
	}

	public boolean isAllNames() {
		return allNames;
	}

	public void setAllNames(boolean allNames) {
		this.allNames = allNames;
	}

	public boolean isPseudogenes() {
		return pseudogenes;
	}

	public void setPseudogenes(boolean pseudogenes) {
		this.pseudogenes = pseudogenes;
	}

	@Override
	public String getQueryName() {
		return "Quick search";
	}

}
