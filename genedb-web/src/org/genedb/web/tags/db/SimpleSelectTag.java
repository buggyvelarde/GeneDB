package org.genedb.web.tags.db;

import org.genedb.db.loading.TaxonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class SimpleSelectTag extends AbstractHomepageTag {
    
    @Override
    protected void display(TaxonNode root, JspWriter out, int indent) throws IOException {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        getAllChildren(root, nodes);
        for (TaxonNode child : root.getChildren()) {
            nodes.add(child);
        }
        
        out.write("<select name=\"organism\">");
        for (TaxonNode node : nodes) {
            out.write("<option value=\"");
            out.write(node.getShortName());
            out.write("\">");
            out.write(node.getShortName());
            out.write("</option>");
        }
        out.write("</select>");
    }
    
    
    private void getAllChildren(TaxonNode node, List<TaxonNode> list) {
        list.add(node);
        for (TaxonNode child : node.getChildren()) {
            getAllChildren(child, list);
        }
    }

}
