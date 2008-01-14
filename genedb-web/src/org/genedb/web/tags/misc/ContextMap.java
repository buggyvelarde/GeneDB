package org.genedb.web.tags.misc;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.genedb.web.gui.ImageInfo;
import org.genedb.web.mvc.controller.WebUtils;
import org.gmod.schema.sequence.Feature;

public class ContextMap extends SimpleTagSupport {
	
	private Feature gene;
	
	@Override
    public void doTag() throws JspException, IOException {
		ImageInfo info = WebUtils.drawContextMap(gene);
		JspWriter out = getJspContext().getOut();
		
		out.print("<img src=\"" + info.contextMapData + "\"/>");
	}

	public void setGene(Feature gene) {
		this.gene = gene;
	}
}
