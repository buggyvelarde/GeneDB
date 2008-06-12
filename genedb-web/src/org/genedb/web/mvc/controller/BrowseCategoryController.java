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
import org.genedb.db.dao.CvDao;
import org.genedb.db.taxon.TaxonNode;

import org.gmod.schema.utils.CountedName;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns cvterms based on a particular cv
 * 
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class BrowseCategoryController extends TaxonNodeBindingFormController {
    
    private static final Logger logger = Logger.getLogger(BrowseCategoryController.class);
	
    private CvDao cvDao;

    @Override
    protected Map<String,BrowseCategory[]> referenceData(@SuppressWarnings("unused") HttpServletRequest request) throws Exception {
        Map<String,BrowseCategory[]> reference = new HashMap<String,BrowseCategory[]>();
        reference.put("categories", BrowseCategory.values());
        return reference;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException be) throws Exception {
        BrowseCategoryBean bcb = (BrowseCategoryBean) command;
        String category = bcb.getCategory().toString();
        logger.info("category is " + category);
        TaxonNode[] taxonNodes = bcb.getOrganism();
        List<String> orgNames = taxonNodes[0].getAllChildrenNames(); // FIXME 
        List<CountedName> results = cvDao.getCountedNamesByCvNameAndOrganism(category, orgNames);
        
        if (results == null || results.size() == 0) {
            logger.info("result is null"); // TODO Improve text
            be.reject("no.results");
            return showForm(request, response, be);
        }
        logger.debug(results.get(0));
        
        // Go to list results page
        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("results", results);
        mav.addObject("category", category);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String org : orgNames) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(org);
        }
        mav.addObject("organism",sb.toString());
        return mav;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

}

class BrowseCategoryBean {
    
    private TaxonNode[] organism;
    private BrowseCategory category;
    
    public BrowseCategory getCategory() {
        return this.category;
    }
    public void setCategory(BrowseCategory category) {
        this.category = category;
    }
    public TaxonNode[] getOrganism() {
        return this.organism;
    }
    public void setOrganism(TaxonNode[] organism) {
        this.organism = organism;
    }
    
}