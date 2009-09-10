package org.genedb.web.mvc.controller.services;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;


/**
 * A base class for writing out a map of objects to a stream supplied in 
 * the renderMergedOutputModel(). Particularly it handles a special case for 
 * GenericObjects. It doesn't care about what format the data should be exported 
 * in, as this is left to the extending classes. It will ignore any components of 
 * the map that aren't GenericObjects.
 * 
 *  The advantages of this approach are
 *  - make use of streaming to limit memory consumption for large exports
 *  - no need for reflection because the GenericObject's public methods are used directly
 *  - no need to implement a large data-model for the view, as the model can be constructed 
 *  	from GenericObjects directly
 * 
 * @author gv1
 *
 */
public abstract class ServiceAbstractView extends AbstractView {
    
    private static final Logger logger = Logger.getLogger(ServiceAbstractView.class);
    
    protected PrintWriter writer;
    
    protected String rootTemplate;
    
    /**
     * Will cycle through the supplied map. 
     */
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model,
        HttpServletRequest request, HttpServletResponse response) 
    	throws Exception
    {
    	try
    	{
    		rootTemplate = "resultset";
    		
    		if (model.containsKey("error"))
    		{
    			logger.info("The model supplied is an error report");
    			rootTemplate = "errors";
    		}
    		
    		logger.debug("rootTemplate --- " + rootTemplate);
    		
    		preInit(response);
    		init(response);
    		
            for (Entry<String, Object> entry : model.entrySet()) {
                renderObject(entry.getKey(), entry.getValue());
            }
            
            endit();
            postEndit();
            
    	} catch (Exception e)
    	{
    		logger.error(e.getMessage());
    		e.printStackTrace();
    		
    		response.reset();
    		rootTemplate = "errors";
    		
    		preInit(response);
    		init(response);
    		
    		objectFieldStart("error");
    		stringField("message", e.getMessage());
    		endObject("error");
    		
    		endit();
            postEndit();
    	}
		
    }
    
    
    
    /**************************************************************** 
     * The following abstract methods essentially define an interface 
     * that must be implemented by the sub-classes. 
     * These are essentially hooks in the data output flow. 
     ****************************************************************/
    
    protected abstract void init(HttpServletResponse response) throws Exception;
    protected abstract void endit() throws Exception;
    
    protected abstract void arrayFieldStart(String key) throws Exception;
    protected abstract void endArray(String key) throws Exception;
    
    protected abstract void objectFieldStart(String key) throws Exception;
    protected abstract void endObject(String key) throws Exception;
    
    protected abstract void stringField(String key, String value) throws Exception;
    
    protected abstract void stringValue(String value) throws Exception;
    
    
    
    
    /****************************************************************
     * 
     * Privates from here on in.
     * 
     ****************************************************************/
    
    
    /**
     * Initializes the stream.
     */
    private void preInit(HttpServletResponse response) throws IOException 
    {
    	writer = response.getWriter();		
    }
    
    /**
     * Flushes and closes the stream.
     */
    private void postEndit()
    {
    	writer.flush();
    	writer.close();
    }
    
    /**
     * Tries to render an object supplied in the map. 
     * 
     * @param key the name of the object as stored in the map
     * @param value the object itself, which may be a GenericObject
     */
    private void renderObject(String key, Object value)
    	throws Exception
    {
    	if (value == null)
            return;
        
    	logger.debug(key + " " + value.getClass() + " " + value.toString());
    	
        if (key.startsWith("org.springframework.validation.BindingResult"))
        {
        	BindingResult br = (BindingResult) value;
        	logger.info("binding result!");
        	logger.info(br);
        	if (br.hasErrors())
        	{
        		this.stringField(key, value.toString());
        	}
        }
        else if (value instanceof GenericObject)
        {
        	renderVO( (GenericObject) value );
        } 
        else if (value instanceof String)
        {
        	this.stringField(key, (String) value);
        }
        else if (value instanceof String[])
        {
        	String[] subValues = (String[]) value;
        	for (String subValue : subValues)
        	{
        		this.stringField(key, subValue);
        	}
        } 
        else if (value instanceof Object[])
        {
        	Object[] subValues = (Object[]) value;
        	for (Object subValue : subValues)
        	{
        		this.stringField(key, subValue.toString());
        	}
        } 
        else
        {
        	this.stringField(key, value.toString());
        }
    }
    
    /**
     * Handles recursions in GenericObject.
     * 
     * @param vo a GenericObject
     */
    private void renderVO(GenericObject object)
    	throws Exception
    {
    	logger.debug(object.getName());
    	
    	String objectName = object.getName();
    	
    	if (object.isEmpty())
    	{
    		return;
    	}
    	else
    	{
    		if (object.size() > 1)
    			this.objectFieldStart(objectName);
			
    		for (Object subObject : object)
    		{
    			if (subObject instanceof GenericObject)
        		{
        			this.renderVO((GenericObject) subObject);        			
        		} else
        		{
        			this.renderObject(objectName, subObject);
        		}
    		}
    		
    		if (object.size() > 1)
    			this.endObject(objectName);
    	}
    }
        
}
