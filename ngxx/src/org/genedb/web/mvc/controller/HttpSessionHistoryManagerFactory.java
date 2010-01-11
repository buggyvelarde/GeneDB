package org.genedb.web.mvc.controller;

import org.genedb.querying.history.HistoryManager;

import javax.servlet.http.HttpSession;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
public class HttpSessionHistoryManagerFactory implements HistoryManagerFactory {
    
    private static final String HISTORY_MANAGER = "_HISTORY_MANAGER";

    public HistoryManager getHistoryManager(Object key) {
        HttpSession session = (HttpSession) key;
        HistoryManager ret = null;
        ret = (HistoryManager) session.getAttribute(HISTORY_MANAGER);
        if (ret == null) {
            ret = new HttpSessionHistoryManager(session);
            session.setAttribute(HISTORY_MANAGER, ret);
        }
        return ret;
    }

}