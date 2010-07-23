package org.genedb.db.taxon;


import org.apache.log4j.Logger;
import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Locale;

public class TaxonNodeListFormatter implements Formatter<TaxonNodeList> {

	private Logger logger = Logger.getLogger(TaxonNodeListFormatter.class);

    private TaxonNodeManager taxonNodeManager;

    @Override
    public String print(TaxonNodeList nodeList, Locale locale) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (TaxonNode node : nodeList.getNodes()) {
            if (!first) {
                builder.append(":");
            }
            builder.append(node.getLabel());
            logger.debug(node.getLabel());
            first = false;
        }
        return builder.toString();
    }

    @Override
    public TaxonNodeList parse(String text, Locale locale) throws ParseException {
    	System.err.println("The input text is '"+text+"'");
    	logger.error("The input text is '"+text+"'");
        if (! StringUtils.hasText(text)) {
            logger.error("Returning root");
            return new TaxonNodeList(taxonNodeManager.getTaxonNodeByString("Root", false));
        }
        logger.debug("Getting the taxonNodeList");
        String[] parts = text.split(":");
        TaxonNodeList nodeList = new TaxonNodeList();
        for (String part : parts) {
            TaxonNode node = taxonNodeManager.getTaxonNodeByString(part, true);
            if (node == null) {
                throw new IllegalArgumentException("Can't parse '"+part+"' as a organism identifier");
            }
            logger.error("Adding node of '"+node+"'");
            nodeList.add(node);
            logger.debug(node);
        }
        return nodeList;
    }


    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }

}
