package org.genedb.web.mvc.controller.services;

import java.util.Locale;

import org.apache.log4j.Logger;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

public class SingleViewViewResolver implements ViewResolver, Ordered{
    
    private static final Logger logger = Logger.getLogger(SingleViewViewResolver.class);
    
	private final View view;

	private int order;

	public SingleViewViewResolver(View view) {
		this.view = view;
	}

	
	public View resolveViewName(String viewName, Locale locale) throws Exception {
	    logger.info("view NAME??? " + viewName + " locale???" + locale);
	    logger.info("returning view " + view);
		return view;
	}

	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
