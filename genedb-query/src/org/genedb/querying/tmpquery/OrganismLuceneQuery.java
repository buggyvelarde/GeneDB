package org.genedb.querying.tmpquery;

import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.QueryParam;




public abstract class OrganismLuceneQuery extends LuceneQuery {
	
	private TaxonNodeManager taxonNodeManager;
	
    @QueryParam(
            order=3,
            title="Organism restriction"
    )
    protected TaxonNode[] taxons;

    
    protected void makeQueryForOrganisms(TaxonNode[] taxons, List<org.apache.lucene.search.Query> queries) {

        List<String> taxonNames = taxonNodeManager.getNamesListForTaxons(taxons);        
        
        if (taxonNames.size() == 0) {
        	return;
        }
        
        BooleanQuery organismQuery = new BooleanQuery();
        
        for (String organism : taxonNames) {
            organismQuery.add(new TermQuery(new Term("organism.commonName",organism)), Occur.SHOULD);
        }
        
    }
    
}
