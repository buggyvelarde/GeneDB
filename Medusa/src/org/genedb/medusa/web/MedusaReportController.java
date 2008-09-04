package org.genedb.medusa.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A web controller for managing reporting of changes eg how many genes in organism x
 * have had structural changes between two dates
 *
 * @author art
 */
@Controller
public class MedusaReportController {

    private String formView;
    private String reportView;


    @RequestMapping(method=RequestMethod.GET)
    public String prepareForm() {
        return formView;
    }

    @RequestMapping(method=RequestMethod.POST)
    public String processForm() {
        return reportView;
    }

}
