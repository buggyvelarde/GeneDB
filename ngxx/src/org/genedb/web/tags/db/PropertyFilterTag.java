package org.genedb.web.tags.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.utils.propinterface.PropertyI;

public class PropertyFilterTag extends SimpleTagSupport {

    private static final Logger logger = Logger.getLogger(PropertyFilterTag.class);

    private Collection<PropertyI> items;
    private String cvName, cvTermName, var;
    private Pattern cvNamePattern;

    public void setVar(String var) {
        this.var = var;
    }

    public void setItems(Collection<PropertyI> items) {
        this.items = items;
    }

    public void setCv(String name) {
        if (cvNamePattern != null)
            throw new IllegalStateException("Cannot set CV name if CV pattern has been set");
        this.cvName = name;
    }

    public void setCvTerm(String name) {
        this.cvTermName = name;
    }

    public void setCvPattern(String pattern) {
        if (cvName != null)
            throw new IllegalStateException("Cannot set CV pattern if CV name has been set");
        this.cvNamePattern = Pattern.compile(pattern);
    }

    private boolean filterMatches(CvTerm cvTerm) {
        if (cvName != null && !cvTerm.getCv().getName().equals(cvName))
            return false; // The wrong CV

        if (cvNamePattern != null) {
            Matcher matcher = cvNamePattern.matcher(cvTerm.getCv().getName());
            if (!matcher.matches())
                return false;
        }

        return (cvTermName == null || cvTerm.getName().equals(cvTermName));
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
            if (filterMatches(propertyI.getType()))
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
