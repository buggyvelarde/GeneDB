package org.genedb.web.mvc.controller;


import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryUtils;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/History")
public class HistoryController {

    Logger logger = Logger.getLogger(HistoryController.class);

    private HistoryManagerFactory historyManagerFactory;
    private String historyView;
    private String downloadView;
    
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView listHistory(HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession(true);

        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("items", historyManager.getHistoryItems());
        
        Map<String, String> descriptions = new HashMap<String, String>();
        for (Entry<String, HistoryItem> entry : historyManager.getHistoryItems().entrySet()) {

        	HistoryItem item = entry.getValue();
        	logger.info(item.getName());
        	
        	String description = getFormattedParameterMap(item);
        	
        	if (description == null) {
        		if (item.getName().equals("AUTO_BASKET")) {
        			description = "Genomic elements that you have viewed in this session.";
        		}
        		else if (item.getName().equals("BASKET")) {
        			description = "Genomic elements that you have added to the basket in this session.";
        		} else {
        			description = "(no description)";
        		}
        	}
        	
        	descriptions.put(item.getName(), description);
        }
        
        model.put("descriptions", descriptions);

        return new ModelAndView("history/list", model);
    }
    
    

    @RequestMapping(method=RequestMethod.GET, value="/{historyItem}")
    public ModelAndView editHistoryItem(HttpServletRequest request,HttpServletResponse response,
            @PathVariable("historyItem") int historyItem) {

        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("history", historyItem);
        HistoryItem item = historyManager.getHistoryItemByID(historyItem-1);
        
        //model.put("historyVersion", historyManager.getVersion());
        
        model.put("items", item.getIds());
        model.put("historyName", item.getName());

        return new ModelAndView("history/editHistoryItem",model);
    }

    @RequestMapping(method=RequestMethod.POST, value="/{historyItem}")
    public ModelAndView deleteHistoryItems(HttpServletRequest request,HttpServletResponse response,
            @PathVariable("historyItem") int historyItem) {

        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("history", historyItem);
        HistoryItem item = historyManager.getHistoryItemByID(historyItem-1);
        
        List<String> allids = item.getIds();
        
        List<String> hits = Lists.newArrayList();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("item")) {
                name = name.substring(4);
                
                int hitindex = Integer.parseInt(name)-1;
                
                String hit = allids.get(hitindex);
                
                hits.add(hit);
                
            }
        }
        
        allids.removeAll(hits);
        
        logger.info("removing " + hits);
        logger.info(" from " + allids);
        
        item.setIds(allids);
        logger.info(" result " + item.getIds());
        
        if (item.getNumberItems() < 1) {
            return new ModelAndView("history/list");
        }
        
        //model.put("historyVersion", historyManager.getVersion());
        
        model.put("items", item.getIds());
        model.put("historyName", item.getName());

        return new ModelAndView("history/editHistoryItem",model);
    }


    @RequestMapping(method=RequestMethod.POST, params="historyItem")
    public ModelAndView deleteHistoryItem(HttpServletRequest request,HttpServletResponse response,
            @RequestParam("historyItem") int historyItem) {
    	
        HttpSession session = request.getSession(false);
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
        logger.info("Removing item from history");
        
        HistoryItem item = historyManager.getHistoryItemByID(historyItem-1);
        historyManager.removeItem(item.getName());
        return new ModelAndView("redirect:/History");
    }



    
    
    public static String getFormattedParameterMap(HistoryItem item) {
    	
    	Query q = item.getQuery();
    	
    	if (q != null) {
    		
    		Map<String,String> map = QueryUtils.getParameterMap(q);
    		StringBuffer description = new StringBuffer();
    		description.append("<div style='font-size:1.2em;font-weight:bold;text-decoration:underline;'>" + q.getQueryName() + "</div>");
    		
    		for (String key : map.keySet()) {
    			description.append("<div><span style='font-weight:bold;' >" + key + "</span> : <span style='font-style:italic' >" + map.get(key) + "</span></div>");
    		}
    		
    		return description.toString();
    	} 
    
        return "";
    }


    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }

    public String getHistoryView() {
        return historyView;
    }

    public void setHistoryView(String historyView) {
        this.historyView = historyView;
    }

    public String getDownloadView() {
        return downloadView;
    }

    public void setDownloadView(String downloadView) {
        this.downloadView = downloadView;
    }


}
