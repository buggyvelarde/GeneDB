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
    protected Map<String,BrowseCategory[]> referenceData(HttpServletRequest request) throws Exception {
        Map<String,BrowseCategory[]> reference = new HashMap<String,BrowseCategory[]>();
        reference.put("categories", BrowseCategory.values());
        return reference;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException be) throws Exception {
        BrowseCategoryBean bcb = (BrowseCategoryBean) command;
        String category = bcb.getCategory().toString();
        String orgNames = TaxonUtils.getOrgNamesInHqlFormat(bcb.getOrganism());

        List<CountedName> results = cvDao.getCountedNamesByCvNameAndOrganism(category, orgNames);
        
        if (results .isEmpty()) {
            logger.info("result is null");
            be.reject("no.results");
            return showForm(request, response, be);
        }
        logger.debug(results.get(0));
        
        // Go to list results page
        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject("results", results);
        mav.addObject("category", category);
        mav.addObject("organism",bcb.getOrganism());
        return mav;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }
    
    public static class BrowseCategoryBean {
        
        private BrowseCategory category;
        private String organism;
        
        public String getOrganism() {
            return organism;
        }
        public void setOrganism(String organism) {
            this.organism = organism;
        }
        public BrowseCategory getCategory() {
            return this.category;
        }
        public void setCategory(BrowseCategory category) {
            this.category = category;
        }
    }
}