package org.genedb.querying.tmpquery;

import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.QueryParam;

import org.gmod.schema.cfg.OrganismHeirachy;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.List;


public abstract class OrganismLuceneQuery extends LuceneQuery implements TaxonQuery {

    private static final long serialVersionUID = -1581819678507010911L;

    protected static final TermQuery isCurrentQuery = new TermQuery(new Term("obsolete", "false"));

    protected static final TermQuery geneQuery = new TermQuery(new Term("type.name","gene"));
    protected static final TermQuery pseudogeneQuery = new TermQuery(new Term("type.name","pseudogene"));
    protected static final BooleanQuery geneOrPseudogeneQuery = new BooleanQuery();
    static {
        geneOrPseudogeneQuery.add(geneQuery, Occur.SHOULD);
        geneOrPseudogeneQuery.add(pseudogeneQuery, Occur.SHOULD);
    }

    protected static final TermQuery mRNAQuery = new TermQuery(new Term("type.name", "mRNA"));
    protected static final TermQuery pseudogenicTranscriptQuery = new TermQuery(new Term("type.name","pseudogenic_transcript"));
    protected BooleanQuery productiveTranscriptQuery = new BooleanQuery();

    private transient OrganismHeirachy organismHeirachy;

    @Autowired
    protected transient TaxonNodeManager taxonNodeManager;

    @QueryParam(
            order=3,
            title="Organism restriction"
    )
    protected TaxonNodeList taxons;

    @Autowired
    public void setOrganismHeirachy(OrganismHeirachy organismHeirachy) {
        this.organismHeirachy = organismHeirachy;
        for (Integer id : organismHeirachy.getIds()) {
            productiveTranscriptQuery.add(new TermQuery(new Term("type.cvTermId", "" + id)), Occur.SHOULD);
        }
    }


    /* (non-Javadoc)
     * @see org.genedb.querying.tmpquery.TaxonQuery#getTaxons()
     */
    public TaxonNodeList getTaxons() {
        return taxons;
    }


    /* (non-Javadoc)
     * @see org.genedb.querying.tmpquery.TaxonQuery#setTaxons(org.genedb.db.taxon.TaxonNode[])
     */
    public void setTaxons(TaxonNodeList taxons) {
        System.err.println("The taxons in setTaxons is '"+taxons.getNodes().get(0)+"'");
        this.taxons = taxons;
    }



    @Override
    protected void getQueryTerms(List<Query> queries) {
        getQueryTermsWithoutOrganisms(queries);
        if (taxons==null) {
            throw new NullPointerException();
        }
        makeQueryForOrganisms(taxons, queries);
    }


    abstract protected void getQueryTermsWithoutOrganisms(List<Query> queries);


    private void makeQueryForOrganisms(TaxonNodeList taxons, List<org.apache.lucene.search.Query> queries) {
        System.err.println("The taxons in makeQueryFO is '"+taxons.getNodes().get(0)+"'");
        List<String> taxonNames = taxonNodeManager.getNamesListForTaxons(taxons);
        if (taxonNames.size() == 0) {
            return;
        }
        BooleanQuery organismQuery = new BooleanQuery();
        for (String organism : taxonNames) {
            organismQuery.add(new TermQuery(new Term("organism.commonName",organism)), Occur.SHOULD);
        }
        queries.add(organismQuery);
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

    /**
     * Replace all lucene reserved characters by escaping them
     * @param searchText
     * @return escaped searchText
     */
    protected String escapseSearchText(String searchText){
        if (!StringUtils.isEmpty(searchText)){
            String escapeChars ="[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
            String escaped = searchText.replaceAll(escapeChars, "\\\\$0");
            return escaped;
        }
        return searchText;
    }


}
