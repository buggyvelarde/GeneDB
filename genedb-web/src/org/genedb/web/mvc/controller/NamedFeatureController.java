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

import org.gmod.schema.sequence.Feature;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class NamedFeatureController extends PostOrGetFormController {
     private static final Logger logger = Logger.getLogger(NamedFeatureController.class);

    private SequenceDao sequenceDao;
    private String geneView;
    private String geneDetailsView;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        NameLookupBean nlb = (NameLookupBean) command;

        Feature feature = sequenceDao.getFeatureByUniqueName(nlb.getName(), Feature.class);
        if (feature == null) {
            logger.warn(String.format("Failed to find feature '%s'", nlb.getName()));
            be.reject("no.results");
            return showForm(request, response, be);
        }

        String viewName = nlb.isDetailsOnly() ? geneDetailsView : geneView;
        Map<String, Object> model = modelBuilder.prepareFeature(feature);
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

    private ModelBuilder modelBuilder;

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public static class NameLookupBean {
        private String name;
        private boolean detailsOnly = false;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        /*
         * We need this because the form that is shown when the feature
         * can't be found (search/nameLookup.jsp) expects an 'organism'
         * property.
         */
        public String getOrganism() {
            return null;
        }

        public boolean isDetailsOnly() {
            return detailsOnly;
        }

        public void setDetailsOnly(boolean detailsOnly) {
            this.detailsOnly = detailsOnly;
        }
    }
}