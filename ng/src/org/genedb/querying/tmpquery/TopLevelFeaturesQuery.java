package org.genedb.querying.tmpquery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.genedb.querying.core.QueryException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class TopLevelFeaturesQuery extends OrganismLuceneQuery {
	
	@XStreamAlias("feature")
	public static class TopLevelFeature {
		@XStreamAlias("length")
	    @XStreamAsAttribute
	    public int length;
		
		@XStreamAlias("name")
	    @XStreamAsAttribute
	    public String name;
	}
	
	@Override
	public String getQueryDescription() {
		return "Returns the top level features in an organism.";
	}
	
	@Override
	public String getQueryName() {
	    return "Top Level Features";
	}


	@Override
	protected void getQueryTermsWithoutOrganisms(List<Query> queries) {
		BooleanQuery bq = new BooleanQuery();
	}

	@Override
	protected String getluceneIndexName() {
		return "org.gmod.schema.mapped.Feature";
	}

	@Override
	protected String[] getParamNames() {
		return new String[] {};
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List getResults() throws QueryException {
		
		Hashtable <String, TopLevelFeature> results = new Hashtable<String, TopLevelFeature>();
		
        try {
            TopDocs topDocs = lookupInLucene();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = fetchDocument(scoreDoc.doc);
                
                Field chr = document.getField("chr");
                Field chrlen = document.getField("chrlen");
                
                String chrValue = chr.stringValue();
                
                if (results.containsKey(chrValue)) {
                	continue;
                }
                
                Integer chrlenValue = Integer.parseInt(chrlen.stringValue());
                
                System.out.println(chrValue + " :: " + chrlenValue);
                
                TopLevelFeature top = new TopLevelFeature();
                top.length = chrlenValue;
                top.name = chrValue;
                
                results.put(chrValue, top);
                
            }
            
            List<TopLevelFeature> tops = new ArrayList<TopLevelFeature>(results.values());
            
            Collections.sort(tops, new TopLevelFeatureSorter());

            if(luceneIndex.getMaxResults() == tops.size()){
                isActualResultSizeSameAsMax = true;
            }

            return tops;
        } catch (CorruptIndexException exp) {
            throw new QueryException(exp);
        } catch (IOException exp) {
            throw new QueryException(exp);
        }
    }
	
	private class TopLevelFeatureSorter implements Comparator<TopLevelFeature> {
		@Override public int compare(TopLevelFeature f1, TopLevelFeature f2) {
			return f1.name.compareTo(f2.name);
		}
	}
	
}
