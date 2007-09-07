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
import org.genedb.db.loading.TaxonNodeManager;

import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
        
        String orgs = nlb.getOrgs();
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        
        logger.info("Look up is not null calling getFeaturesByAnyNameAndOrganism");
        //logger.info("TaxonNode[0] is '"+taxonNode[0]+"'");
        //List<String> orgNames = taxonNode[0].getAllChildrenNames(); // FIXME 
        List<Feature> results = null;
        String orgNames = TaxonUtils.getOrgNamesInHqlFormat(orgs);
        if (!nlb.isUseProduct()) {
        	results = sequenceDao.getFeaturesByAnyNameAndOrganism(
        			nlb.getName(), orgNames.toString(), nlb.getFeatureType());
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
        	if (nlb.isHistory()) {
        		List<String> ids = new ArrayList<String>(results.size());
        		for (Feature feature : results) {
					ids.add(feature.getUniqueName());
				}
        		HistoryManager historyManager = getHistoryManagerFactory().getHistoryManager(request.getSession());
        		historyManager.addHistoryItems("name lookup '"+nlb+"'", ids);
        		logger.info("Trying to save history item and redirect");
        		viewName = "redirect:/History/View";
        	} else {
        		//model.put("nameLookup", nl);
        		model.put("results", results);
        	}
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
    private boolean addWildcard = false;
    private String featureType = "gene";
    private boolean useProduct = false;
    private boolean history = false;
    private String orgs;
    
	public String getOrgs() {
		return orgs;
	}

	public void setOrgs(String orgs) {
		this.orgs = orgs;
	}

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

	public boolean isHistory() {
		return history;
	}

	public void setHistory(boolean history) {
		this.history = history;
	}
    
	@Override
    public String toString() {
		return getName()+","+getOrgs();
	}
    
}
