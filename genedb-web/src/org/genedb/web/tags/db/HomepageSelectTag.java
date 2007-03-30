package org.genedb.web.tags.db;

import org.genedb.db.loading.TaxonNameType;
import org.genedb.db.loading.TaxonNode;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public class HomepageSelectTag extends AbstractHomepageTag {
    
    @Override
    protected void display(TaxonNode node, JspWriter out, int indent) throws IOException {
        out.write(""+indent+" ");
        out.write(node.getName(TaxonNameType.FULL));
        for (TaxonNode child : node.getChildren()) {
            display(child, out, indent+1);
        }
    }

}
