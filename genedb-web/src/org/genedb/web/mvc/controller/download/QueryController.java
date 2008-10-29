package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.web.mvc.controller.WebConstants;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/Query")
public class QueryController {

    private static final Logger logger = Logger.getLogger(QueryController.class);

    @Autowired
    private QueryFactory queryFactory;


    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        return "redirect:/QueryList";
    }

    @RequestMapping(method = RequestMethod.GET , params= "q")
    public String setUpForm(
            @RequestParam(value="q") String queryName,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

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
        logger.error(String.format("The number of parameters is '%d'", request.getParameterMap().keySet().size()));
        if (request.getParameterMap().keySet().size() == 1) {
            // That'll be q so the user has only chosen the query so far, not any values
            Map<String, Object> modelData = query.prepareModelData();
            for (Map.Entry<String, Object> entry : modelData.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
            return "search/"+queryName;
        }


        // Attempt to fill in form
        ServletRequestDataBinder binder = new ServletRequestDataBinder(query);
        // register custom editors, if desired
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), false, 10));
        binder.bind(request);
        // optionally evaluate binding errors
        Errors errors = binder.getBindingResult();
        Validator validator = query.getValidator();
        validator.validate(query, errors);

        if (errors.getErrorCount() != 0) {
            // Problem
            throw new RuntimeException(errors.toString());
        }

        List<String> results = query.getResults();

        switch (results.size()) {
        case 0:
            logger.error("No results found for query");
            session.setAttribute(WebConstants.FLASH_MSG, "No results found - please try again");
            return "search/"+queryName;
        case 1:
            return "redirect:/NamedFeature?name="+results.get(0);
            // TODO Send feature name
        default:
            model.addAttribute("results", results);
            return "list/stupid";
        }
    }

    @RequestMapping(method = RequestMethod.POST , params= "q")
    public String processForm(
            @RequestParam(value="q") String queryName,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

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
        logger.error(String.format("The number of parameters is '%d'", request.getParameterMap().keySet().size()));
        if (request.getParameterMap().keySet().size() == 1) {
            // That'll be q so the user has only chosen the query so far, not any values
            Map<String, Object> modelData = query.prepareModelData();
            for (Map.Entry<String, Object> entry : modelData.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
            return "search/"+queryName;
        }


        // Attempt to fill in form
        ServletRequestDataBinder binder = new ServletRequestDataBinder(query);
        // register custom editors, if desired
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), false, 10));
        binder.bind(request);
        // optionally evaluate binding errors
        Errors errors = binder.getBindingResult();
        Validator validator = query.getValidator();
        validator.validate(query, errors);

        if (errors.getErrorCount() != 0) {
            // Problem
            throw new RuntimeException(errors.toString());
        }

        List<String> results = query.getResults();

        switch (results.size()) {
        case 0:
            logger.error("No results found for query");
            session.setAttribute(WebConstants.FLASH_MSG, "No results found - please try again");
            return "search/"+queryName;
        case 1:
            return "redirect:/NamedFeature?name="+results.get(0);
            // TODO Send feature name
        default:
            model.addAttribute("results", results);
            return "list/stupid";
        }
    }

}
