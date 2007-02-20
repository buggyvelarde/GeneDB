package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.TaxonNode;
import org.genedb.web.mvc.controller.TaxonNodeManager;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class HomepageSelectTag extends AbstractHomepageTag {
    
    @Override
    protected void display(TaxonNode node, JspWriter out, int indent) throws IOException {
        out.write(""+indent+" ");
        out.write(node.getFullName());
        for (TaxonNode child : node.getChildren()) {
            display(child, out, indent+1);
        }
    }

}
