package org.genedb.web.mvc.controller;

import org.genedb.db.loading.TaxonNode;

import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaxonUtils {
    
    public static String getTaxonListFromNodes(TaxonNode[] nodes) {
        return StringUtils.arrayToDelimitedString(nodes, " ");
    }

    public static String getOrgNamesInHqlFormat(TaxonNode[] nodes) {
    	Set<String> orgNames = new HashSet<String>();
    	for (TaxonNode node : nodes) {
			orgNames.addAll(node.getAllChildrenNames());
		}
    	StringBuilder ret = new StringBuilder();
    	boolean notFirst = false;
    	for (String orgName : orgNames) {
    		if (notFirst) {
    			ret.append(", ");
    		} else {
    			notFirst = true;
    		}
    		ret.append('\'');
    		ret.append(orgName);
    		ret.append('\'');
    	}
    	return ret.toString();
    }
    
	public static Taxon getTaxonFromList(List<String> answers, int i) {
		// TODO Auto-generated method stub
		return new Taxon();
	}

}
