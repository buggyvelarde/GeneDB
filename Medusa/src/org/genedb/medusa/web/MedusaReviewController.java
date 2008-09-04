package org.genedb.medusa.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This controller interacts with the review tool/web-page to accept or deny
 * a particular ChangeLogMessage
 *
 * @author art
 */
public class MedusaReviewController {
    private String formView;
    private String resultView;


    @RequestMapping(method=RequestMethod.GET)
    public String prepareForm() {
        return formView;
    }

    @RequestMapping(method=RequestMethod.POST)
    public String processForm() {
        return resultView;
    }

}
