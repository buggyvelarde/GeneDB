package org.genedb.web.tags.db;

import org.genedb.db.loading.TaxonNode;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public class HomepageTreeTag extends AbstractHomepageTag {
    
    @Override
    protected void display(TaxonNode node, JspWriter out, int indent) throws IOException {
        out.write("<ul>");
        out.write("<li>");
        out.write(node.getFullName());
        out.write("</li>");
        for (TaxonNode child : node.getChildren()) {
            display(child, out, indent+1);
        }
        out.write("</ul>");
    }

}
