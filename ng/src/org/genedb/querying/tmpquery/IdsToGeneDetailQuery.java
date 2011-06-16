package org.genedb.querying.tmpquery;

import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.QueryException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

public class IdsToGeneDetailQuery extends LuceneQuery {
	
	private List<String> ids;
	
	@Override
    public String getQueryDescription() {
    	return "Generates a basket entry from a feature id.";
    }
    
    @Override
    public String getQueryName() {
        return "Ids2BasketEntry";
    }
	
	

	@Override
	protected void extraValidation(Errors errors) {
		// no validation....
		
	}

	@Override
    protected String[] getParamNames() {
        return new String[] {"ids"};
    }

	@Override
	protected void getQueryTerms(List<Query> queries) {
		BooleanQuery bq = new BooleanQuery();
        for(String id : ids) {
            bq.add(new TermQuery(new Term("uniqueName",id)), Occur.SHOULD);
        }

        queries.add(bq);
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
    
    protected Pager<GeneDetail> geneDetailPager = new Pager<GeneDetail>() {
		@Override public GeneDetail convert(Document document) {
			String location = document.get("start") + " - " + document.get("stop");
			
			int strand = Integer.parseInt(document.get("strand"));
			if (strand < 0) {
				location += " (reverse strand)";
			}
			
			List<String> synonyms = new ArrayList<String>();
			for (Field field : document.getFields("synonym")) {
				synonyms.add(field.stringValue());
			}
			
			
			GeneDetail ret = new GeneDetail(
	            document.get("uniqueName"), 
	            document.get("organism.commonName"), 
	            document.get("product"), 
	            document.get("chr"), 
	            location,
	            synonyms,
	            document.get("type.name"),
	            document.get("featureId"),
	            document.get("name")
	        );
	        return ret;
		}
	};
	
    public List<GeneDetail> getGeneDetails(int page, int length) throws QueryException {
    	return geneDetailPager.getResults(page, length);
    }
    
	

}
