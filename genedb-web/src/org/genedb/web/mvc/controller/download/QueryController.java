package org.genedb.web.mvc.controller.download;

import org.genedb.query.core.Query;
import org.genedb.query.core.QueryException;
import org.genedb.query.core.QueryFactory;
import org.genedb.query.hql.ProteinLengthQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import javax.servlet.ServletRequest;

import com.sshtools.j2ssh.net.HttpRequest;


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
        model.addAttribute("query", query);
        return "search/query";
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

        //QueryExecutor executor = queryExecutorFactory.retrieveExecutor(query);

    	ModelAndView mav = new ModelAndView("features/stupid");
    	List<String> results = null;// = executor.execute(query);
    	mav.addObject("results", results);
    	return mav;
    }

}
