package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;


@Controller
@RequestMapping("/Query")
public class QueryController {

    @Autowired
    private QueryFactory queryFactory;



    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm(
            @RequestParam(value="q", required=false) String queryName,
            Model model) {

        if (!StringUtils.hasText(queryName)) {
            return "redirect:/QueryList";
        }
        Query query = queryFactory.retrieveQuery(queryName);
        if (query == null) {
            return "redirect:/QueryList"; // FIXME - Send flash msg
        }
        model.addAttribute("query", query);
        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
			model.addAttribute(entry.getKey(), entry.getValue());
		}
        //return "search/query";
        return "search/"+queryName;
    }

    @RequestMapping(method = RequestMethod.POST)
	public ModelAndView processSubmit(
            @RequestParam(value="q", required=false) String queryName,
	        ServletRequest request
	        ) throws QueryException {

        if (!StringUtils.hasText(queryName)) {
            return new ModelAndView("redirect:/QueryList"); // FIXME - Send flash msg
        }

        Query query = queryFactory.retrieveQuery(queryName);
        ServletRequestDataBinder binder = new ServletRequestDataBinder(query);
        // register custom editors, if desired
        //binder.registerCustomEditor(...);
        binder.bind(request);
        // optionally evaluate binding errors
        Errors errors = binder.getBindingResult();

        if (errors.getErrorCount() != 0) {
            // Problem
        }

    	ModelAndView mav = new ModelAndView("list/stupid");
    	List<String> results = query.getResults();
    	mav.addObject("results", results);
    	return mav;
    }

}
