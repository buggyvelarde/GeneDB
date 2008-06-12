/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.web.mvc.controller;


import org.genedb.db.taxon.TaxonNodeManager;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TaxonManagerListener implements ServletContextListener {

    public static final String TAXON_NODE_MANAGER = "_TAXON_NODE_MANAGER";

    //This method is invoked when the Web Application
    //is ready to service requests
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        WebApplicationContext wac = 
            WebApplicationContextUtils.getWebApplicationContext(context);

        TaxonNodeManager manager = (TaxonNodeManager) wac.getBean("taxonNodeManager");
        context.setAttribute(TAXON_NODE_MANAGER, manager);
    }


    public void contextDestroyed(ServletContextEvent event) {
        // Deliberately empty
    }

}