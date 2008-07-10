package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;
import org.genedb.query.BasicQueryI;
import org.genedb.query.NumberedQueryI;
import org.genedb.query.QueryPlaceHolder;
import org.genedb.query.Result;
import org.genedb.query.SimpleListResult;
import org.genedb.query.bool.BooleanOp;
import org.genedb.query.bool.BooleanQuery;
import org.genedb.query.history.History;
import org.genedb.query.history.SimpleHistory;
import org.genedb.web.tags.bool.QueryTreeWalker;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
public class HttpSessionHistoryManager implements HistoryManager {
	// TODO use map for storage
	// Synch
    
	private int nextNumber = 1;
	private int version = 1;
	
	private static final String HISTORY_LIST = "_HISTORY_LIST";
	private static final String DEFAULT_CART_NAME = "Feature Basket";
	
	private WeakReference<HttpSession> sessionReference;
	private String cartName = DEFAULT_CART_NAME;
	
	
	public HttpSessionHistoryManager(HttpSession session) {
		this.sessionReference = new WeakReference<HttpSession>(session);
	}

	/* (non-Javadoc)
	 * @see org.genedb.web.mvc.controller.HistoryManager#getHistoryItems()
	 */
	@SuppressWarnings("unchecked")
	public List<HistoryItem> getHistoryItems() {
		List<HistoryItem> ret = (List<HistoryItem>) sessionReference.get().getAttribute(HISTORY_LIST);
		if (ret == null) {
			ret = new ArrayList<HistoryItem>();
			sessionReference.get().setAttribute(HISTORY_LIST, ret);
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.genedb.web.mvc.controller.HistoryManager#addHistoryItem(java.lang.String, java.util.List)
	 */
	public void addHistoryItems(String name, List<String> ids) {
		
		List<HistoryItem> history = getHistoryItems();
		boolean found = false;
		for (HistoryItem hi : history) {
            if (hi.getName().equals(name)) {
                found = true;
                break;
            }
        }
		if(!found) {
		  HistoryItem item = new HistoryItem(name, ids);
		  history.add(item);
		}
		
	}
	
	public void addHistoryItem(String name, String id) {
		List<HistoryItem> history = getHistoryItems();
		HistoryItem found = null;
		for (HistoryItem item : history) {
			if (item.getName().equals(name)) {
				found = item;
				break;
			}
		}
		if (found == null) {
			found = new HistoryItem(name, id);
			history.add(found);
		} else {
			found.addResult(id);
		}
	}

	public String getCartName() {
		return cartName;
	}

	public void removeItem(int index, int version) {
		List<HistoryItem> history = getHistoryItems();
		if (version != this.version) {
			throw new RuntimeException("Version mismatch");
		}
		if (index < 0 || index > history.size()) {
			throw new IllegalArgumentException("Index is out of range");
		}
		history.remove(index);
		version++;
	}

	public String getNextName() {
		String ret = NumberNameConverter.convert(nextNumber);
		nextNumber++;
		return ret;
	}

	public int getVersion() {
		return version;
	}

	public int getNumHistoryItems() {
		return getHistoryItems().size();
	}

}