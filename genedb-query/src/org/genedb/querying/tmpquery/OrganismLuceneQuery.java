package org.genedb.querying.tmpquery;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.QueryParam;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.List;


public abstract class OrganismLuceneQuery extends LuceneQuery {

    @Autowired
    private TaxonNodeManager taxonNodeManager;

    @QueryParam(
            order=3,
            title="Organism restriction"
    )
    protected TaxonNode[] taxons;


    public TaxonNode[] getTaxons() {
        return taxons;
    }


    public void setTaxons(TaxonNode[] taxons) {
        this.taxons = taxons;
    }


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

    @Override
    protected GeneSummary convertDocumentToReturnType(Document document) {
        GeneSummary ret = new GeneSummary(
                document.get("uniqueName"), // systematic
                document.get("organism.commonName"), // taxon-name,
                document.get("product"), // product
                document.get("chr"), // toplevename
                Integer.parseInt(document.get("start")) // leftpos
                );
        return ret;
    }


    @Override
    protected void extraValidation(Errors errors) {
        // Deliberately empty
    }

}
