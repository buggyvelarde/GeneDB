package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.DbXRefListener;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;


public class DbXRefLinkTag extends SimpleTagSupport {

    private String dbXRef;

    public void setDbXRef(String dbXRef) {
        this.dbXRef = dbXRef;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doTag() throws JspException, IOException {
        // different class for internal, external URL

        String url = null;
        String[] parts = dbXRef.split(":");
        if (parts.length > 1) {
            // db name should be in parts[0], the accession in parts[1]

            if (parts[0].equalsIgnoreCase("PUBMED")) {
                parts[0] = "PMID";
                dbXRef = parts[0] + ":" + parts[1]; // So PMID gets displayed as well as linked
            }

            Map<String, String> dbUrlMap = (Map<String, String>) getJspContext().getAttribute(DbXRefListener.DB_URL_MAP, PageContext.APPLICATION_SCOPE);
            if (dbUrlMap.containsKey(parts[0])) {
                url = dbUrlMap.get(parts[0]) + parts[1];
            }
        }

        JspWriter out = getJspContext().getOut();
        if (url != null) {
            out.write(String.format("<a href=\"%s\">", url));
        }
        out.write(dbXRef);
        if (url != null) {
            out.write("</a>");
        }
    }

}
