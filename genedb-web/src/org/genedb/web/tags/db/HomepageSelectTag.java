package org.genedb.web.tags.db;

import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class HomepageSelectTag extends AbstractHomepageTag {

    private static final String DEFAULT_TITLE = "Select an organism";

    private String baseUrl;
    private boolean leafOnly = false;
    private String title = DEFAULT_TITLE;

    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        getAllChildren(root, nodes);
        
        
        
        out.write(String.format("<select name=\"organism\" onChange=\"if (this.selectedIndex != 0) { document.location.href='%s'+this.value ;}\">", baseUrl));
        if (this.title != null) {
            //out.write(String.format("<option value=\"%s\" disabled=\"disabled\">%s</option>", "none", title));
            out.write(String.format("<option value=\"%s\">%s</option>", "none", title));
        }
        for (TaxonNode node : nodes) {
        	
            out.write(String.format("<option value=\"%s\">%s</option>", node.getLabel(), node.getName(TaxonNameType.FULL)));
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

    public void setTitle(String title) {
        this.title = title;
    }

}
