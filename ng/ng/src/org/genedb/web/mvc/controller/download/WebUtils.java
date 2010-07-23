package org.genedb.web.mvc.controller.download;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.controller.WebConstants;

public class WebUtils {

	private static Logger logger = Logger.getLogger(WebUtils.class);

	public static void setFlashMessage(String message, HttpSession session) {
		logger.error("Setting flash message '"+message+"' in session '"+session.getId()+"'");
		session.setAttribute(WebConstants.FLASH_MSG, message);
	}

	public static void removeFlashMessage(HttpSession session) {
		session.removeAttribute(WebConstants.FLASH_MSG);
	}

}
