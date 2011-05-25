package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.genedb.querying.core.PagedQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;
import org.genedb.querying.history.QueryHistoryItem;

import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * @author Adrian Tivey
 * @author gv1
 */
public class HttpSessionHistoryManager implements HistoryManager {
	
	private static final Logger logger = Logger.getLogger(HttpSessionHistoryManager.class);
	
	// TODO use map for storage
    // gv1 TODONE

    //private int nextNumber = 1;
    //private int version = 1;

    public static final String HISTORY_LIST = "_HISTORY_LIST";
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
    public LinkedHashMap<String, HistoryItem> getHistoryItems() {
    	LinkedHashMap<String, HistoryItem> ret = (LinkedHashMap<String, HistoryItem>) sessionReference.get().getAttribute(HISTORY_LIST);
        if (ret == null) {
            ret = new LinkedHashMap<String, HistoryItem>();
            sessionReference.get().setAttribute(HISTORY_LIST, ret);
        }
        return ret;
    }

    public HistoryItem getHistoryItemByName(String name) {
    	return getHistoryItems().get(name);
    }
    
    public HistoryItem getHistoryItemByID(int id) {
    	int i =0;
    	for (HistoryItem item : getHistoryItems().values()) {
    		if (i == id) {
    			return item;
    		}
    		i++;
    	}
    	return null;
    }


    public HistoryItem getHistoryItemByType(HistoryType historyType) {
        for (HistoryItem historyItem : getHistoryItems().values()) {
            if (historyItem.getHistoryType().equals(historyType)) {
                return historyItem;
            }
        }
        return null;
    }
    
    public QueryHistoryItem addQueryHistoryItem(String name, PagedQuery query) {
    	QueryHistoryItem item = (QueryHistoryItem) getHistoryItemByName(name);
    	
    	if (item == null) {
    		item = new QueryHistoryItem(name);
    		item.setHistoryType(HistoryType.QUERY);
    		item.setQuery(query);
    		
    		
    		
    		getHistoryItems().put(name, item);
    	} 
    	
		return item;
		
    }
    
    /* (non-Javadoc)
     * @see org.genedb.web.mvc.controller.HistoryManager#addHistoryItem(java.lang.String, java.util.List)
     */
    public HistoryItem addHistoryItem(String name, HistoryType type, List<String> ids) {
    	
    	HistoryItem item = getHistoryItemByName(name);
    	
    	if (item == null) {
    		item = new HistoryItem(name, ids);
    		item.setHistoryType(type);
    		getHistoryItems().put(name, item);
    	} else {
    		for (String id : ids) {
    			item.addResult(id);
    		}
    	}
    	
		return item;
    }
    
    /*
     * gv1 - added a history item without ids, but a name and type
     */
    public HistoryItem addHistoryItem(String name, HistoryType type) {
    	
    	HistoryItem item = getHistoryItemByName(name);
    	
    	if (item == null) {
    		item = new HistoryItem(name);
    		item.setHistoryType(type);
    		getHistoryItems().put(name, item);
    		logger.info("Item has been created ");
    		
    	} else {
    		logger.info("Item already there ");
    	}
    	
    	logger.info(this.getHistoryItems());
		logger.info(this.getHistoryItems().get(name));
    	
    	
		return item;
    }
    
    public HistoryItem addHistoryItem(String name, HistoryType type,String id) {
    	return this.addHistoryItem(name, type, Lists.newArrayList(id) );
    }

    public HistoryItem addHistoryItem(HistoryType type, String id) {
    	return this.addHistoryItem(type.name(), type, Lists.newArrayList(id) );
    	
    }
    
    public String getCartName() {
        return cartName;
    }

    public void removeItem(String name) {
    	getHistoryItems().remove(name);
    }
    
//    public void removeItem(int index, int version) {
//        List<HistoryItem> history = getHistoryItems();
//        if (version != this.version) {
//            throw new RuntimeException("Version mismatch");
//        }
//        if (index < 0 || index > history.size()) {
//            throw new IllegalArgumentException("Index is out of range");
//        }
//        history.remove(index);
//        version++;
//    }

//    public String getNextName() {
//        String ret = NumberNameConverter.convert(nextNumber);
//        nextNumber++;
//        return ret;
//    }
//
//    public int getVersion() {
//        return version;
//    }

    public int getNumHistoryItems() {
        return getHistoryItems().size();
    }

    @Override
    public String getFormalName(String name) {
        return name;
    }

	@Override
	public QueryHistoryItem getQueryHistoryItem(String name) {
		return (QueryHistoryItem) getHistoryItemByName(name);
	}

}