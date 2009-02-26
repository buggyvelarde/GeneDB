package org.genedb.web.mvc.controller.download;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.genedb.querying.core.QueryException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;


@Controller
@RequestMapping("/ResultsNavigator")
@SessionAttributes("results")
public class ResultsNavigatorController {


    @RequestMapping(method = RequestMethod.GET )
    public String navigate(
            @RequestParam(value="index") String indexStr,
            @RequestParam(value="lastIndex") String lastIndexStr,
            @RequestParam(value="goto") String goTo,
            @RequestParam(value="q") String query,
            ServletRequest request,
            HttpSession session) throws QueryException {
    		
    	
    	List results = (List)session.getAttribute("results");
    	
    	if ("first".equals(goTo)){
    		Object[] item = (Object[])results.get(0);
    		return "redirect:/NamedFeature?name="+item[0] + assembleRequestParameters(request, goTo);
    		
    	}else if ("previous".equals(goTo)){
    		int index = Integer.parseInt(indexStr);
    		Object[] item = (Object[])results.get(index-1);
    		return "redirect:/NamedFeature?name="+item[0] + assembleRequestParameters(request, goTo);
    		
    		
    	}else if("next".equals(goTo)){
    		int index = Integer.parseInt(indexStr);
    		Object[] item = (Object[])results.get(index+1);
    		return "redirect:/NamedFeature?name="+item[0] + assembleRequestParameters(request, goTo);
    		
    	}else if("last".equals(goTo)){
    		int lastIndex = Integer.parseInt(lastIndexStr);
    		Object[] item = (Object[])results.get(lastIndex);
    		return "redirect:/NamedFeature?name="+item[0] + assembleRequestParameters(request, goTo);
    		
    	}else if("results".equals(goTo)){    		
    		return "redirect:/Query?" + assembleRequestParameters(request, goTo);
    		
    	}else{
    		return null;
    	}
    }
    
    private String assembleRequestParameters(ServletRequest request, String goTo ){
    	StringBuffer paramConcats = new StringBuffer();
    	for(Enumeration en = request.getParameterNames(); en.hasMoreElements();){
    		String paramName = (String)en.nextElement();
    		String value = request.getParameter(paramName);
    		
    		if("goto".equals(paramName)){
    			continue;
    			
    		}else if("name".equals(paramName)){
    			continue;
    			
    		}else if ("first".equals(goTo) && "index".equals(paramName)){
    			paramConcats.append("&index=0");
    			
    		}else if("previous".equals(goTo) && "index".equals(paramName)){
    			paramConcats.append("&index");
    			paramConcats.append("=");
    			paramConcats.append(Integer.parseInt(value)-1);
    			
    		}else if("next".equals(goTo) && "index".equals(paramName)){
    			paramConcats.append("&index");
    			paramConcats.append("=");
    			paramConcats.append(Integer.parseInt(value)+1);
    			
    		}else if("last".equals(goTo) && "index".equals(paramName)){
    			paramConcats.append("&index");
    			paramConcats.append("=");
    			paramConcats.append(
    					Integer.parseInt(request.getParameter("lastIndex")));
    			
    		}else if("results".equals(goTo) && 
    				("index".equals(paramName) || "lastIndex".equals(paramName))){
    			continue;
    			
    		}else{
    			paramConcats.append("&");
    			paramConcats.append(paramName);
    			paramConcats.append("=");
    			paramConcats.append(value);
    		}
    	}
    	String values = paramConcats.toString();
    	return values;
    }

}
