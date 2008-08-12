package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.web.mvc.controller.WebConstants;

import org.apache.log4j.Logger;
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
import org.springframework.web.util.WebUtils;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/Query")
public class QueryController {

    private static final Logger logger = Logger.getLogger(QueryController.class);

    @Autowired
    private QueryFactory queryFactory;



    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm(
            @RequestParam(value="q", required=false) String queryName,
            HttpSession session,
            Model model) {

        if (!StringUtils.hasText(queryName)) {
               session.setAttribute(WebConstants.FLASH_MSG, "Unable to identify which query to use");
            return "redirect:/QueryList";
        }

        Query query = queryFactory.retrieveQuery(queryName);
        if (query == null) {
            session.setAttribute(WebConstants.FLASH_MSG, String.format("Unable to find query called '%s'", queryName));
            return "redirect:/QueryList";
        }

        model.addAttribute("query", query);

        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }

        return "search/"+queryName;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView processSubmit(
            @RequestParam(value="q", required=false) String queryName,
            ServletRequest request,
            HttpSession session
            ) throws QueryException {

        if (!StringUtils.hasText(queryName)) {
            session.setAttribute(WebConstants.FLASH_MSG, "Unable to identify which query to use");
            return new ModelAndView("redirect:/QueryList");
        }

        Query query = queryFactory.retrieveQuery(queryName);
        if (query == null) {
            session.setAttribute(WebConstants.FLASH_MSG, String.format("Unable to find query called '%s'", queryName));
            return new ModelAndView("redirect:/QueryList");
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(query);
        // register custom editors, if desired
        //binder.registerCustomEditor(...);
        binder.bind(request);
        // optionally evaluate binding errors
        Errors errors = binder.getBindingResult();

        if (errors.getErrorCount() != 0) {
            // Problem
        }

        ModelAndView mav = null;
        List<String> results = query.getResults();

        switch (results.size()) {
        case 0:
            logger.error("No results found for query");
            mav = new ModelAndView("search/"+queryName);
            break;
        case 1:
            mav = new ModelAndView("redirect:/NamedFeature");
            // TODO Send feature name
            break;
        default:
            mav = new ModelAndView("list/stupid");
            mav.addObject("results", results);
            break;
        }
        return mav;
    }

}
