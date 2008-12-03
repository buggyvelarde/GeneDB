package org.genedb.web.tags.db;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;




public class DbXRefLinkTag extends SimpleTagSupport {

    private String dbXRef;

    public void setDbXRef(String dbXRef) {
        this.dbXRef = dbXRef;
    }

    @Override
    public void doTag() throws JspException, IOException {
        // TODO currently a no-op
        // Should lookup URL
        // different class for internal, external URL

        String url = null;


        JspWriter out = getJspContext().getOut();
        if (url != null) {
            out.write(String.format("<a href=\"%s>\">", url));
        }
        out.write(dbXRef);
        if (url != null) {
            out.write("</a>");
        }
    }

}
