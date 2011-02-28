package org.genedb.web.mvc.controller;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.genedb.querying.core.Query;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.web.mvc.controller.download.ResultEntry;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sleepycat.collections.StoredMap;
/**
 *
 * @author larry@sangerinstitute
 * @desc    The purpose of the class is to cleanup the Result cache of the current user session,
 *          once it's due for expiration
 *
 **/
public class ResultCacheSessionListener implements HttpSessionListener {

    Logger logger = Logger.getLogger(ResultCacheSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        logger.debug(String.format("Entered ResultCacheSessionListener.sessionCreated for %s", event.getSession().getId()));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        //Initial plumbing code
        ServletContext context = event.getSession().getServletContext();
        WebApplicationContext wac =
            WebApplicationContextUtils.getWebApplicationContext(context);

        //Retrieve the ResultCacheFactory bean
        ResultsCacheFactory resultsCacheFactory = (ResultsCacheFactory)wac.getBean("resultsCacheFactory");
        
        HttpSession session = event.getSession();
        
        //The session ID is used to construct keys used to caching results
        String sessionId = session.getId();
        
        logger.info("Attempting to cleanup session " + sessionId);
        
        //Remove all relevant results before session is invalidated
        StoredMap<String, ResultEntry> storedMap = resultsCacheFactory.getResultsCacheMap();
        for (String key: storedMap.keySet()){
            if (key.startsWith(sessionId)){
                storedMap.remove(key);
                logger.info(String.format("results cache with key %s removed...", key));
            }
        }
        
        
        HistoryManager historyManager = (HistoryManager) session.getAttribute(HttpSessionHistoryManagerFactory.HISTORY_MANAGER);
        if (historyManager != null) {
        	logger.info("removing " + historyManager + " from session " + sessionId);
        	session.removeAttribute(HttpSessionHistoryManagerFactory.HISTORY_MANAGER);
        } else {
        	logger.info("no  historyManager associated with session " + sessionId);
        }
        
        @SuppressWarnings("unchecked")
        List<HistoryItem> historyItemList = (List<HistoryItem>) session.getAttribute(HttpSessionHistoryManager.HISTORY_LIST);
        if (historyItemList != null) {
        	
        	for (HistoryItem historyItem : historyItemList) {
        		Query q = historyItem.getQuery();
        		historyItem.cleanup();
        	}
        	
        	logger.info("removing " + historyItemList + " from session " + sessionId);
        	session.removeAttribute(HttpSessionHistoryManager.HISTORY_LIST);
        }
        
        logger.info(String.format("Ended ResultCacheSessionListener.sessionDestroyed for %s", event.getSession().getId()));
        
        
        
    }

}
