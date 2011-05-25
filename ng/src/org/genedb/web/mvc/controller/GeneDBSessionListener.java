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


import org.genedb.web.mvc.controller.download.ResultEntry;
//import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.google.common.collect.Lists;
import com.sleepycat.collections.StoredMap;

public class GeneDBSessionListener implements HttpSessionListener {
    
    private static final Logger logger = Logger.getLogger(GeneDBSessionListener.class);


    @Override
    public void sessionCreated(HttpSessionEvent arg0) {
        // Deliberately empty
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        
        if (session == null) {
            return;
        }
        
        String key = session.getId();
        logger.trace("Expiring session "+key);
        String prefix = key + ":";
        
        ServletContext context = session.getServletContext();
        WebApplicationContext wac =
            WebApplicationContextUtils.getWebApplicationContext(context);
        
        
//        ResultsCacheFactory rcf = (ResultsCacheFactory) wac.getBean("resultsCacheFactory");
//        
//        List<String> keys = Lists.newArrayList();
//        StoredMap<String, ResultEntry> cache = rcf.getResultsCacheMap();
//        for (String k : cache.keySet()) {
//            if (k.startsWith(prefix)) {
//                keys.add(k);
//            }
//        }
//        for (String k : keys) {
//            logger.trace("Removing entry "+k);
//            cache.remove(k);
//        }
    }

}