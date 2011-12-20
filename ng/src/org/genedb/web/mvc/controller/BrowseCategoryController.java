/*
 * Copyright (c) 2006-2010 Genome Research Limited.
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

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.tmpquery.BrowseCategory;
import org.gmod.schema.utils.CountedName;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;

/**
 * Returns cvterms based on a particular cv
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/category")
public class BrowseCategoryController extends BaseController {

    private static final Logger logger = Logger.getLogger(BrowseCategoryController.class);

    //private String formView = "jsp:search/browseCategory";
    private String successView = "jsp:list/categories";

    private TaxonNodeManager taxonNodeManager;
    private CvDao cvDao;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setConversionService(conversionService);
    }

    //@RequestMapping(method = RequestMethod.GET)
    public ModelAndView setUpForm(TaxonNodeList taxons) {
    	ModelAndView mav = new ModelAndView(successView);
    	populateMav(mav);
    	List<String> orgNames = taxonNodeManager.getNamesListForTaxons(taxons); // taxonNodeManager.getAllOrgNamesUnlessRoot(taxons);
        String displayName = taxonNodeManager.getSingleStringVersion(orgNames);
    	mav.addObject("taxonNodeName", displayName);
    	mav.addObject("orgNames", orgNames);
        return mav;
    }


    public ModelAndView listCategory(HttpSession session,
        BrowseCategory category,
        TaxonNodeList taxons,
        String format) {

        if (taxons==null) {
            TaxonNode root = taxonNodeManager.getTaxonNodeForLabel("Root");
            taxons = new TaxonNodeList(root);
        }
        
        // using getNamesListForTaxons() because we want to filter out organisms that are not public
        List<String> orgNames = taxonNodeManager.getNamesListForTaxons(taxons); // taxonNodeManager.getAllOrgNamesUnlessRoot(taxons);
        String displayName = taxons.getNodes().get(0).getLabel(); //taxonNodeManager.getSingleStringVersion(orgNames);
        
//        logger.info(orgNames);
//        logger.info(displayName);
        //logger.info(taxons.getNodes().get(0).getLabel());
        
        List<CountedName> results = cvDao.getCountedNamesByCvNamePatternAndOrganism(category.getLookupName(), orgNames, true);

        if (results.isEmpty()) {
            logger.info("result is null");

            ModelAndView mav = new ModelAndView(successView);
            populateMav(mav);
            mav.addObject("noResultFound", true);
            mav.addObject("category", category.name());
            mav.addObject("taxonNodeName", displayName);
            return mav;
        }

        
        // Go to list results page
        ModelAndView mav = new ModelAndView(successView);
        populateMav(mav);
        //mav.addObject("categories", BrowseCategory.values());
        mav.addObject("results", results);
        mav.addObject("category", category.name());
        mav.addObject("taxonNodeName", displayName);
        mav.addObject("orgNames", orgNames);
        
        
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView listCategoryAsHtml(
            HttpSession session, 
            @RequestParam(value = "category", required = false) BrowseCategory category, 
            @RequestParam(value = "taxons", required=false) TaxonNodeList taxons) {
        
//        logger.info("category");
//        logger.info(category);
//        
//        logger.info("taxons");
//        logger.info(taxons);
        
        if (category == null) {
            return setUpForm(taxons);
        }
        
        return listCategory(session, category, taxons, "jsp");
    }

//    @RequestMapping(method= RequestMethod.GET, value="/{category}.json", params="taxons")
//    public ModelAndView listCategoryAsJson(HttpSession session,
//        @PathVariable BrowseCategory category,
//        @RequestParam("taxons") TaxonNodeList taxons) {
//
//    	return listCategory(session, category, taxons, "json");
//    }
//
//
//    @RequestMapping(method= RequestMethod.GET, value="/{category}.xml", params="taxons")
//    public ModelAndView listCategoryAsXml(HttpSession session,
//        @PathVariable BrowseCategory category,
//        @RequestParam("taxons") TaxonNodeList taxons) {
//
//    	return listCategory(session, category, taxons, "xml");
//    }


//    @RequestMapping(method= RequestMethod.GET, value="/{category}/{cvterm}", params="taxons")
//    public ModelAndView listGenesForCvTerm(HttpSession session,
//        @PathVariable(value="category") BrowseCategory category,
//        @PathVariable(value="cvterm") String cvTerm,
//        @RequestParam("taxons") TaxonNodeList taxons) {
//
//        List<String> orgNames = taxonNodeManager.getAllOrgNamesUnlessRoot(taxons);
//        String displayName = taxonNodeManager.getSingleStringVersion(orgNames);
//
//        SequenceDao sequenceDao;
//        sequenceDao.getFeaturesByCvNamePatternAndCvTermNameAndOrganisms(cvNamePattern, cvTerm, orgs);
//
//        List<CountedName> results = cvDao.getCountedNamesByCvNamePatternAndOrganism(category.getLookupName(), orgNames, true);
//
//        if (results.isEmpty()) {
//            logger.info("result is null");
//
//            ModelAndView mav = new ModelAndView(formView);
//            populateMav(mav);
//            mav.addObject("noResultFound", true);
//            mav.addObject("category", category.name());
//            mav.addObject("taxons", displayName);
//            return mav;
//        }
//
//        // Go to list results page
//        ModelAndView mav = new ModelAndView(successView);
//        populateMav(mav);
//        //mav.addObject("categories", BrowseCategory.values());
//        mav.addObject("results", results);
//        mav.addObject("category", category.name());
//        mav.addObject("taxons", displayName);
//        mav.addObject("orgNames", orgNames);
//        return mav;
//        //-------------------------------------------------------------------------------
//    }

    private void populateMav(ModelAndView mav) {
    	List<String> names = Lists.newArrayList();
		for (BrowseCategory bc : BrowseCategory.values()) {
			names.add(bc.name());
			//logger.info(bc.name());
		}
        mav.addObject("categories", names);
	}


	public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

//    public void setFormView(String formView) {
//        this.formView = formView;
//    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }


	public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
		this.taxonNodeManager = taxonNodeManager;
	}

}
