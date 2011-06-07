package org.genedb.web.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/gene")
public class WebArtemisStandaloneController {
	
	 @RequestMapping(method=RequestMethod.GET, value="/")
	 public ModelAndView index() {
		 return new ModelAndView("/gene/web-artemis");
	 }
}
