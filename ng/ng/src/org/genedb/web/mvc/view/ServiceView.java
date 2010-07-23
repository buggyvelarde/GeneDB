package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.View;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 *
 * A view for services, which will inspect the request URI for a trailing extension. Defaults to XML.
 *
 * @author gv1
 *
 */
public class ServiceView implements View {

    public static final String SERVICE_ROOT = "root";

    private String contentType = "application/xml";

    private static final Logger logger = Logger.getLogger(ServiceView.class);

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void render(Map<String, ?> map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String extension = getExtension(request);
        
        logger.debug("using " + extension);
        
        XStream xstream = null;
        if (extension.equals("json")) {
            contentType = "application/json";
            JettisonMappedXmlDriver jmxd = new JettisonMappedXmlDriver();
            xstream = new XStream(jmxd);
        } else {
            contentType = "application/xml";
            xstream = new XStream();
        }
        
        xstream.autodetectAnnotations(true);
        
        ResponseContainer responseContainer = new ResponseContainer();
        
        for (String key : map.keySet())	 
        {	 
        	Object value = map.get(key);	 
        	if (value instanceof BeanPropertyBindingResult)	 
        		continue;
        	responseContainer.addResult(value);
        }
        
        PrintWriter writer = response.getWriter();
        String json = xstream.toXML(responseContainer);
        writer.write(json);
        
    }

    /**
     * Generates and appropriate extension based on the existing HTTP request.
     *
     * @param request
     * @return
     */
    private String getExtension(HttpServletRequest request) {
        String uri = request.getRequestURI();
        logger.debug("parsing uri: " + uri);
        String extension = "xml";
        if (uri.endsWith(".json")) {
            extension = "json";
        }
        return extension;
    }

}

@XStreamAlias("response")	 
class ResponseContainer	 
{	 
	@XStreamImplicit()	 
	private List<Object> results = new ArrayList<Object>();	 
	 
	public void addResult(Object result)	 
	{	 
		results.add(result);	 
	}	 
	
}

