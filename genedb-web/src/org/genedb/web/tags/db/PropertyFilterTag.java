package org.genedb.web.tags.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;
import org.gmod.schema.utils.propinterface.PropertyI;

public class PropertyFilterTag extends SimpleTagSupport {
    
    private static final Logger logger = Logger.getLogger(PropertyFilterTag.class);

    private Collection<PropertyI> items;
    private String cvName;
    private String cvTermName;
    private String var;

    public void setVar(String var) {
        this.var = var;
    }

    public void setItems(Collection<PropertyI> items) {
        this.items = items;
    }

    public void setCv(String name) {
        this.cvName = name;
    }
    
    public void setCvTerm(String name) {
        this.cvTermName = name;
    }

    @Override
    public void doTag() throws JspException, IOException {
        if (items == null) {
            logger.error("Items is null");
            getJspContext().setAttribute(var, null);
            return;
        }

        List<PropertyI> filteredItems = new ArrayList<PropertyI>();
        for (PropertyI propertyI : items)
            if ((cvName == null || propertyI.getCvTerm().getCv().getName().equals(cvName))
            && (cvTermName == null || propertyI.getCvTerm().getName().equals(cvTermName)))
                    filteredItems.add(propertyI);

        getJspContext().setAttribute(var, filteredItems);
        
        JspFragment body = getJspBody();
        if (body != null) {
            JspContext context = getJspContext();
            body.invoke(context.getOut());
            context.removeAttribute(var);
        }
    }
}
