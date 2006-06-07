package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class WebUtils {

    public static boolean extractTaxonOrOrganism(HttpServletRequest request, boolean required, boolean onlyOne, List<String> answers) {
	boolean problem = false;
	String[] ids = request.getParameterValues("taxId");
	String[] names = request.getParameterValues("org");
	List<String> idsAndNames = new ArrayList<String>();
	if (ids != null) idsAndNames.addAll(Arrays.asList(ids));
	if (names != null) idsAndNames.addAll(Arrays.asList(names));
	
	int length = idsAndNames.size();
	if (required && length==0) {
	    buildErrorMsg(request, "No taxon id (or organism name) supplied when expected");
	    problem = true;
	}
	if (onlyOne && length>1) {
	    buildErrorMsg(request, "Only expected 1 taxon id (or organism name)");
	    problem = true;
	}
			
			
	if (problem) {
	    return false;
	}
	//		if (errMsg == null) {
	//			request.setAttribute(errMsg, errMsg);
	//		}
	answers.addAll(idsAndNames);
	// TODO check required and onlyone
	// TODO store error message if necessary
	// TODO check if ids for which we have data - need new flag
	return true;
    }

    public static void buildErrorMsg(HttpServletRequest request, String msg) {
	List<String> stored = (List<String>) request.getAttribute(WebConstants.ERROR_MSG);
	if (stored == null) {
	    stored = new ArrayList<String>();
	}
	stored.add(msg);
	request.setAttribute(WebConstants.ERROR_MSG, stored);
    }

}
