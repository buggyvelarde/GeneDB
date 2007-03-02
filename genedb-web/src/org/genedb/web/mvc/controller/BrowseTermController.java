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


import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.utils.CountedName;

import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;



/**
 * Looks up a feature by uniquename, and possibly synonyms
 * 
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class BrowseTermController extends PostOrGetFormController {

    private OrganismDao organismDao;
    private CvDaoI cvDaoI;
    private String cvName;

  	
	@Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        boolean post = super.isFormSubmission(request);
        if (post || request.getParameterMap().size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        BrowseCategory bc = (BrowseCategory) command;

        List<CountedName> results = null; // FIXME = cvDaoI.getCvTermsByCvNameAndOrganism(cvName);
        String viewName;
        Map model = new HashMap();
        
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
        model.put("results", results);

        return new ModelAndView((String)null, model);
    }


//    public void setOrganismDao(OrganismDao organismDao) {
//        this.organismDao = organismDao;
//    }
//
//    public void setSequenceDao(SequenceDao sequenceDao) {
//        this.sequenceDao = sequenceDao;
//    }

    public void setCvDaoI(CvDaoI cvDaoI) {
        this.cvDaoI = cvDaoI;
    }

}
