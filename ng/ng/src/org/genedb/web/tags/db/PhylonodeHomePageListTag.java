package org.genedb.web.tags.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;

public class PhylonodeHomePageListTag extends AbstractHomepageTag {

	private String baseUrl;
    private boolean leafOnly = false;

    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        getAllChildren(root, nodes);
        
        out.write("<ul>");
        for (TaxonNode node : nodes) {
        	if (node == root)
        		continue;
            out.write(String.format("<li><a href=\"%s\">%s</a></li>", this.baseUrl + node.getLabel(), node.getName(TaxonNameType.FULL)));
        }
        out.write("</ul>");
    }

    private void getAllChildren(TaxonNode node, List<TaxonNode> list) {
        if (node.isLeaf()) {
            if (node.isPopulated()) {
                list.add(node);
            }
        } else {
            if (!leafOnly && node.isChildrenPopulated()) {
                list.add(node);
            }
        }

        for (TaxonNode child : node.getChildren()) {
            getAllChildren(child, list);
        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setLeafOnly(boolean leafOnly) {
        this.leafOnly = leafOnly;
    }

    

}
