package org.genedb.web.mvc.controller.services;

import org.apache.log4j.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import javax.servlet.http.HttpServletResponse;

/**
 * A StAX-based XML output. The advantages of using StAX in this way (over many OXMs) are : 
 * 
 * - you can output the XML while streaming, and therefore do not need to rely on 
 * 		constructing a large DOM-like memory model and thus use less memory
 * - you do not need to rely on a predefined model to un-martial
 * - the StAX library should take care of keeping the output well-formed
 * 
 * @author gv1
 *
 */
public class ServiceXMLView extends ServiceAbstractView {
    
    private static final Logger logger = Logger.getLogger(ServiceXMLView.class);
    protected XMLStreamWriter xwriter;
    
    @Override
	protected void init(HttpServletResponse response) throws Exception
	{
		logger.info("Init XML output");
		
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		xwriter = factory.createXMLStreamWriter(writer);
		xwriter.writeStartDocument("UTF-8", "1.0");
		xwriter.writeStartElement(rootTemplate);
		
	}
    
    @Override
    protected void endit() throws Exception
    {
    	
    	xwriter.writeEndDocument();
		xwriter.flush();
		xwriter.close();
		
    }
    
	@Override
	protected void arrayFieldStart(String key)
		throws Exception
	{
		xwriter.writeStartElement(key.toLowerCase());
	}

	@Override
	protected void endArray(String key) 
		throws Exception 
	{
		xwriter.writeEndElement();
	}

	@Override
	protected void endObject(String key) 
		throws Exception
	{
		xwriter.writeEndElement();
	}

	@Override
	protected void objectFieldStart(String key) 
		throws Exception
	{
		xwriter.writeStartElement(key.toLowerCase());
	}

	@Override
	protected void stringField(String key, String value) 
		throws Exception 
	{
		xwriter.writeStartElement(key.toLowerCase());
		xwriter.writeCharacters(value);
		xwriter.writeEndElement();
	}
	
	protected void stringValue(String value) 
		throws Exception
	{
		xwriter.writeCharacters(value);
	}
        
}
