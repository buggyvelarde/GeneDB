package org.genedb.querying.tmpquery;

import org.genedb.querying.core.QueryClass;
import org.genedb.querying.core.QueryParam;
import org.springframework.validation.Errors;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.RangeQuery;

import java.util.List;

@QueryClass(
        title="Coding and pseudogenes by protein length",
        shortDesc="Get a list of transcripts ",
        longDesc=""
    )
public class ProteinNumTMQuery extends OrganismLuceneQuery {

    @QueryParam(
            order=1,
            title="Minimum length of protein in bases"
    )
    private int min = 1;

    @QueryParam(
            order=2,
            title="Minimum length of protein in bases"
    )
    private int max = 10;


    @Override
    protected String getluceneIndexName() {
        return "org.gmod.schema.mapped.Feature";
    }
    
    @Override
    public String getQueryDescription() {
    	return "Searches for proteins that have a given number of transmembrane helices.";
    }
    
    @Override
    public String getQueryName() {
        return "Transmembrane Helices";
    }

    @Override
    protected void getQueryTermsWithoutOrganisms(List<org.apache.lucene.search.Query> queries) {

        Term lowerTerm = new Term("numberTMDomains", String.format("%05d",  min));
        Term upperTerm = new Term("numberTMDomains", String.format("%05d",  max));
        RangeQuery rq = new RangeQuery(lowerTerm, upperTerm, true);
        
        queries.add(rq);
        //queries.add(geneOrPseudogeneQuery);
    }

    // ------ Autogenerated code below here



    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    protected String[] getParamNames() {
        return new String[] {"min", "max"};
    }

    @Override
    protected void extraValidation(Errors errors) {

        //validate dependent properties
        if (!errors.hasErrors()) {
            if (getMin() > getMax()) {
                errors.reject("min.greater.than.max");
            }
        }
    }
}
