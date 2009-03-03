package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;

import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class OrganismNameTag extends SimpleTagSupport {

    private String name;
    private TaxonNameType type;


    public void setType(String typeString) {
        this.type = TaxonNameType.valueOf(typeString);
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void doTag() throws JspException, IOException {
        
        TaxonNodeManager tnm = (TaxonNodeManager) 
        getJspContext().getAttribute(TAXON_NODE_MANAGER, APPLICATION_SCOPE);
    
        TaxonNode node = tnm.getTaxonNodeForLabel(name);
    
        if (node == null) {
            throw new JspException("Organism Name Tag: Can't identify taxonNode for '"+name+"'");
        }
        
        JspWriter out = getJspContext().getOut();
        out.write(node.getName(type));
    }
    
//      PageContext pc = (PageContext) getJspContext();
//      HttpServletRequest req = (HttpServletRequest) pc.getRequest();
//      String contextPath = req.getContextPath();

}


