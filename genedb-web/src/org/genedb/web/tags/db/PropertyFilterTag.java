package org.genedb.web.tags.db;

import org.genedb.db.hibernate3gen.FeatureProp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PropertyFilterTag extends SimpleTagSupport {
	
	private Collection<FeatureProp> collection;
	private String name;
    private String var;


	public void setVar(String var) {
        this.var = var;
    }

    public void setCollection(Collection<FeatureProp> collection) {
        this.collection = collection;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
	public void doTag() throws JspException, IOException {
        List<FeatureProp> filtered = new ArrayList<FeatureProp>();
        if (collection != null) {
            for (FeatureProp prop : collection) {
                if (name.equals(prop.getCvterm().getName())) {
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
