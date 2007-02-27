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
import org.genedb.db.dao.PhylogenyDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.controller.NameLookup;
import org.genedb.db.loading.FeatureUtils;

import org.gmod.schema.phylogeny.Phylonode;
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
public class NameFeatureController extends SimpleFormController {

    private String listResultsView;
    private String formInputView;
    private SequenceDao sequenceDao;
    private OrganismDao organismDao;
    private PhylogenyDao phylogenyDao;
  	
	@Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        NameLookup nl = (NameLookup) command;
        String queryString = nl.getLookup();
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        
        List<Phylonode> nodes = this.phylogenyDao.getAllPhylonodes();
        List<String> organisms = new ArrayList<String>();
        for (Phylonode node : nodes) {
			organisms.add(node.getLabel());
		}
        if (queryString == "Please enter search text ..."){
			List <String> err = new ArrayList <String> ();
			err.add("No search String found");
        	err.add("please use the form below to search again");
        	model.put("status", err);
        	model.put("organisms", organisms);
        	model.put("nameLookup", nl);
        	viewName = formInputView;
        	return new ModelAndView(viewName,model);
		} else if (queryString == null){
			viewName = formInputView;
			model.put("organisms", organisms);
			model.put("nameLookup", nl);
        	return new ModelAndView(viewName,model);
		}
        
        logger.info("Look up is not null calling getFeaturesByAnyNameAndOrganism");
        List<String> org = new ArrayList<String>();
        String organism = nl.getOrganism();
        if (this.organismDao.getOrganismByCommonName(organism) != null){
        	org.add(organism);
        } else {
        	List<Phylonode> pNodes = this.phylogenyDao.getPhylonodesByParent(this.phylogenyDao.getPhylonodeByName(organism).get(0));
        	org = getOrganisms(pNodes,org);
        }
        List<Feature> results = sequenceDao.getFeaturesByAnyNameAndOrganism(nl.getLookup(), org,"gene");
        
        if (results == null || results.size() == 0) {
            logger.info("result is null");
            // TODO Fail page
        }
        if (results.size() > 1) {
            // Go to list results page
        	//ResultBean rb = new ResultBean();
        	//List<String> organisms = organismDao.findAllOrganismCommonNames();
        	//nl.setOrganisms(organisms);
        	viewName = listResultsView;
            //model.put("nameLookup", nl);
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
                model.put("transcript", mRNA);
                String seqString = FeatureUtils.getResidues(polypeptide);
                Alphabet protein = ProteinTools.getAlphabet();
        		SymbolTokenization proteinToke = null;
        		SymbolList seq = null;
        		PeptideProperties pp = new PeptideProperties();
        		try {
        			proteinToke = protein.getTokenization("token");
        			seq = new SimpleSymbolList(proteinToke, seqString);
        			System.out.println("symbol list is : " + seq);
        		} catch (BioException e) {
        			System.out.println("in exception : " );
        			e.printStackTrace();
        		}
    			IsoelectricPointCalc ipc = new IsoelectricPointCalc();
    			Double cal = ipc.getPI(seq, true, true);
    			DecimalFormat df = new DecimalFormat("#.##");
    			pp.setIsoelectricPoint(df.format(cal));
    			pp.setAminoAcids(Integer.toString(seqString.length()));
    			MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS,false);
    			cal = mc.getMass(seq) / 1000;
    			pp.setMass(df.format(cal));
    			
    			cal = WebUtils.getCharge(seq);
    			pp.setCharge(df.format(cal));
    			model.put("polyprop", pp);
            }

        }

        return new ModelAndView(viewName, model);
    }

    private List<String> getOrganisms(List<Phylonode> nodes, List<String> org) {
    	for (Phylonode phylonode : nodes) {
			if (this.organismDao.getOrganismByCommonName(phylonode.getLabel()) != null){
	        	org.add(phylonode.getLabel());
			} else {
				org = getOrganisms(this.phylogenyDao.getPhylonodesByParent(phylonode),org);
			}
		}
		return org;
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

	public PhylogenyDao getPhylogenyDao() {
		return phylogenyDao;
	}

	public void setPhylogenyDao(PhylogenyDao phylogenyDao) {
		this.phylogenyDao = phylogenyDao;
	}
}
