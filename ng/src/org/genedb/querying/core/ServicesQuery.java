package org.genedb.querying.core;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

public class ServicesQuery implements Query {

	
	
	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean supports(Class<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object arg0, Errors arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getQueryDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getResults() throws QueryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParseableDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> prepareModelData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMaxResultsReached() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getQueryName() {
		// TODO Auto-generated method stub
		return null;
	}

}
