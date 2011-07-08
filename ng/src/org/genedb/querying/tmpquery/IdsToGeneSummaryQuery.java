package org.genedb.querying.tmpquery;

import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.LuceneQuery.Pager;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IdsToGeneSummaryQuery extends LuceneQuery {

    private List<String> ids;

//    protected GeneSummary getGeneSummary(Document document) {
//    	String displayId = getGeneUniqueNameOrUniqueName(document);
//        GeneSummary ret = new GeneSummary(
//        		displayId,
//                document.get("uniqueName"), // systematic
//                document.get("organism.commonName"), // taxon-name,
//                document.get("product"), // product
//                document.get("chr"), // toplevename
//                Integer.parseInt(document.get("start")) // leftpos
//                );
//        return ret;
//    }
    
   
    
    @Override
    public String getQueryDescription() {
    	return "Generates a summary of from a feature id.";
    }
    
    @Override
    public String getQueryName() {
        return "Ids2Genes";
    }

    @Override
    protected void extraValidation(Errors errors) {
        // Deliberately empty
    }

    @Override
    protected String[] getParamNames() {
        return new String[] {"ids"};
    }

    @Override
    protected void getQueryTerms(List<org.apache.lucene.search.Query> queries) {
        BooleanQuery bq = new BooleanQuery();
        for(String id : ids) {
            bq.add(new TermQuery(new Term("uniqueName",id)), Occur.SHOULD);
        }

        queries.add(bq);
        //queries.add(geneQuery);
    }

    @Override
    protected String getluceneIndexName() {
        return "org.gmod.schema.mapped.Feature";
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
    
    protected GeneSummary docToGeneSummary(Document doc) {
    	String displayId = getGeneUniqueNameOrUniqueName(doc);
		return new GeneSummary(
				displayId,
				doc.get("uniqueName"), // systematic
				doc.get("organism.commonName"), // taxon-name,
				doc.get("product"), // product
				doc.get("chr"), // toplevename
                Integer.parseInt(doc.get("start")) // leftpos
		);
    }
    
    protected Pager<GeneSummary> geneSummaryPager = new Pager<GeneSummary>() {
		@Override public GeneSummary convert(Document doc) {
			return docToGeneSummary(doc);
		}
	};
	
    public List<GeneSummary> getResultsSummaries(int page, int length) throws QueryException {
    	return geneSummaryPager.getResults(page, length);
    }
    
    public List<GeneSummary> getResultsSummaries() throws QueryException {
    	
    	List<GeneSummary> summaries= new ArrayList<GeneSummary>();
    	
        TopDocs topDocs = lookupInLucene();
        
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        	try {
				summaries.add(
						docToGeneSummary(
								fetchDocument(scoreDoc.doc)));
			} catch (CorruptIndexException e) {
				throw new QueryException(e);
			} catch (IOException e) {
				throw new QueryException(e);
			}
        }
    	return summaries;
    }
    
}
