package org.genedb.querying.tmpquery;

import java.util.List;


import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.regex.RegexQuery;

import org.genedb.querying.core.QueryParam;

public class MotifQuery extends OrganismLuceneQuery {
	
	@QueryParam(
            order=1,
            title="The search string"
    )
    private String search = "";
	
	@Override
	public String getQueryName() {
		return "motifQuery";
	}

	
	@Override
	protected void getQueryTermsWithoutOrganisms(List<Query> queries) {
		RegexQuery r = new RegexQuery(new Term("residues",search.toUpperCase()));
        queries.add(r);
	}

	@Override
    protected String getluceneIndexName() {
        return "org.gmod.schema.mapped.Feature";
    }

    @Override
    public String getQueryDescription() {
    	return "Searches for polypeptide residue patterns with a regular expression.";
    }

	@Override
	protected String[] getParamNames() {
		return new String[] {"search"};
	}

}
