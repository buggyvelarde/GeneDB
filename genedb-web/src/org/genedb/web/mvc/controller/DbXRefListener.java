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


import org.genedb.db.dao.GeneralDao;

import org.gmod.schema.mapped.Db;

import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.common.collect.Maps;

public class DbXRefListener implements ServletContextListener {

    public static final String DB_URL_MAP = "_DB_URL_MAP";

    //This method is invoked when the Web Application
    //is ready to service requests
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        WebApplicationContext wac =
            WebApplicationContextUtils.getWebApplicationContext(context);

        GeneralDao generalDao = (GeneralDao) wac.getBean("generalDao");
        List<Db> dbs = generalDao.getAllDbs();

        Map<String, String> dbUrlMap = Maps.newHashMap();
        for (Db db : dbs) {
            if (StringUtils.hasText(db.getUrlPrefix())) {
                dbUrlMap.put(db.getName(), db.getUrlPrefix());
            }
        }

        context.setAttribute(DB_URL_MAP, dbUrlMap);
    }


    public void contextDestroyed(ServletContextEvent event) {
        // Deliberately empty
    }

}