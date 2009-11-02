package org.genedb.web.tags.db;

import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class HomepageSelectTag extends AbstractHomepageTag {

    private String baseUrl;
    private boolean leafOnly = false;

    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        getAllChildren(root, nodes);

        out.write("<select name=\"organism\" onChange=\"document.location.href='"+baseUrl+"\'+this.value\">");
        for (TaxonNode node : nodes) {
            out.write("<option value=\"");
            out.write(node.getLabel());
            out.write("\">");
            out.write(node.getName(TaxonNameType.FULL));
            out.write("</option>");
        }
        out.write("</select>");
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
