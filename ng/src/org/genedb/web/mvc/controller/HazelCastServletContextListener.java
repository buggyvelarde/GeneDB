package org.genedb.web.mvc.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;

public class HazelCastServletContextListener implements ServletContextListener {
	
	private static final Logger logger = Logger.getLogger(HazelCastServletContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Starting up context listener.");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Shutting down hazelcasts!");
		Hazelcast.shutdownAll();
	}

}
