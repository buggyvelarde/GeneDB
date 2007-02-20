package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.TaxonNode;
import org.genedb.web.mvc.controller.TaxonNodeManager;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public abstract class AbstractHomepageTag extends SimpleTagSupport {

	private String top;
    
	
    @Override
    public void doTag() throws JspException, IOException {    
        TaxonNodeManager tnm = null; // FIXME - Get from application scope
        
        TaxonNode topNode = tnm.getTaxonNodeForLabel(top);
        
        if (topNode == null) {
            throw new JspException("Homepage Tag: Can't identify taxonNode for '"+top+"'");
        }
        
        JspWriter out = getJspContext().getOut();
        display(topNode, out, 0);
    }
    
    abstract protected void display(TaxonNode node, JspWriter out, int indent) throws IOException;

	public void setTop(String top) {
		this.top = top;
	}

}
