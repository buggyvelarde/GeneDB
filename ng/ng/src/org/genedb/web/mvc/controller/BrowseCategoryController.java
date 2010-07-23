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
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.db.taxon.TaxonNodeListFormatter;
import org.genedb.querying.tmpquery.BrowseCategory;

import org.gmod.schema.utils.CountedName;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.xml.MarshallingView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Returns cvterms based on a particular cv
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/category")
public class BrowseCategoryController extends BaseController {

    private String formView;
    private String successView;

    private static final Logger logger = Logger.getLogger(BrowseCategoryController.class);
    private static final String RESULTS_ATTR = "results";

    private CvDao cvDao;





    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm(Model model) {
        logger.warn("called method 1");
        model.addAttribute("categories", BrowseCategory.values());
        BrowseCategoryBean bc = new BrowseCategoryController.BrowseCategoryBean();
        model.addAttribute("browseCategory", bc);
        return formView;
    }

    @RequestMapping(method= RequestMethod.GET, value="/{category}", params="taxons")
    public ModelAndView listCategory(HttpSession session,
            @PathVariable BrowseCategory category,
            @RequestParam("taxons") TaxonNodeList taxons,
            Model model) {
        BrowseCategoryController.BrowseCategoryBean bean = new BrowseCategoryBean();
        bean.setCategory(category);
        bean.setTaxons(taxons);

        return setUpForm(bean, session, model);
    }


    @RequestMapping(method = RequestMethod.GET, params = {"category","taxons"})
    public ModelAndView setUpForm(
            BrowseCategoryController.BrowseCategoryBean bean,
            HttpSession session,
            Model model) {
        logger.warn("Called method 2");

        model.addAttribute("categories", BrowseCategory.values());
        model.addAttribute("browseCategory", bean);

        //Clear session of any search result
        session.removeAttribute(RESULTS_ATTR);

        //-------------------------------------------------------------------------------
        //Collection<String> orgNames = TaxonUtils.getOrgNames(bcb.getOrganism());
        //Collection<String> orgNames = Arrays.asList(new String[] {"Pfalciparum"});
        /* This is to include all the cvs starting with CC. In future when the other cvs have more terms in,
         * this can be removed and the other cvs starting with CC can be added to BrowseCategory
         */
        TaxonNodeList taxons = bean.getTaxons();
        String orgName = "Root";
        List<String> orgNames = new ArrayList<String>();
        if (taxons.getNodes().size() > 0) {
            orgName = taxons.getNodes().get(0).getLabel();
            for (TaxonNode tn : taxons.getNodes()) {
                orgNames.addAll(tn.getAllChildrenNames());
            }
        }
        List<CountedName> results = cvDao.getCountedNamesByCvNamePatternAndOrganism(bean.getCategory().getLookupName(), orgNames, true);

        if (results.isEmpty()) {
            logger.info("result is null");
            model.addAttribute("noResultFound", true);

            return new ModelAndView("jsp:search/browseCategory", "browseCategory", bean);
        }
        logger.debug(results.get(0));

        // Go to list results page
        ModelAndView mav = new ModelAndView("jsp:list/categories");
        mav.addObject("results", results);
        mav.addObject("category", bean.getCategory());
        mav.addObject("taxons", orgName);
        return mav;
        //-------------------------------------------------------------------------------
    }

    protected Map<String,BrowseCategory[]> referenceData(HttpServletRequest request) throws Exception {
        Map<String,BrowseCategory[]> reference = new HashMap<String,BrowseCategory[]>();
        reference.put("categories", BrowseCategory.values());
        return reference;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public static class BrowseCategoryBean {

        private BrowseCategory category;
        private TaxonNodeList taxons;

        public TaxonNodeList getTaxons() {
            return taxons;
        }
        public void setTaxons(TaxonNodeList taxons) {
            this.taxons = taxons;
        }
        public BrowseCategory getCategory() {
            return this.category;
        }
        public void setCategory(BrowseCategory category) {
            this.category = category;
        }
    }

    public void setFormView(String formView) {
        this.formView = formView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

}
