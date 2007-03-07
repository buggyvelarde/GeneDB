package org.genedb.web.mvc.controller;

import org.genedb.db.loading.TaxonNode;

import org.springframework.util.StringUtils;

import java.util.List;

public class TaxonUtils {
    
    public static String getTaxonListFromNodes(TaxonNode[] nodes) {
        return StringUtils.arrayToDelimitedString(nodes, " ");
    }

	public static Taxon getTaxonFromList(List<String> answers, int i) {
		// TODO Auto-generated method stub
		return new Taxon();
	}

}
