package org.genedb.web.tags.db;

import org.gmod.schema.utils.propinterface.PropertyI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PropertyFilterTag extends SimpleTagSupport {
	
	private Collection<PropertyI> collection;
	private String name;
    private String var;


	public void setVar(String var) {
        this.var = var;
    }

    public void setCollection(Collection<PropertyI> collection) {
        this.collection = collection;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    @Override
	public void doTag() throws JspException, IOException {
    	List filtered = new ArrayList();
        if (collection != null) {
        	//String type = collection.getClass().getName();
            for (PropertyI prop : collection) {
            	if (name.equals(prop.getCvTerm().getName())) {
            		filtered.add(prop);
            	}
            }
        } else {
            System.err.println("Collection is null");
        }
		JspWriter out = getJspContext().getOut();
        getJspContext().setAttribute(var, filtered);
        getJspBody().invoke(out);
        getJspContext().removeAttribute(var);
	}
	
//		PageContext pc = (PageContext) getJspContext();
//		HttpServletRequest req = (HttpServletRequest) pc.getRequest();
//		String contextPath = req.getContextPath();

}
