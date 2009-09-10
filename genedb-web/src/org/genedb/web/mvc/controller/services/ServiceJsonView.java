package org.genedb.web.mvc.controller.services;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 * Uses the Jettison JSON-StAX library to generate JSON. 
 * 
 * @author gv1
 *
 */
public class ServiceJsonView extends ServiceXMLView
{
	private static final Logger logger = Logger.getLogger(ServiceJsonView.class);
	
	@Override
	protected void init(HttpServletResponse response) throws Exception
	{
		MappedNamespaceConvention con = new MappedNamespaceConvention();
		xwriter = new MappedXMLStreamWriter(con, writer);
		xwriter.writeStartDocument();
		xwriter.writeStartElement(rootTemplate);
		
		logger.info("Init JSON output");
	}
	
}

