package org.genedb.medusa.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This class is a web controller which takes a request and creates one or more 
 * ChangeLogMessage.
 * 
 * These are passed onto the parsing code for further sanity checking, before going onto
 * the review code.
 * 
 * @author art
 */
public class MedusaSubmissionController {
	
	private String formView;
	private String resultView;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public String prepareForm() {
		return formView;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public String processForm() {
		return resultView;
	}
	
}
