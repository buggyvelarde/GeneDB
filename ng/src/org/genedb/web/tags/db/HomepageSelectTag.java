package org.genedb.web.tags.db;

import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import static org.genedb.db.taxon.TaxonNameType.FULL;

public class HomepageSelectTag extends AbstractHomepageTag {

    private static final String DEFAULT_TITLE = "Select an organism";

    private String baseUrl;
    private boolean leafOnly = false;
    private boolean alwaysLink = false;
	private String suffix = "";
    private String title = DEFAULT_TITLE;

    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        getAllChildren(root, nodes);

        if (leafOnly) {
            Collections.sort(nodes, new Comparator<TaxonNode>() {
                @Override
                public int compare(TaxonNode tn1, TaxonNode tn2) {
                    return tn1.getName(FULL).compareTo(tn2.getName(FULL));
                }

            });
        }

        out.write(String.format("<select class='homepageselect wide' name=\"organism\" onChange=\"if (this.selectedIndex != 0) { document.location.href='%s'+this.value+'%s' ;}\">", baseUrl, suffix));
        if (this.title != null) {
            //out.write(String.format("<option value=\"%s\" disabled=\"disabled\">%s</option>", "none", title));
            out.write(String.format("<option value=\"%s\" >%s</option>", "none", title));
        }
        for (TaxonNode node : nodes) {
        	if (node.isWebLinkable() || alwaysLink) {
        		out.write(String.format("<option value=\"%s\">%s</option>", node.getLabel(), node.getName(TaxonNameType.FULL)));
        	} else {
        		out.write(String.format("<option value=\"%s\" disabled=\"disabled\">%s</option>", node.getLabel(), node.getName(TaxonNameType.FULL)));
        	}
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

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setAlwaysLink(boolean alwaysLink) {
		this.alwaysLink = alwaysLink;
	}

}
