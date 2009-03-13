package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.DbXRefListener;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;


public class HyperlinkDbsInText extends SimpleTagSupport {

    @SuppressWarnings("unchecked")
    @Override
    public void doTag() throws JspException, IOException {
        // different class for internal, external URL

        JspFragment body = getJspBody();
        PageContext pageContext = (PageContext) getJspContext();
        JspWriter out = pageContext.getOut();

        StringWriter stringWriter = new StringWriter();
        body.invoke(stringWriter);
        String text = stringWriter.getBuffer().toString();

        Pattern xref = Pattern.compile("\\((\\w+:\\w+)\\)");

        Matcher matcher = xref.matcher(text);

        for (int i = 1; i < matcher.groupCount(); i++ ) {
            String dbxref = matcher.group(i);

            String url = null;
            String[] parts = dbxref.split(":");
            if (parts.length > 1) {
                // db name should be in parts[0], the accession in parts[1]
                if (parts[0].equalsIgnoreCase("PUBMED")) {
                    parts[0] = "PMID";
                    dbxref = parts[0] + ":" + parts[1]; // So PMID gets displayed as well as linked
                }

                Map<String, String> dbUrlMap = (Map<String, String>) getJspContext().getAttribute(DbXRefListener.DB_URL_MAP, PageContext.APPLICATION_SCOPE);
                if (dbUrlMap.containsKey(parts[0])) {
                    url = dbUrlMap.get(parts[0]) + parts[1];

                    text = text.substring(0, matcher.start(i)) + "<a href=\""+url+"\">"+dbxref+"</a>"+text.substring(matcher.end(i));
                }


            }

        }

        out.write(text);
    }
}

