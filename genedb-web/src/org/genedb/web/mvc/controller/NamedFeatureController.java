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
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.helpers.NameLookup;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.util.ArrayList;
import java.util.Collection;
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
public class NamedFeatureController extends SimpleFormController {

    private String listResultsView;
    private String formInputView;
    private SequenceDao sequenceDao;
    private OrganismDao organismDao;

  	
	@Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        NameLookup nl = (NameLookup) command;
        Map<String, Object> model = new HashMap<String, Object>(3);
        String viewName = null;
        if(nl.getLookup() == "" || nl.getLookup() == null) {
        	List <String> err = new ArrayList <String> ();
        	logger.info("Look up is null");
        	err.add("No search String found");
        	err.add("please use the form below to search again");
        	model.put("status", err);
        	model.put("nameLookup", nl);
        	viewName = formInputView;
        	return new ModelAndView(viewName,model);
        }
        
        List<Feature> results = sequenceDao.getFeaturesByAnyName(nl, "gene");
        
        if (results == null || results.size() == 0) {
            logger.info("result is null");
            // TODO Fail page
        }
        if (results.size() > 1) {
            // Go to list results page
        	ResultBean rb = new ResultBean();
        	List<String> organisms = organismDao.findAllOrganismCommonNames();
        	for (String string : organisms) {
				logger.info(string);
			}
        	rb.setResults(organisms);
        	viewName = listResultsView;
            model.put("rb", rb);
            model.put("results", results);
        } else {
            Feature feature = results.get(0);
            model.put("feature", feature);
            String type = feature.getCvTerm().getName();
            if (type != null && type.equals("gene")) {
                viewName = "features/gene";
                Feature mRNA = null;
                Collection<FeatureRelationship> frs = feature.getFeatureRelationshipsForObjectId(); 
                for (FeatureRelationship fr : frs) {
                    mRNA = fr.getFeatureBySubjectId();
                    break;
                }
                Feature polypeptide = null;
                Collection<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForObjectId(); 
                for (FeatureRelationship fr : frs2) {
                    Feature f = fr.getFeatureBySubjectId();
                    if ("polypeptide".equals(f.getCvTerm().getName())) {
                        polypeptide = f;
                    }
                }
                model.put("polypeptide", polypeptide);
            }

        }

        return new ModelAndView(viewName, model);
    }

    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

	public void setFormInputView(String formInputView) {
		this.formInputView = formInputView;
	}

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}
