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


import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.TaxonNode;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
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
public class NamedFeatureController extends TaxonNodeBindingFormController {

    private String listResultsView;
    private SequenceDao sequenceDao;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
    	
        NameLookupBean nlb = (NameLookupBean) command;
        
        TaxonNode[] taxonNode = nlb.getOrganism();
        
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        
//        if (queryString == "Please enter search text ..."){
//			List <String> err = new ArrayList <String> ();
//			err.add("No search String found");
//        	err.add("please use the form below to search again");
//        	model.put("status", err);
//        	model.put("organisms", organisms);
//        	model.put("nameLookup", nl);
//        	viewName = getFormView();
//        	return new ModelAndView(viewName,model);
//		} else if (queryString == null){
//			viewName = getFormView();
//			model.put("organisms", organisms);
//			model.put("nameLookup", nl);
//        	return new ModelAndView(viewName,model);
//		}
        
        logger.info("Look up is not null calling getFeaturesByAnyNameAndOrganism");
        logger.info("TaxonNode[0] is '"+taxonNode[0]+"'");
        List<String> orgNames = taxonNode[0].getAllChildrenNames(); // FIXME 
        List<Feature> results = null;
        
        if (nlb.isUseProduct()) {
        	results = sequenceDao.getFeaturesByAnyNameAndOrganism(
        			nlb.getName(), orgNames, nlb.getFeatureType());
        } else {
        	results = sequenceDao.getFeaturesByAnyNameOrProductAndOrganism(
        			nlb.getName(), orgNames, nlb.getFeatureType());
        }
        	
        if (results == null || results.size() == 0) {
            logger.info("result is null");
            be.reject("no.results");
            return showForm(request, response, be);
        }
        
        if (results.size() > 1) {
            // Go to list results page
        	viewName = listResultsView;
            //model.put("nameLookup", nl);
            model.put("results", results);
        } else {
            Feature feature = results.get(0);
            model.put("feature", feature);
            String type = feature.getCvTerm().getName();
            if (type != null && type.equals("gene")) {
                viewName = "features/"+type; // TODO Check this is known else go to features/generic
                Feature mRNA = null;
                Collection<FeatureRelationship> frs = feature.getFeatureRelationshipsForObjectId(); 
                if (frs != null) {
                	for (FeatureRelationship fr : frs) {
                		mRNA = fr.getFeatureBySubjectId();
                		break;
                	}
                	if (mRNA != null) {
                		Feature polypeptide = null;
                		Collection<FeatureRelationship> frs2 = mRNA.getFeatureRelationshipsForObjectId(); 
                		for (FeatureRelationship fr : frs2) {
                			Feature f = fr.getFeatureBySubjectId();
                			if ("polypeptide".equals(f.getCvTerm().getName())) {
                				polypeptide = f;
                			}
                		}
                		model.put("polypeptide", polypeptide);
                		//                model.put("transcript", mRNA);
//                String seqString = FeatureUtils.getResidues(polypeptide);
//                Alphabet protein = ProteinTools.getAlphabet();
//        		SymbolTokenization proteinToke = null;
//        		SymbolList seq = null;
//        		PeptideProperties pp = new PeptideProperties();
//        		try {
//        			proteinToke = protein.getTokenization("token");
//        			seq = new SimpleSymbolList(proteinToke, seqString);
//        			System.out.println("symbol list is : " + seq);
//        		} catch (BioException e) {
//        			System.out.println("in exception : " );
//        			e.printStackTrace();
//        		}
//    			IsoelectricPointCalc ipc = new IsoelectricPointCalc();
//    			Double cal = ipc.getPI(seq, true, true);
//    			DecimalFormat df = new DecimalFormat("#.##");
//    			pp.setIsoelectricPoint(df.format(cal));
//    			pp.setAminoAcids(Integer.toString(seqString.length()));
//    			MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS,false);
//    			cal = mc.getMass(seq) / 1000;
//    			pp.setMass(df.format(cal));
//    			
//    			cal = WebUtils.getCharge(seq);
//    			pp.setCharge(df.format(cal));
//    			model.put("polyprop", pp);
                	}
                }
            }
        }

        return new ModelAndView(viewName, model);
    }

	public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

	
}


class NameLookupBean {
    
    private String name; // The name to lookup, using * for wildcards
    private TaxonNode[] organism;
    private boolean addWildcard = false;
    private String featureType = "gene";
    private boolean useProduct = false;
       
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
    	if (addWildcard) {
    		StringBuilder ret = new StringBuilder(this.name);
    		if (!(ret.charAt(0)=='*')) {
    			ret.insert(0, '*');
    		}
    		if (!(ret.charAt(ret.length()-1)=='*')) {
    			ret.append('*');
    		}
    		return ret.toString();
    	}
    	return this.name;
    }

	public TaxonNode[] getOrganism() {
		return organism;
	}

	public void setOrganism(TaxonNode[] organism) {
		this.organism = organism;
	}

	public String getFeatureType() {
		return featureType;
	}

	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	public void setAddWildcard(boolean addWildcard) {
		this.addWildcard = addWildcard;
	}

	public boolean isUseProduct() {
		return useProduct;
	}

	public void setUseProduct(boolean useProduct) {
		this.useProduct = useProduct;
	}
    
    
}
