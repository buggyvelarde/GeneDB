package org.genedb.web.mvc.controller.services;

import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * A special ModelAndView for reporting errors in a standardised way to the web-service client.
 * 
 * @author gv1
 *
 */
public class ErrorReport extends ModelAndView {
	
	public enum ErrorType
	{
		MISSING_PARAMETER,
		INVALID_PARAMETER,
		NO_RESULT,
		QUERY_FAILURE
	}
	
	public ErrorReport(String viewName, ErrorType errorType, String message)
	{
		super(viewName);
		addObject("error_type", new GenericObject("error_type", errorType.toString().toLowerCase()));
		addObject("error", new GenericObject("error", message));
	}
}
