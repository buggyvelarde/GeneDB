package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static javax.servlet.jsp.PageContext.REQUEST_SCOPE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;
import static org.genedb.web.mvc.controller.WebConstants.TAXON_NODE;

import org.genedb.db.loading.TaxonNode;
import org.genedb.db.loading.TaxonNodeManager;
import org.genedb.web.mvc.controller.WebConstants;

import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BreadcrumbTag extends SimpleTagSupport {
	
    String seperator = "<small> >> </small>";
	
    @Override
    public void doTag() throws JspException, IOException {
        TaxonNodeManager tnm = (TaxonNodeManager)
        getJspContext().getAttribute(TAXON_NODE_MANAGER, APPLICATION_SCOPE);
        
        if (tnm == null) {
            // TODO Log problem
            return;
        }
        
        TaxonNode taxonNode = (TaxonNode) getJspContext().getAttribute(TAXON_NODE, REQUEST_SCOPE);
        if (taxonNode == null) {
            // TODO Log problem
            return;
        }
        
        String prefix = getContextPathFromJspContext(getJspContext());
        String trail = checkCache(taxonNode);
        if (trail == null) {
            StringBuilder buf = new StringBuilder();
            List<TaxonNode> nodes = tnm.getHeirachy(taxonNode);
            boolean first = true;
            for (TaxonNode node : nodes) {
                if (!first) {
					buf.append(seperator);
                }
                if (node.isWebLinkable()) {
                  	buf.append("<a href=\"");
                  	buf.append(prefix);
                  	buf.append("/Homepage?org=");
                	buf.append(node.getShortName());
                	buf.append("\">");
                }
                // TODO Don't hyperlink last org if page is homepage
                buf.append(node.getShortName());
                if (node.isWebLinkable()) {
                	buf.append("</a>");
                }
                first = false;
            }
            trail = buf.toString();
            // Store in cache
        }
        
        trail += seperator + getJspContext().getAttribute(WebConstants.CRUMB, REQUEST_SCOPE);
        
        JspWriter out = getJspContext().getOut();
        out.write(trail);
    }


	private String getContextPathFromJspContext(JspContext context) {
		String prefix = (String) context.getAttribute(WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE, REQUEST_SCOPE);
        if (!prefix.equals("/")) {
        	prefix += "/";
        }
		return prefix;
	}
    

	private String checkCache(TaxonNode tn) {
        // TODO 
        return null;
    }

	private void setCache(TaxonNode tn, String path) {
	    // TODO
    }

}
