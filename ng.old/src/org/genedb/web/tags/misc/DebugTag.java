package org.genedb.web.tags.misc;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static javax.servlet.jsp.PageContext.PAGE_SCOPE;
import static javax.servlet.jsp.PageContext.REQUEST_SCOPE;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DebugTag extends SimpleTagSupport {
    
    @Override
    public void doTag() throws JspException, IOException {
        
        PageContext pc = (PageContext) getJspContext();
        JspWriter out = getJspContext().getOut();
        
        displayAttrsInScope(out, pc, "Application", APPLICATION_SCOPE);
        //displayAttrsInScope(out, pc, SESSION_SCOPE);
        displayAttrsInScope(out, pc, "Request", REQUEST_SCOPE);
        displayAttrsInScope(out, pc, "Page", PAGE_SCOPE);


    }
    

    private void displayAttrsInScope(JspWriter out, PageContext pc, String scopeName, int scope) throws IOException {
        StringBuilder ret = new StringBuilder();
        ret.append("<h2>");
        ret.append(scopeName+ " ("+scope+")");
        ret.append("</h2>");
        ret.append("<table border=\"1\">");
        Enumeration<String> e = pc.getAttributeNamesInScope(scope);
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            toRow(ret, key, pc.getAttribute(key, scope));
        }
        ret.append("</table>");
        
        out.write(ret.toString());
    }

    private void toRow(StringBuilder in, String key, Object o) {
        in.append("<tr><td><b>");
        in.append(key);
        in.append("</b></td><td>");
        in.append(o);
        in.append("</td></tr>");
    }

}
