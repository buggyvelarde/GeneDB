package org.genedb.web.gui;

import java.awt.Toolkit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

public class InitHeadlessToolkitServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(InitHeadlessToolkitServlet.class);
	
    @Override
	public void init() throws ServletException {
    	System.setProperty("java.awt.headless", "true");
    	@SuppressWarnings("unused")
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		super.init();
	}
    
}
