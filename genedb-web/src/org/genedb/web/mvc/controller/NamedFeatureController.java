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

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
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
@ManagedResource(objectName="bean:name=namedFeatureController", description="NamedFeature Controller")
public class NamedFeatureController extends PostOrGetFormController {
     private static final Logger logger = Logger.getLogger(NamedFeatureController.class);

    private SequenceDao sequenceDao;
    private String geneView;
    private String geneDetailsView;
    private int cacheHit = 0;
    private int cacheMiss = 0;

    private BlockingCache dtoCache;

    private ModelBuilder modelBuilder;


    @Override
    //@RequestMapping(method=Request)
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        NameLookupBean nlb = (NameLookupBean) command;

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

        TranscriptDTO dto = null;
        Element element = dtoCache.get(transcript.getUniqueName());

        if (element == null) {
            cacheMiss++;
            logger.error("dto cache miss for '"+feature.getUniqueName());
            dto = modelBuilder.prepareTranscript(transcript);
            dtoCache.put(new Element(feature.getUniqueName(), dto));
        } else {
            logger.debug("dto cache hit for '"+feature.getUniqueName());
            dto = (TranscriptDTO) element.getValue();
            cacheHit++;
        }

        HashMap<String, TranscriptDTO> model = Maps.newHashMap();
        model.put("dto", dto);
        return new ModelAndView(viewName, model);
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

    public void setDtoCache(BlockingCache dtoCache) {
        this.dtoCache = dtoCache;
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

}