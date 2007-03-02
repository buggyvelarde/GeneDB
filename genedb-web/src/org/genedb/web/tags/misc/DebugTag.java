package org.genedb.web.tags.misc;

import static javax.servlet.jsp.PageContext.APPLICATION;
import static javax.servlet.jsp.PageContext.PAGE;
import static javax.servlet.jsp.PageContext.SESSION;
import static javax.servlet.jsp.PageContext.REQUEST;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DebugTag extends SimpleTagSupport {
	
    @Override
    public void doTag() throws JspException, IOException {
        
        PageContext pc = (PageContext) getJspContext();
        JspWriter out = getJspContext().getOut();
        
        displayAttrsInScope(out, pc, APPLICATION);
        displayAttrsInScope(out, pc, SESSION);
        displayAttrsInScope(out, pc, REQUEST);
        displayAttrsInScope(out, pc, PAGE);


    }
    

	private void displayAttrsInScope(JspWriter out, PageContext pc, String application) throws IOException {
        StringBuilder ret = new StringBuilder();
        ret.append("<h2>");
        ret.append(application);
        ret.append("</h2>");
        ret.append("<table border=\"1\">");
        int scope = pc.getAttributesScope(application);
        Enumeration e = pc.getAttributeNamesInScope(scope);
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
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
