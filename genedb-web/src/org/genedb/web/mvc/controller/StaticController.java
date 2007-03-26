package org.genedb.web.mvc.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 *
 * @author Adrian Tivey
 */
public class StaticController extends AbstractController {

	/**
	 * Custom handler for homepage
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
//	        List<String> answers = new ArrayList<String>();
//	        if (WebUtils.extractTaxonOrOrganism(request, false, true, answers)) {
//	            	if (answers.size() > 0) {
//	            	    Taxon taxon = TaxonUtils.getTaxonFromList(answers, 0);
//	            	    return new ModelAndView("homepages/"+taxon.getHomepageViewName(), "taxon", taxon);
//	            	}
//	        }
	        return new ModelAndView("static/general/logos");
	}


}