package org.genedb.querying.tmpquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.NumericQueryVisibility;
import org.springframework.validation.Errors;

public class MockProteinLengthQuery implements Query {

	private List<String> results = new ArrayList<String>();
	
	public void initResult(List<String> results){
		this.results = results;
	}

	@Override
	public String getParseableDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getResults() throws QueryException {
		return results;
	}

	@Override
	public void validate(Object target, Errors errors) {                
		return;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return MockProteinLengthQuery.class.isAssignableFrom(clazz);
	}


	@Override
	public Map<String, Object> prepareModelData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public boolean isMaxResultsReached() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public String getQueryName() {
        return "Mock Protein Length";
    }
    
}
