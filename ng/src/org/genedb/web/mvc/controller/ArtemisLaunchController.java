package org.genedb.web.mvc.controller;

import java.util.HashMap;
import java.util.Map;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/ArtemisLaunch")
public class ArtemisLaunchController {

    private static final int DEFAULT_OFFSET = 13000;

    private int offset = DEFAULT_OFFSET;

    @RequestMapping(method=RequestMethod.GET, value="/{organism}/{chromosome}.jnlp", params={"start", "end"})
    public ModelAndView launchMainArtemis(
            @PathVariable("organism") String organism,
            @PathVariable("chromosome") String chromosome,
            @RequestParam("start") int start,
            @RequestParam("end") int end) {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("organism", organism);
        model.put("chromosome", chromosome);
        model.put("start", start);
        model.put("end", end);

        return new ModelAndView("jsp:artemis/artemis", model);
    }


    @RequestMapping(method=RequestMethod.GET, value="/{systematicId}")
    public ModelAndView launchArtemisGeneBuilder(
            @PathVariable("systematicId") String systematicId) {

        // TODO Check systematic id works

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("systematicId", systematicId);
        return new ModelAndView("jsp:artemis/artemisGeneBuilder", model);
    }

}
