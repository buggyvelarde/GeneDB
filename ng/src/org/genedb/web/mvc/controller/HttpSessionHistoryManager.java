package org.genedb.web.mvc.controller;

import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

    public HistoryItem getHistoryItemByName(String name) {
        for (HistoryItem historyItem : getHistoryItems()) {
            if (historyItem.getName().equals(name)) {
                return historyItem;
            }
        }
        return null;
    }


    public HistoryItem getHistoryItemByType(HistoryType historyType) {
        for (HistoryItem historyItem : getHistoryItems()) {
            if (historyItem.getHistoryType().equals(historyType)) {
                return historyItem;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.genedb.web.mvc.controller.HistoryManager#addHistoryItem(java.lang.String, java.util.List)
     */
    public HistoryItem addHistoryItem(String name, HistoryType type, List<String> ids) {

        List<HistoryItem> history = getHistoryItems();
        boolean found = false;
        for (HistoryItem hi : history) {
            if (hi.getName().equals(name) && hi.getHistoryType().equals(type)) {
                found = true;
                return hi;
                
            }
        }
        if(!found) {
          HistoryItem item = new HistoryItem(name, ids);
          item.setHistoryType(type);
          history.add(item);
          version++;
          return item;
        }

        return null;
    }


    public HistoryItem addHistoryItem(HistoryType type, String id) {
        List<HistoryItem> history = getHistoryItems();
        HistoryItem found = null;
        for (HistoryItem item : history) {
            if (item.getHistoryType().equals(type)) {
                found = item;
                break;
            }
        }
        if (found == null) {
            found = createNewHistoryItem(null, type);
            history.add(found);
        }
        found.addUniqueResult(id);
        version++;
        return found;
    }

    private HistoryItem createNewHistoryItem(String name, HistoryType historyType) {
        if (name == null) {
            name = historyType.name();
        }
        HistoryItem ret = new HistoryItem(name);
        ret.setHistoryType(historyType);
        return ret;
    }

    public HistoryItem addHistoryItem(String name, HistoryType type,String id) {
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
            version++;
            return found;
        }

        return null;
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

    @Override
    public String getFormalName(String name) {
        // TODO Auto-generated method stub
        return name;
    }

}