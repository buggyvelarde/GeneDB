/*
 * Copyright (c) 2006-2007 Genome Research Limited.
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
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.feature.AbstractGene;
import org.gmod.schema.sequence.feature.Polypeptide;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Looks up a feature by uniquename, and possibly synonyms
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class BrowseTermController extends TaxonNodeBindingFormController {

    private static final Logger logger = Logger.getLogger(BrowseTermController.class);
    private SequenceDao sequenceDao;
    private GeneDBWebUtils webUtils;
    private String geneView;

    @Override
    protected Map<?,?> referenceData(@SuppressWarnings("unused") HttpServletRequest request) throws Exception {
        Map<String,Object> reference = new HashMap<String,Object>();
        reference.put("categories", BrowseCategory.values());
        return reference;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        BrowseTermBean btb = (BrowseTermBean) command;
        String orgNames = TaxonUtils.getOrgNamesInHqlFormat(btb.getOrganism());
        Map<String, Object> model = new HashMap<String, Object>();
        String category = btb.getCategory().toString();

        /* This is to include all the cvs starting with CC.
         * In future when the other cvs have more terms in,
         * this can be removed and the other cvs starting
         * with CC can be added to BrowseCategory
         */
        List<Feature> results;
        if(category.equals("ControlledCuration"))
            results = sequenceDao.getFeaturesByCvNamePatternAndCvTermNameAndOrganisms(
                "CC\\_%", btb.getTerm(), orgNames);
        else
            results = sequenceDao.getFeaturesByCvNameAndCvTermNameAndOrganisms(
        		category, btb.getTerm(), orgNames);

        if (results == null || results.size() == 0) {
            logger.info("result is null");
            be.reject("no.results");
            return showForm(request, response, be);
        }

        if (results.size() == 1) {
            AbstractGene gene;
            if (results.get(0) instanceof Polypeptide) {
                Polypeptide polypeptide = (Polypeptide) results.get(0);
                gene = polypeptide.getGene();
            } else {
                gene = (AbstractGene) results.get(0);
            }
            webUtils.prepareGene(gene, model);
            return new ModelAndView(geneView, model);
        }

        List<Feature> newResults = new ArrayList<Feature>(results.size());
        for (Feature feature : results) {
            if (!GeneUtils.isPartOfGene(feature)) {
                newResults.add(feature);
            } else {
                newResults.add(GeneUtils.getGeneFromPart(feature));
            }
        }

        model.put("results", newResults);
        model.put("controllerPath", "/BrowseTerm");

        return new ModelAndView(getSuccessView(), model);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public String getGeneView() {
        return geneView;
    }

    public void setGeneView(String geneView) {
        this.geneView = geneView;
    }

    public void setWebUtils(GeneDBWebUtils webUtils) {
        this.webUtils = webUtils;
    }

}

class BrowseTermBean {

    private BrowseCategory category;
    private String term;
    private String organism;

    public BrowseCategory getCategory() {
        return this.category;
    }

    public void setCategory(BrowseCategory category) {
        this.category = category;
    }

    public String getTerm() {
        return this.term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

}
