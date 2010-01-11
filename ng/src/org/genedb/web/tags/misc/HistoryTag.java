package org.genedb.web.tags.misc;

import static javax.servlet.jsp.PageContext.REQUEST_SCOPE;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class HistoryTag extends SimpleTagSupport {
    
    @Override
    public void doTag() throws JspException, IOException {
        
        PageContext pc = (PageContext) getJspContext();
        JspWriter out = getJspContext().getOut();
        
        String url = (String) pc.getAttribute("javax.servlet.forward.request_uri", REQUEST_SCOPE);
        String params = (String) pc.getAttribute("javax.servlet.forward.query_string", REQUEST_SCOPE);

        out.print(url+"?"+params+"&history=true");
        
    }

}
