/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 *
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/Feedback")
public class FeedbackController extends TaxonNodeBindingFormController {
     private static final Logger logger = Logger.getLogger(FeedbackController.class);

    private String formView = "feedback/general";

    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }



    @RequestMapping(method=RequestMethod.GET)
    public String displayForm() {
        return formView;
    }

    private String[] CURATORS = {"art@sanger.ac.uk"};

    @RequestMapping(method=RequestMethod.POST)
    public String processForm() {
        // Validate

        String uri = "http://api-verify.recaptcha.net/verify";

        RestTemplate template = new RestTemplate();
        String key="fred";

        String s = template.postForObject(uri, key, String.class);
        //if (!"true".equals(s[0])) {

        //}



        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(CURATORS);
        //msg.setText(
        //    "Dear " + order.getCustomer().getFirstName()
        //        + order.getCustomer().getLastName()
        //        + ", thank you for placing order. Your order number is "
        //        + order.getOrderNumber());
        //try{
            this.mailSender.send(msg);
        //}
        //catch(MailException ex) {
            // simply log it and go on...
        //    System.err.println(ex.getMessage());
        //}
            String originalPage = null;
       if (originalPage != null) {
            return "redirect:"+originalPage;
       }
       return "redirect:/Homepage";
    }


    public String getFormView() {
        return formView;
    }

    public void setFormView(String formView) {
        this.formView = formView;
    }

}
