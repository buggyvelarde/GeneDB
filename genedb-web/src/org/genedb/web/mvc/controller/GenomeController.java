package org.genedb.web.mvc.controller;

import org.genedb.domain.ExtendedOrganism;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GenomeController is the controller for the GMOD common URL
 * mechanism 
 * 
 * @author A. Tivey
 */
public class GenomeController extends AbstractController {
	// TODO Add second view resolver that understand redirect
	// TODO Add redirects for all levels ?
	// TODO Factor out org code
        

	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	private String PATH_PREFIX = "/genome";
	private ExtendedOrganism root;
	
	
	@Required
	public void setRoot(ExtendedOrganism root) {
	    this.root = root;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String urlPath = this.urlPathHelper.getLookupPathForRequest(request);
		String viewName = null;
		if (logger.isDebugEnabled()) {
			logger.debug("Returning view name '" + viewName + "' for lookup path: " + urlPath);
		}
		if (PATH_PREFIX.equals(urlPath)) {
			// FIXME  Also matches /genome/genome
			return new ModelAndView("redirect:genome/");
		}
		if (urlPath.startsWith("/")) {
			urlPath = urlPath.substring(1);
		}
		String[] parts = urlPath.split("/");
		String org = null;
		if (parts.length == 0 || parts[0].length() == 0) {
			return displayOrgs();
		}
		if (parts.length >= 1) {
			org = parts[0];
			if (!isValidOrg(org)) {
				String errMsg = "Unrecognized organism name '"+org+"'";
				ModelAndView mav = displayOrgs();
				mav.addObject(WebConstants.APP_ERROR_MSG, errMsg);
				return mav;
			}
			if (parts.length == 1) {
				return displayVersions(org);
			}
		}
		String version = null;
		if (parts.length >= 2) {
			version = parts[1];
			if ("current".equals(version)) {
				version = getCurrentVersion(org);
			} else {
				if (!isSupportedVersion(org, version)) {
					String errMsg = "Unrecognized version number '"+version+"'";
					ModelAndView mav = displayVersions(org);
					mav.addObject(WebConstants.APP_ERROR_MSG, errMsg);
					return mav;
				}
			}
			if (parts.length == 2) {
				return displayOptions(org, version);
			}
			String type = parts[2];
			if (!isSupportedOption(type)) {
				String errMsg = "Unrecognized type '"+type+"'";
				ModelAndView mav = displayOptions(org, version);
				mav.addObject(WebConstants.APP_ERROR_MSG, errMsg);
				return mav;
			}
		}
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println("<html><body>");
		out.println("<p>urlPath = "+urlPath);
		out.println("<p>format = "+request.getParameter("format"));
		out.println("</body></html>");
		return new ModelAndView(viewName);
	}

	private boolean isSupportedVersion(String org, String version) {
		return "1".equals(version);
	}

	private String getCurrentVersion(String org) {
		return "1";
	}

	private boolean isValidOrg(String org) {
		return ORGANISMS.contains(org);
	}

	private ModelAndView displayOrgs() {
		return createMAV("Organisms", ORGANISMS, "/genedb-web/genome/");
	}

	private ModelAndView displayVersions(String org) {
		List<String> list = new ArrayList<String>();
		list.add("current");
		return createMAV("Versions", list, "/genedb-web/genome/"+org+"/"); 
	}
	
	
	private static List<String> OPTIONS;
	private static List<String> ORGANISMS;
	static {
		OPTIONS = new ArrayList<String>();
		OPTIONS.add("dna");
		OPTIONS.add("mrna");
		OPTIONS.add("ncrna");
		OPTIONS.add("protein");
		OPTIONS.add("feature");
		
		ORGANISMS = new ArrayList<String>();
		ORGANISMS.add("Trypanasoma_brucei_brucei");
		ORGANISMS.add("Leishmania_major");
		ORGANISMS.add("Plasmodium_falciparum");
		ORGANISMS.add("Aspergilis_fumigatus");
	}
	
	private ModelAndView displayOptions(String org, String version) {
		return createMAV("Options", OPTIONS, "/genedb-web/genome/"+org+"/"+version+"/");
	}
	
	private ModelAndView createMAV(String title, List values, String prefix) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageTitle", title);
		map.put("list", values);
		map.put("prefix", prefix);
		return new ModelAndView("commonURL", map); 
	}
	
	private boolean isSupportedOption(String option) {
		return OPTIONS.contains(option);
	}
}
