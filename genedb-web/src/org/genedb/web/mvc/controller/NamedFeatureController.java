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
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;

/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/NamedFeature")
@SessionAttributes("results")
//@ManagedResource(objectName="bean:name=namedFeatureController", description="NamedFeature Controller")
public class NamedFeatureController extends TaxonNodeBindingFormController {
     private static final Logger logger = Logger.getLogger(NamedFeatureController.class);

    private SequenceDao sequenceDao;
    private String formView;
    private String geneView;
    private String geneDetailsView;
    private int cacheHit = 0;
    private int cacheMiss = 0;

    private BerkeleyMapFactory bmf;
    private ModelBuilder modelBuilder;
    private HistoryManagerFactory hmFactory;


    @Override
    public void setHistoryManagerFactory(HistoryManagerFactory hmFactory) {
        this.hmFactory = hmFactory;
    }

    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView lookUpFeature(HttpServletRequest request, HttpServletResponse response,
            NameLookupBean nlb, BindingResult be) throws Exception {

        logger.debug("Trying to find NamedFeature of '"+nlb.getName()+"'");

        Feature feature = sequenceDao.getFeatureByUniqueName(nlb.getName(), Feature.class);
        if (feature == null) {
            logger.warn(String.format("Failed to find feature '%s'", nlb.getName()));
            be.reject("no.results");
            return showForm(request, response, be);
        }

        Transcript transcript = modelBuilder.findTranscriptForFeature(feature);
        if (transcript == null) {
            // If feature isn't transcript redirect - include model
            // is it part of a gene
            logger.warn(String.format("Failed to find transcript for an id of '%s'", nlb.getName()));
            be.reject("no.results");
            return showForm(request, response, be);
        }

        String viewName = nlb.isDetailsOnly() ? geneDetailsView : geneView;

        TranscriptDTO dto = bmf.getDtoMap().get(transcript.getFeatureId());

        if (dto == null) {
            cacheMiss++;
            logger.error(String.format("dto cache miss for '%s'. Looked for featureId of '%d'", feature.getUniqueName(), feature.getFeatureId()));
            throw new RuntimeException(String.format("Unable to find '%s' in cache", feature.getUniqueName()));
        }

        cacheHit++;
        logger.trace("dto cache hit for '"+feature.getUniqueName());
        HistoryManager hm = hmFactory.getHistoryManager(request.getSession());
        HistoryItem autoBasket = hm.getHistoryItemByType(HistoryType.AUTO_BASKET);
        logger.debug(String.format("Basket is '%s'", autoBasket));
        hm.addHistoryItem(HistoryType.AUTO_BASKET, feature.getUniqueName());
//                if (nlb.isAddToBasket()) {
//                    hm.addHistoryItem(HistoryType.BASKET, feature.getUniqueName());
//                // Add message here
//            }

        HashMap<String, Object> model = Maps.newHashMap();

        model.put("taxonNodeName", dto.getOrganismCommonName());
        model.put("dto", dto);


        if (nlb.getKey() != null &&
                StringUtils.hasText(nlb.getKey()) &&
                (nlb.getResultsLength() > 0) &&
                (nlb.getIndex() > 0) &&
                nlb.getIndex() < nlb.getResultsLength()) {

            model.put("key", nlb.getKey());
            model.put("index", nlb.getIndex());
            model.put("resultsLength", nlb.getResultsLength());
        }


        HistoryItem basket = hm.getHistoryItemByType(HistoryType.BASKET);
        logger.debug(String.format("Basket is '%s'", basket));
        if (basket != null && basket.containsEntry(feature.getUniqueName())) {
            logger.trace(String.format("Setting inBasket to true for '%s'", feature.getUniqueName()));
            model.put("inBasket", Boolean.TRUE);
        } else {
            logger.trace(String.format("Setting inBasket to false for '%s'", feature.getUniqueName()));
            model.put("inBasket", Boolean.FALSE);
        }

        return new ModelAndView(viewName, model);
    }

    // TODO
    private ModelAndView showForm(HttpServletRequest request,
            HttpServletResponse response, BindingResult be) {
        return new ModelAndView("/Query?q=allNameProduct");
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setGeneView(String geneView) {
        this.geneView = geneView;
    }

    public void setGeneDetailsView(String geneDetailsView) {
        this.geneDetailsView = geneDetailsView;
    }

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }


    @ManagedAttribute(description="The no. of times the controller found the entry in the cache")
    public int getCacheHit() {
        return cacheHit;
    }

    @ManagedAttribute(description="The no. of times the controller didn't find the entry in the cache")
    public int getCacheMiss() {
        return cacheMiss;
    }



    public static class NameLookupBean {
        private String name;
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

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
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



    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

    public String getFormView() {
        return formView;
    }

    public void setFormView(String formView) {
        this.formView = formView;
    }

}
