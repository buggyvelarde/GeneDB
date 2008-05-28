package org.genedb.web.tags.db;

import org.genedb.db.loading.TaxonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class HomepageSelectTag extends AbstractHomepageTag {
    
    @Override
    protected void display(TaxonNode root, JspWriter out, @SuppressWarnings("unused") int indent) throws IOException {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        getAllChildren(root, nodes);
        
        out.write("<select name=\"organism\" onChange=\"document.location.href='/Homepage?org='+this.value\">");
        for (TaxonNode node : nodes) {
            out.write("<option value=\"");
            out.write(node.getLabel());
            out.write("\">");
            out.write(node.getLabel());
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
