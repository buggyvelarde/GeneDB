package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.DbXRefListener;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;


public class DbNameTag extends SimpleTagSupport {

    private String db;

    public void setDb(String db) {
        this.db = db;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doTag() throws JspException, IOException {
        // different class for internal, external URL

    	String name = null;
    	Map<String, String> dbNameMap = (Map<String, String>) getJspContext().getAttribute(DbXRefListener.DB_NAME_MAP, PageContext.APPLICATION_SCOPE);
    	if (dbNameMap.containsKey(db)) {
    		name = dbNameMap.get(db);
    	}

    	if (!StringUtils.hasText(name)) {
    		name = db;
    	}
        getJspContext().getOut().write(name);
    }

}
