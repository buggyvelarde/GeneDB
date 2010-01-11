package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;

/**
 * 
 * @author larry@sangerinstitute
 *
 */
public class TaxonNameTag extends SimpleTagSupport {
    
    private String label;
    
    private String taxonNameType;

    private static final Logger logger = Logger.getLogger(TaxonNameTag.class);

     @Override
    public void doTag() throws JspException, IOException {

        TaxonNodeManager taxonNodeManager = (TaxonNodeManager) getJspContext().getAttribute(TAXON_NODE_MANAGER, APPLICATION_SCOPE);
        
        TaxonNode node = taxonNodeManager.getTaxonNodeForLabel(label);
        
        TaxonNameType type = TaxonNameType.valueOf(taxonNameType);
        
        if(type==null){
            throw new JspException("Unknown type" + taxonNameType);
        }
        
        String otherName = node.getName(type);
        
        // Get the writer
        JspWriter out = getJspContext().getOut();
        
        if (!StringUtils.isEmpty(otherName)){
            out.write(otherName);
        }else{
            out.write(label);
            logger.error(label + " lacks the " + taxonNameType + " name type needed to display the correct name");
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTaxonNameType() {
        return taxonNameType;
    }

    public void setTaxonNameType(String taxonNameType) {
        this.taxonNameType = taxonNameType;
    }

}
