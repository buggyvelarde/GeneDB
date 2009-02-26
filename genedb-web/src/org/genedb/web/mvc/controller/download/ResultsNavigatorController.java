package org.genedb.web.mvc.controller.download;

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
            @RequestParam(value="goto") String goWhere,
            @RequestParam(value="resultsUri") String resultsUri,
            @RequestParam(value="q") String query,
            ServletRequest request,
            HttpSession session) throws QueryException {
    		
    	
    	List results = (List)session.getAttribute("results");
    	
    	if ("first".equals(goWhere)){
    		Object[] item = (Object[])results.get(0);
    		return "redirect:/NamedFeature?name="+item[0]+"&index=0" + "&lastIndex=" + lastIndexStr + "&q=" + query + "&resultsUri=" + resultsUri;
    		
    	}else if ("previous".equals(goWhere)){
    		int index = Integer.parseInt(indexStr);
    		Object[] item = (Object[])results.get(index-1);
    		return "redirect:/NamedFeature?name="+item[0]+"&index="+(index-1) + "&lastIndex=" + lastIndexStr + "&q=" + query + "&resultsUri=" + resultsUri;
    		
    	}else if("next".equals(goWhere)){
    		int index = Integer.parseInt(indexStr);
    		Object[] item = (Object[])results.get(index+1);
    		return "redirect:/NamedFeature?name="+item[0]+"&index="+(index+1) + "&lastIndex=" + lastIndexStr + "&q=" + query + "&resultsUri=" + resultsUri;
    		
    	}else if("last".equals(goWhere)){
    		int lastIndex = Integer.parseInt(lastIndexStr);
    		Object[] item = (Object[])results.get(lastIndex);
    		return "redirect:/NamedFeature?name="+item[0]+"&index="+lastIndex+ "&lastIndex=" + lastIndexStr + "&q=" + query + "&resultsUri=" + resultsUri;
    		
    	}else if("results".equals(goWhere)){    		
    		return "redirect:/" + resultsUri + "?q=" + query;
    		
    	}else{
    		return null;
    	}
    }

}
