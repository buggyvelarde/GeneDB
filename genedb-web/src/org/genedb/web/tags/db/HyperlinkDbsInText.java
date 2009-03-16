package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.DbXRefListener;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;


public class HyperlinkDbsInText extends SimpleTagSupport {

    @Override
    public void doTag() throws JspException, IOException {
        // different class for internal, external URL

        JspFragment body = getJspBody();
        PageContext pageContext = (PageContext) getJspContext();
        JspWriter out = pageContext.getOut();

        StringWriter stringWriter = new StringWriter();
        body.invoke(stringWriter);
        String text = stringWriter.getBuffer().toString();

        out.write(hyperLinkText(text));
    }

    public String hyperLinkText(String text) throws IOException {
        Pattern xref = Pattern.compile("\\(\\w+:\\w+\\)");

        Matcher matcher = xref.matcher(text);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {

            String dbxref = matcher.group().substring(1, matcher.group().length()-1);
            //System.err.println(dbxref);
            String[] parts = dbxref.split(":");
            if (parts.length > 1) {
                // db name should be in parts[0], the accession in parts[1]
                if (parts[0].equalsIgnoreCase("PUBMED")) {
                    parts[0] = "PMID";
                    dbxref = parts[0] + ":" + parts[1]; // So PMID gets displayed as well as linked
                }

                Map<String, String> dbUrlMap = (Map<String, String>) getJspContext().getAttribute(DbXRefListener.DB_URL_MAP, PageContext.APPLICATION_SCOPE);
                //Map<String, String> dbUrlMap = new HashMap<String, String>();
                //dbUrlMap.put("PMID", "wibble");
                if (dbUrlMap.containsKey(parts[0])) {
                    String url = dbUrlMap.get(parts[0]) + parts[1];
                    String replace =  "(<a href=\""+url+"\">"+dbxref+"</a>)";
                    matcher.appendReplacement(sb, replace);
                    //matcher.appendTail(sb);
                    //} else {
                    //matcher.appendReplacement(sb, dbxref);
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();

    }


    public static void main(String[] args) throws IOException {
        String a = "This is a comment (PMID:1234) and more (PMID:3456) but this isn't (FRED:56875)";
        HyperlinkDbsInText t = new HyperlinkDbsInText();
        System.err.println(t.hyperLinkText(a));


    }

}

