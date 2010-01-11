package org.genedb.db.taxon;


import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Locale;

public class TaxonNodeListFormatter implements Formatter<TaxonNodeList> {

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
            first = false;
        }
        return builder.toString();
    }

    @Override
    public TaxonNodeList parse(String text, Locale locale) throws ParseException {
        if (! StringUtils.hasText(text)) {
            return new TaxonNodeList(taxonNodeManager.getTaxonNodeByString("Root", false));
        }
        String[] parts = text.split(":");
        TaxonNodeList nodeList = new TaxonNodeList();
        for (String part : parts) {
            TaxonNode node = taxonNodeManager.getTaxonNodeByString(part, true);
            if (node == null) {
                throw new IllegalArgumentException("Can't parse '"+part+"' as a organism identifier");
            }
            nodeList.add(node);
        }
        return nodeList;
    }


    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }

}
