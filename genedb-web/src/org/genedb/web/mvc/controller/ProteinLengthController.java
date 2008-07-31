package org.genedb.web.mvc.controller;

import org.genedb.query.core.QueryException;
import org.genedb.query.hql.ProteinLengthQuery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Controller
@RequestMapping("/ProteinLength")
public class ProteinLengthController {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView setUpForm() {
        ProteinLengthQuery plq = new ProteinLengthQuery();
        ModelAndView mav = new ModelAndView();
        mav.addObject("query", plq);
        mav.setViewName("search/proteinLength");
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST)
	public ModelAndView doForm(ProteinLengthQuery plq) throws QueryException {
    	ModelAndView mav = new ModelAndView();
    	List<String> results = plq.getResults();
    	mav.addObject("results", results);
    	mav.setViewName("features/stupid");
    	return mav;
    }

}
