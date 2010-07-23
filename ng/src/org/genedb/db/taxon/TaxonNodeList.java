package org.genedb.db.taxon;


import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

public class TaxonNodeList implements Serializable {

    private static final Logger logger = Logger.getLogger(TaxonNodeList.class);
    private List<TaxonNode> nodes = Lists.newArrayList();

    public TaxonNodeList() {
        //
    }

    public TaxonNodeList(TaxonNode node) {
        nodes.add(node);
    }

    public List<TaxonNode> getNodes() {
    	logger.debug("Returning");
    	for (TaxonNode node : nodes) {
    		logger.debug(node);
    	}
        return nodes;
    }

    public void add(TaxonNode node) {
        nodes.add(node);
    }

    public String toString() {
    	return StringUtils.collectionToCommaDelimitedString(nodes);
    }

    public int getNodeCount() {
        return nodes.size();
    }

}
