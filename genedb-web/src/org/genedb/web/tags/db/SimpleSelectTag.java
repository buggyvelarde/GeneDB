package org.genedb.web.tags.db;

import org.genedb.db.taxon.TaxonNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class SimpleSelectTag extends AbstractHomepageTag {
    
	private String[] indentSpaces = {"", ".", "..", "...", "....", ".....", "......", ".......", "......._"};
	
    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {
        
        out.write("<select name=\"taxons\">");
        displayImmediateChildren(root, out, 0);

        out.write("</select>");
    }
    
    
    private void displayImmediateChildren(TaxonNode node, JspWriter out, int indent) throws IOException {
        out.write("<option value=\"");
        out.write(node.getLabel());
        out.write("\">");
        if (indent > 7) {
        	indent=8;
        }
        out.write(indentSpaces[indent]);
        out.write(node.getLabel());
        out.write("</option>");
        List<TaxonNode> children = node.getChildren();
        Collections.sort(children, new Comparator<TaxonNode>() {
			@Override
			public int compare(TaxonNode arg0, TaxonNode arg1) {
				return arg0.getLabel().compareTo(arg1.getLabel());
			}
        });
        for (TaxonNode child : node.getChildren()) {
            displayImmediateChildren(child, out, indent+1);
        }
    }

}
