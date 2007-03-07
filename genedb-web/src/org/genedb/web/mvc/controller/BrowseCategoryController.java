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

import org.gmod.schema.utils.CountedName;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

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
public class BrowseCategoryController extends TaxonNodeBindingFormController {
    
    private CvDao cvDao;
    private String cvName;

    

    @SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
    	Map reference = new HashMap();
    	reference.put("categories", BrowseCategory.values());
    	return reference;
	}

	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException be) throws Exception {
        BrowseCategoryBean bcb = (BrowseCategoryBean) command;

        String taxonList = TaxonUtils.getTaxonListFromNodes(bcb.getTaxonNodes());
        List<CountedName> results = cvDao.getCountedNamesByCvNameAndOrganism(bcb.getCategory().toString(), taxonList);
        
        if (results == null || results.size() == 0) {
            logger.info("result is null"); // TODO Improve text
            be.reject("No results"); // FIXME - Should be message key
            return showForm(request, response, be);
        }
        
        // Go to list results page
        ModelAndView mav = new ModelAndView(getSuccessView());
        mav.addObject(results);

        
        return mav;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

}

class BrowseCategoryBean {
    
    private TaxonNode[] taxonNodes;
    private BrowseCategory category;
    
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
    
}