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

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;
import org.genedb.util.Pair;
import org.genedb.web.mvc.controller.download.ResultEntry;
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.ResultsCacheFactory;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Maps;
import com.sleepycat.collections.StoredMap;

/**
 *
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/Feedback")
public class FeedbackController extends TaxonNodeBindingFormController {
     private static final Logger logger = Logger.getLogger(FeedbackController.class);

    private String formView;
    private String geneView;

    private MailSender mailSender;
    private SimpleMailMessage templateMessage;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setTemplateMessage(SimpleMailMessage templateMessage) {
        this.templateMessage = templateMessage;
    }



    @RequestMapping(method=RequestMethod.GET)
    public String displayForm() {

        return "feedback/technical"; // FIXME
    }

    @RequestMapping(method=RequestMethod.POST)
    public String processForm() {
        // Do the business calculations...

        // Call the collaborators to persist the order...

        // Create a thread safe "copy" of the template message and customize it
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        //msg.setTo(order.getCustomer().getEmailAddress());
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

        return ""; // FIXME
    }

    public void setGeneView(String geneView) {
        this.geneView = geneView;
    }


    public static class NameLookupBean {
        private boolean detailsOnly = false;
        private boolean addToBasket = false;
        private String key;
        private int index;
        private int resultsLength;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getResultsLength() {
            return resultsLength;
        }

        public void setResultsLength(int resultsLength) {
            this.resultsLength = resultsLength;
        }

        public boolean isAddToBasket() {
            return addToBasket;
        }

        public void setAddToBasket(boolean addToBasket) {
            this.addToBasket = addToBasket;
        }

        public boolean isDetailsOnly() {
            return detailsOnly;
        }

        public void setDetailsOnly(boolean detailsOnly) {
            this.detailsOnly = detailsOnly;
        }

        /*
         * We need this because the form that is shown when the feature
         * can't be found (search/nameLookup.jsp) expects an 'organism'
         * property.
         */
        public String getOrganism() {
            return null;
        }
    }

    public String getFormView() {
        return formView;
    }

    public void setFormView(String formView) {
        this.formView = formView;
    }

}
