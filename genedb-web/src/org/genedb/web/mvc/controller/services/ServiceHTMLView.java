package org.genedb.web.mvc.controller.services;

import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletResponse;

/**
 * A view for rendering HTML, simply writes to the stream (for now).
 * @author gv1
 *
 */
public class ServiceHTMLView extends ServiceAbstractView {
    
    private static final Logger logger = Logger.getLogger(ServiceHTMLView.class);
    
    @Override
	protected void init(HttpServletResponse response) 
	{
		writer.write("<div class=\"" + rootTemplate + "\">");		
		logger.info("Init HTML output");
	}
    
    @Override
    protected void endit()
    {
    	writer.write("</div>");
    }
    
	@Override
	protected void arrayFieldStart(String key) {
		writer.write("\n<div class=\"array\"><div class=\"title\">" + key.toLowerCase() + "</div>");
	}

	@Override
	protected void endArray(String key) {
		writer.write("</div>");
	}

	@Override
	protected void endObject(String key) {
		writer.write("</div>");
	}

	@Override
	protected void objectFieldStart(String key) {
		writer.write("\n<div class=\"object\"><div class=\"title\">" + key.toLowerCase() + "</div>");
		
	}

	@Override
	protected void stringField(String key, String value) {
		writer.write("\n<div class=\"string\"><div class=\"title\">" + key.toLowerCase() + "</div><div class=\"value\">" + value.toLowerCase() + "</div>");
	}
    
	protected void stringValue(String value)
	{
		writer.write(value);
	}
	
}
