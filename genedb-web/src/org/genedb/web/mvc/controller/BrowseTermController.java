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

import org.genedb.db.dao.CvDao;
import org.genedb.db.loading.TaxonNode;

import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.utils.CountedName;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;




/**
 * Looks up a feature by uniquename, and possibly synonyms
 * 
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class BrowseTermController extends TaxonNodeBindingFormController {
    
    private CvDao cvDao;
    private String cvName;


    @Override
    protected ModelAndView onSubmit(Object command, BindException be) throws Exception {

        BrowseTermBean btb = (BrowseTermBean) command;
        
        String nodes = StringUtils.arrayToDelimitedString(btb.getTaxonNodes(), " ");

        List<Feature> results = cvDao.getFeaturesByCvNameAndCvTermNameAndOrganism(btb.getCategory(), btb.getTerm(), nodes);
        
        if (results == null || results.size() == 0) {
            logger.info("result is null");
            // TODO - error page
            getFormView();
        }
        
        // Go to list results page
//        ResultBean rb = new ResultBean();
//        List<String> organisms = organismDao.findAllOrganismCommonNames();
//        for (String string : organisms) {
//            logger.info(string);
//        }
        //rb.setResults(organisms);
        //model.put("rb", rb);
        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject(results);

        return mav;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

}


class BrowseTermBean {
    
    private TaxonNode[] taxonNodes;
    private BrowseCategory category;
    private String term;
    
    public BrowseCategory getCategory() {
        return this.category;
    }
    public void setCategory(BrowseCategory category) {
        this.category = category;
    }
    public TaxonNode[] getTaxonNodes() {
        return this.taxonNodes;
    }
    public void setTaxonNodes(TaxonNode[] taxonNodes) {
        this.taxonNodes = taxonNodes;
    }
    public String getTerm() {
        return this.term;
    }
    public void setTerm(String term) {
        this.term = term;
    }

}

