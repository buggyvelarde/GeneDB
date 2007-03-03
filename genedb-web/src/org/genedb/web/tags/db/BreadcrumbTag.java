package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static javax.servlet.jsp.PageContext.REQUEST_SCOPE;
import static org.genedb.web.mvc.controller.WebConstants.TAXON_NODE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;

import org.genedb.db.loading.TaxonNode;
import org.genedb.db.loading.TaxonNodeManager;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BreadcrumbTag extends SimpleTagSupport {
	
    @Override
    public void doTag() throws JspException, IOException {
        TaxonNodeManager tnm = (TaxonNodeManager)
        getJspContext().getAttribute(TAXON_NODE_MANAGER, APPLICATION_SCOPE);
        
        if (tnm == null) {
            // TODO Log problem
            return;
        }
        System.err.println("TaxonNodeManager is '"+tnm+"'");
        
        TaxonNode taxonNode = (TaxonNode) getJspContext().getAttribute(TAXON_NODE, REQUEST_SCOPE);
        if (taxonNode == null) {
            // TODO Log problem
            return;
        }
        
        System.err.println("TaxonNode is '"+taxonNode+"'");
        
        // Get cache from application scope
        String trail = checkCache(taxonNode);
        if (trail == null) {
            StringBuilder buf = new StringBuilder();
            List<TaxonNode> nodes = tnm.getHeirachy(taxonNode);
            boolean first = true;
            for (TaxonNode node : nodes) {
                if (!first) {
                    buf.append(" >> ");
                }
                buf.append(node.getShortName());
                first = false;
            }
            trail = buf.toString();
            // Store in cache
        }
        
        JspWriter out = getJspContext().getOut();
        out.write(trail);
    }
    

	private String checkCache(TaxonNode tn) {
        // TODO 
        return null;
    }

	private void setCache(TaxonNode tn, String path) {
	    // TODO
    }

}
