package org.genedb.web.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ArtemisLaunchController extends AbstractController {

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {

        String organism = request.getParameter("organism");
        String chromosome = request.getParameter("chromosome");
        String s = request.getParameter("start");
        String e = request.getParameter("end");

        int start = Integer.parseInt(s) - 13000;
        int end = Integer.parseInt(e) + 13000;

        if (start < 0)
            start = 0;

        String argument = organism + ":" + chromosome + ":" + start + ".." + end;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("argument", argument);
        model.put("offset", s);
        // FreeMarkerView fmv = new FreeMarkerView();
        // fmv.setContentType("application/x-java-jnlp-file");
        // fmv.setUrl("artemis");
        return new ModelAndView("artemis", model);
    }

}
