package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/")
public class RootController {

    Logger logger = Logger.getLogger(RootController.class);


    @RequestMapping(method=RequestMethod.GET, value="/")
    public ModelAndView goToHomePage() {

        return new ModelAndView("redirect:/Homepage");
    }


}
