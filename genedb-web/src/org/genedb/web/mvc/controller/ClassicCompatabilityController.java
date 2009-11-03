package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/genedb/")
public class ClassicCompatabilityController {

    Logger logger = Logger.getLogger(ClassicCompatabilityController.class);

    @RequestMapping(method=RequestMethod.GET, value="organisms")
    public ModelAndView listOrganisms() {

        return new ModelAndView();
    }


    @RequestMapping(method=RequestMethod.GET, params="fulltext/gene/${search}/organism")
    public ModelAndView deleteHistoryItem(HttpServletRequest request,HttpServletResponse response,
            @RequestParam("historyItem") int historyItem) {

//        HttpSession session = request.getSession(false);
//        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
//        logger.info("Removing item from history");
//        historyManager.removeItem(historyItem, historyManager.getVersion());
        return new ModelAndView("redirect:/History/View");
    }


    @RequestMapping(method=RequestMethod.GET, value="/History")
    public ModelAndView listHistory(HttpServletRequest request,
            HttpServletResponse response) {
//        HttpSession session = request.getSession(false);
//        if (session == null) {
//            // No session
//            response.setStatus(SESSION_FAILED_ERROR_CODE);
//            return null;
//        }
//
//        HistoryManager historyManager = historyManagerFactory.getHistoryManager(session);
//
        Map<String,Object> model = new HashMap<String,Object>();
//        model.put("items", historyManager.getHistoryItems());

        return new ModelAndView("history/list", model);
    }

}
