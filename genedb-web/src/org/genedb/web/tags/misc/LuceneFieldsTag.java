package org.genedb.web.tags.misc;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.lucene.index.IndexReader;

public class LuceneFieldsTag extends SimpleTagSupport {
	
	@Override
    public void doTag() throws JspException, IOException {
		IndexReader ir = IndexReader.open("/Users/cp2/external/lucene/index/gff/");
		@SuppressWarnings("unchecked")
		Collection<String> c = ir.getFieldNames(IndexReader.FieldOption.INDEXED);
		JspWriter out = getJspContext().getOut();
		out.write("<select name=\"field\">");
		out.write("<option value=\"ALL\">ALL</option>");
		for (String object : c) {
			out.write("<option value=\"" + object + "\">" + object + "</option>");
		}
		out.write("</select>");
	}
}
