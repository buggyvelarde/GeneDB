/**
 * 
 */
package org.genedb.query.params;

import org.genedb.query.Param;
import org.genedb.query.QueryI;
import org.genedb.query.QueryConstants;

/**
 * Basic abstract implementation of Param, designed to be subclassed
 * 
 * @author art
 */
public abstract class AbstractParam implements Param {
	
	private String help;
	private String description;
	private String name;
	private QueryI query;
	
	public QueryI getQuery() {
		return query;
	}

	public void setQuery(QueryI query) {
		this.query = query;
	}

	public abstract Object getValue();
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHelp() {
		return help;
	}
	public void setHelp(String help) {
		this.help = help;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer("{");
		ret.append(name);
		ret.append("=");
		if (getValue()==null) {
			ret.append(QueryConstants.PARAM_NOT_SET);
		} else {
			ret.append(getValue());
		}
		ret.append("}");
		return ret.toString();
	}

}
