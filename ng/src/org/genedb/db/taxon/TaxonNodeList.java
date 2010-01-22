package org.genedb.db.taxon;


import java.util.List;

import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

public class TaxonNodeList {

    private List<TaxonNode> nodes;

    public TaxonNodeList() {
        nodes = Lists.newArrayList();
    }

    public TaxonNodeList(TaxonNode node) {
        nodes.add(node);
    }

    public List<TaxonNode> getNodes() {
        return nodes;
    }

    public void add(TaxonNode node) {
        nodes.add(node);
    }

    public String toString() {
    	return StringUtils.collectionToCommaDelimitedString(nodes);
    }

}
