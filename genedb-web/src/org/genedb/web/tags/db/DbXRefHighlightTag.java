package org.genedb.web.tags.db;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DbXRefHighlightTag extends SimpleTagSupport {

    @Override
	public void doTag() throws JspException, IOException {
        // TODO currently a no-op
		JspWriter out = getJspContext().getOut();
        getJspBody().invoke(out);
	}
}
