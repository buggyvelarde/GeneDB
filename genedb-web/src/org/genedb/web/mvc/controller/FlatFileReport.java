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
import org.genedb.db.loading.FeatureUtils;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.utils.PeptideProperties;

import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.text.DecimalFormat;
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
public class FlatFileReport extends SimpleFormController {

    private String listResultsView;
    private String formInputView;
    private SequenceDao sequenceDao;
    private OrganismDao organismDao;

  	
//	@Override
//    protected boolean isFormSubmission(HttpServletRequest request) {
//        return true;
//    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        FlatFileReportBean ffrb = (FlatFileReportBean) command;
        
        System.err.println("onSubmit has been called");
        
        String outputFormat = ffrb.outputFormat;
            
        if ("Artemis".equals(outputFormat)) {
            //Create command and send as text/plain
            // It'll reference this with EMBL output 
            return null;
        }
        
        if ("EMBL".equals(outputFormat)) {
            //Create subset and stream as text/plain
            return null;
        }
        
        if ("Table".equals(outputFormat)) {
            //Create subset, parse and forward to page
            Map<String, Object> model = new HashMap<String, Object>(3);
            String viewName = null;
            return new ModelAndView(viewName, model);
        }
        
        return null;

//        if(ffrb.getLookup() == "" || ffrb.getLookup() == null) {
//        	List <String> err = new ArrayList <String> ();
//        	logger.info("Look up is null");
//        	err.add("No search String found");
//        	err.add("please use the form below to search again");
//            Map model = new HashMap();
//        	model.put("status", err);
//        	//model.put("nameLookup", nl);
//        	String viewName = formInputView;
//        	return new ModelAndView(viewName,model);
//        }
//        logger.info("Look up is not null calling getFeaturesByAnyNameAndOrganism");
//        List<String> org = new ArrayList<String>();
//        if (nl.getOrglist() != null) {
//        	org.add(nl.getOrglist());
//        }
//        List<Feature> results = sequenceDao.getFeaturesByAnyNameAndOrganism(nl.getLookup(), org,"gene");
//        
//        if (results == null || results.size() == 0) {
//            logger.info("result is null");
//            // TODO Fail page
//        }
//        if (results.size() > 1) {
//            // Go to list results page
//        	//ResultBean rb = new ResultBean();
//        	//List<String> organisms = organismDao.findAllOrganismCommonNames();
//        	//nl.setOrganisms(organisms);
//        	viewName = listResultsView;
//            //model.put("nameLookup", nl);
//            model.put("results", results);
//        } else {
//            Feature feature = results.get(0);
//            model.put("feature", feature);
//            String type = feature.getCvTerm().getName();
//            if (type != null && type.equals("gene")) {
//                viewName = "features/gene";
//                Feature mRNA = null;
//                Collection<FeatureRelationship> frs = feature.getFeatureRelationshipsForObjectId(); 
//                for (FeatureRelationship fr : frs) {
//                    mRNA = fr.getFeatureBySubjectId();
//                    break;
//                }
//                Feature polypeptide = null;
//                Collection<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForObjectId(); 
//                for (FeatureRelationship fr : frs2) {
//                    Feature f = fr.getFeatureBySubjectId();
//                    if ("polypeptide".equals(f.getCvTerm().getName())) {
//                        polypeptide = f;
//                    }
//                }
//                model.put("polypeptide", polypeptide);
//            }
//
//        }
//
//        return new ModelAndView(viewName, model);
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

    class FlatFileReportBean {
        String organism;
        String outputFormat;
        int start;
        int end;
        
        
        
    }
    
  
