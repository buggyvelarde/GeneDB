package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder.Output;

/**
 *
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/gmodrest/v1/")
public class CommonUrlController {

    Logger logger = Logger.getLogger(CommonUrlController.class);

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

    @RequestMapping(method=RequestMethod.GET, value="/wibble/organism/embl/{featureName}")
    public ModelAndView dumpEmbl(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("featureName") String featureName) {

        try {
            OutputStream out = response.getOutputStream();
            response.setContentType("text/plain");

            Process p = Runtime.getRuntime().exec(new String[]{"/bin/ls", "/tmp"});
            InputStream in = p.getInputStream();
            int i;
            while ((i = in.read()) != -1) {
                out.write((char)i);
            }
        }
        catch (IOException exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }


        return null;
    }


}
