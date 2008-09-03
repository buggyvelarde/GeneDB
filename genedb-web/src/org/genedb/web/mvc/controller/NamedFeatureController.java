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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.ImageMapSummary;
import org.genedb.web.gui.ProteinMapDiagram;
import org.genedb.web.gui.RenderedProteinMap;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
public class NamedFeatureController extends PostOrGetFormController {
     private static final Logger logger = Logger.getLogger(NamedFeatureController.class);

    private SequenceDao sequenceDao;
    private String geneView;
    private String geneDetailsView;
    //private String geneSequenceView;

    private BlockingCache dtoCache;
    private String dtoCacheName;

    private BlockingCache proteinMapCache;
    private String proteinMapCacheName;

    private CacheManager cacheManager;

    private ModelBuilder modelBuilder;

    @PostConstruct
    private synchronized void furtherCacheConfiguration() {
        makeBlockingCache(dtoCache, dtoCacheName);
        makeBlockingCache(proteinMapCache, proteinMapCacheName);
    }


    private BlockingCache makeBlockingCache(BlockingCache bc, String name) {
        if (bc != null) {
            return bc;
        }
        Ehcache cache = cacheManager.getEhcache(name);
        if (!(cache instanceof BlockingCache)) {
            //decorate and substitute
            BlockingCache newBlockingCache = new BlockingCache(cache);
            cacheManager.replaceCacheWithDecoratedCache(cache, newBlockingCache);
        }
        return (BlockingCache) cacheManager.getEhcache(name);
    }


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

        if (!(feature instanceof Transcript)) {
            // If feature isn't transcript redirect - include model
            // is it part of a gene
        }

        String viewName = nlb.isDetailsOnly() ? geneDetailsView : geneView;
//        if (nlb.isSequenceView()) {
//            viewName = geneSequenceView;
//        }

        TranscriptDTO dto = null;
        Element element = dtoCache.get(feature.getUniqueName());
        if (element == null) {
            // Get model
            dtoCache.put(new Element(feature.getUniqueName(), dto));
        } else {
            dto = (TranscriptDTO) element.getValue();
        }

        Map<String, Object> model = modelBuilder.prepareFeature(feature);

        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("dto", dto);

        if (model.containsKey("polypeptide")) {
            Polypeptide polypeptide = (Polypeptide) model.get("polypeptide");
            @SuppressWarnings("unchecked")
            List<PolypeptideRegionGroup> domainInformation = (List<PolypeptideRegionGroup>) model.get("domainInformation");

            ImageMapSummary ims = null;
            element = proteinMapCache.get(feature.getUniqueName());
            if (element != null) {
                ims = (ImageMapSummary) element.getValue();
                if (ims.isValid()) {
                    mav.addObject("proteinMap", ims);
                }
            } else {
                // Get image
                ProteinMapDiagram diagram = new ProteinMapDiagram(polypeptide, domainInformation);
                if (!diagram.isEmpty()) {
                    RenderedProteinMap renderedProteinMap = new RenderedProteinMap(diagram);

                    ims = new ImageMapSummary(
                            renderedProteinMap.getWidth(),
                            renderedProteinMap.getHeight(),
                            DiagramCache.fileForDiagram(renderedProteinMap, getServletContext()),
                            renderedProteinMap.getRenderedFeaturesAsHTML("proteinMapMap"));
                    mav.addObject("proteinMap", ims);

                } else {
                    ims = new ImageMapSummary();
                }
                proteinMapCache.put(new Element(feature.getUniqueName(), dto));
            }

        }

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

//    public void setGeneSequenceView(String geneSequenceView) {
//        this.geneSequenceView = geneSequenceView;
//    }

    public void setDtoCache(BlockingCache dtoCache) {
        this.dtoCache = dtoCache;
    }

    public void setDtoCacheName(String dtoCacheName) {
        this.dtoCacheName = dtoCacheName;
    }

    public void setProteinMapCache(BlockingCache proteinMapCache) {
        this.proteinMapCache = proteinMapCache;
    }

    public void setProteinMapCacheName(String proteinMapCacheName) {
        this.proteinMapCacheName = proteinMapCacheName;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }



    public static class NameLookupBean {
        private String name;
        private boolean detailsOnly = false;
        //private boolean sequenceView = false;

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

//        public boolean isSequenceView() {
//            return sequenceView;
//        }
//
//        public void setSequenceView(boolean sequenceView) {
//            this.sequenceView = sequenceView;
//        }

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