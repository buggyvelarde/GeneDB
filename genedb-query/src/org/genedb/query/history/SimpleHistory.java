package org.genedb.query.history;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genedb.query.Result;
import org.genedb.util.MapUtils;

public class SimpleHistory implements History {
	
	private Map<String, List<Result>> entries= new HashMap<String, List<Result>>();
	
	/* (non-Javadoc)
	 * @see org.genedb.query.History2#getTypes()
	 */
	public Set<String> getTypes() {
		return entries.keySet();
	}

	/* (non-Javadoc)
	 * @see org.genedb.query.History2#getResultDataSets(java.lang.String)
	 */
	public List<Result> getResults(String type) {
		return entries.get(type);
	}

	/* (non-Javadoc)
	 * @see org.genedb.query.History2#addResultDataset(org.genedb.query.Result)
	 */
	public void addResult(Result rds) {
		MapUtils.addEntryAsPartOfList(entries, rds.getType(), rds);
	}

	/* (non-Javadoc)
	 * @see org.genedb.query.History2#clear()
	 */
	public void clear() {
		entries.clear();
	}

	/* (non-Javadoc)
	 * @see org.genedb.query.History2#isEmpty()
	 */
	public boolean isFilled() {
		return !entries.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.genedb.query.History2#keyIterator()
	 */
	public Iterator keyIterator() {
		return entries.keySet().iterator();
	}


	/* (non-Javadoc)
	 * @see org.genedb.query.History2#size()
	 */
	public int size() {
		return entries.size();
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray(Object[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public boolean containsAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	

	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
