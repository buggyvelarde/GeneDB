package org.genedb.web.tags.db;

import org.apache.commons.lang.StringUtils;
import org.genedb.db.taxon.TaxonNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

public class SimpleSelectTag extends AbstractHomepageTag {
    
	private String[] indentSpaces = {"", ".", "..", "...", "....", ".....", "......", ".......", "......._"};
	
    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {
        
        out.write("<select name=\"taxons\">");
        
        //Find the previously selected taxons if any in Request
        PageContext pageContext = (PageContext)getJspContext();
        String previouslySelectedTaxons = pageContext.getRequest().getParameter("taxons");
        
        displayImmediateChildren(root, out, 0, previouslySelectedTaxons);

        out.write("</select>");
    }
    
    
    /**
     * 
     * @param node
     * @param out
     * @param indent
     * @param previouslySelectedTaxons Should be pre-populated in a postback or redirect
     * @throws IOException
     */
    private void displayImmediateChildren(TaxonNode node, JspWriter out, int indent, String previouslySelectedTaxons) throws IOException {
        out.write("<option value=\"");
        out.write(node.getLabel());
        out.write("\"");
        
        if(!StringUtils.isEmpty(previouslySelectedTaxons) && previouslySelectedTaxons.equals(node.getLabel())){
            out.write(" selected ");;
        }        
        out.write(">");
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
            displayImmediateChildren(child, out, indent+1, previouslySelectedTaxons);
        }
    }

}
