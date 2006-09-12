package org.genedb.web.mvc.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Ken Krebs
 */
public class NonFormController extends MultiActionController {

	/**
	 * Custom handler for gene test
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView featureHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView("featureView");
	}
	
	/**
	 * Custom handler for MOD common URL
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView commonURLHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView("commonURLView");
	}
	
	
	/**
	 * Custom handler for examples
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView examplesHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView("examplesView");
	}
	
	
	/**
	 * Custom handler for examples
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView examplesWorkingHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("examplesWorkingView");
	}
	
	/**
	 * Custom handler for 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView dropDownHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("dropDownView");
	}
	
	/**
	 * Custom handler for 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView pfamHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("pfamTestView");
	}

}