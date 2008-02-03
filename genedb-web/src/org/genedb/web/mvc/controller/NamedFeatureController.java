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
import org.genedb.web.utils.Grep;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.utils.PeptideProperties;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private Grep grep;
    private LuceneDao luceneDao;
    
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
    	
    	NameLookupBean nlb = (NameLookupBean) command;
    	String orgs = nlb.getOrgs();
    	String name = nlb.getName();
    	String type = nlb.getFeatureType();
    	Map<String,Object> model = new HashMap<String,Object>(2);
    	String viewName = listResultsView;
    	List<ResultHit> results = new ArrayList<ResultHit>();
    	File tmpDir = new File(getServletContext().getRealPath("/index/hibernate/search/indexes/org.gmod.schema.sequence.Feature/"));
    	IndexReader ir = luceneDao.openIndex(tmpDir.getAbsolutePath());
    	List<String> fields = new ArrayList<String>();
    	String query = null;
    	if(orgs == null) {
    		fields.add("uniqueName");
    		fields.add("cvTerm.name");
    		query = "uniqueName:" + name + " AND cvTerm.name:gene";
    		Hits hits = luceneDao.search(ir, new StandardAnalyzer(), fields, query);
    		switch (hits.length()) {
    		case 0:
    			be.reject("No Result");
    			return showForm(request, response, be);
    			//return new ModelAndView(viewName,null);
    		case 1:
    			prepareGene(hits.doc(0).get("uniqueName"), model);
    			viewName = getSuccessView();
    		default:
    			for (int i=0;i<hits.length();i++) {
    				Document doc = hits.doc(i);
    				ResultHit rh = new ResultHit();
    				rh.setName(doc.get("uniqueName"));
    				rh.setType("gene");
    				rh.setOrganism(doc.get("organism.commonName"));
    				results.add(rh);
    			}
    			viewName = listResultsView;
    			model.put("results", results);
    		}
    		
    		if(nlb.isHistory()) {
    			List<String> ids = new ArrayList<String>(results.size());
    			for (ResultHit feature : results) {
    				ids.add(feature.getName());
    			}
    			HistoryManager historyManager = getHistoryManagerFactory().getHistoryManager(request.getSession());
    			historyManager.addHistoryItems("name lookup '"+nlb+"'", ids);
    		}
    	}
    	return new ModelAndView(viewName,model);
    }
    
    private void prepareGene(String systematicId, Map<String, Object> model) throws IOException {
    	String type = "gene";
		Feature gene = sequenceDao.getFeatureByUniqueName(systematicId, type);
        grep.compile("ID=" + systematicId);
        List<String> out = grep.grep();
        model.put("modified", out);
        model.put("feature", gene);

        Feature mRNA = null;
        Collection<FeatureRelationship> frs = gene.getFeatureRelationshipsForObjectId(); 
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
        		model.put("transcript", mRNA);
        		model.put("polypeptide", polypeptide);
                PeptideProperties pp = calculatePepstats(polypeptide);
    			model.put("polyprop", pp);
        	}
        }
    }
    

    private PeptideProperties calculatePepstats(Feature polypeptide) {

        //String seqString = FeatureUtils.getResidues(polypeptide);
        String seqString = new String(polypeptide.getResidues());
    	//System.err.println(seqString);
        Alphabet protein = ProteinTools.getAlphabet();
        SymbolTokenization proteinToke = null;
        SymbolList seq = null;
        PeptideProperties pp = new PeptideProperties();
        try {
        	proteinToke = protein.getTokenization("token");
        	seq = new SimpleSymbolList(proteinToke, seqString);
        } catch (BioException e) {
        	logger.error("Can't translate into a protein sequence");
        	//pp.setWarning("Unable to translate protein"); // FIXME
        	return pp;
        }
        IsoelectricPointCalc ipc = new IsoelectricPointCalc();
        Double cal = 0.0;
        try {
        	cal = ipc.getPI(seq, false, false);
        } catch (IllegalAlphabetException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        } catch (BioException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        DecimalFormat df = new DecimalFormat("#.##");
        pp.setIsoelectricPoint(df.format(cal));
        pp.setAminoAcids(Integer.toString(seqString.length()));
        MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS,false);
        try {
        	cal = mc.getMass(seq)/1000;
        } catch (IllegalSymbolException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        pp.setMass(df.format(cal));
        
        //cal = WebUtils.getCharge(seq);
        pp.setCharge(df.format(cal));
        return pp;
    }
    
	/*
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
    	
    	int start = Integer.parseInt(request.getParameter("start"));
		int limit = Integer.parseInt(request.getParameter("limit"));
		
		if(start == 0) {
			NameLookupBean nlb = (NameLookupBean) command;
			String orgs = nlb.getOrgs();
			List<Feature> results = null;
	        String orgNames = TaxonUtils.getOrgNamesInHqlFormat(orgs);
	        if (!nlb.isUseProduct()) {
	        	results = sequenceDao.getFeaturesByAnyNameAndOrganism(
	        			nlb.getName(), orgNames.toString(), nlb.getFeatureType());
	        } else {
	        	results = sequenceDao.getFeaturesByAnyNameOrProductAndOrganism(
	        			nlb.getName(), orgNames, nlb.getFeatureType());
	        }
	        
	        if (results == null) {
	        	
	        } else {
	        	request.getSession().setAttribute("results", results);
	        	JSONArray array = new JSONArray();
	        	
	    		for (int i=0;i<limit;i++) {
	    			JSONObject obj = new JSONObject();
	    			obj.put("organism", results.get(i).getOrganism().getCommonName());
	    			obj.put("name", results.get(i).getUniqueName());
	    			obj.put("type", results.get(i).getCvTerm().getName());
	    			array.add(obj);
	    		}
	    		JSONObject obj = new JSONObject();
	        	obj.put("total", results.size());
	        	obj.put("results", array);
	        	PrintWriter out = response.getWriter();
	        	out.print(obj);
	        	out.close();
	        }
 		} else {
 			List<Feature> results = (List<Feature>) request.getSession().getAttribute("results");
 			JSONArray array = new JSONArray();
 			int end = limit + start;
 			if (end > results.size())
 				end = results.size();
    		for (int i=start;i<end;i++) {
    			JSONObject obj = new JSONObject();
    			obj.put("organism", results.get(i).getOrganism().getCommonName());
    			obj.put("name", results.get(i).getUniqueName());
    			obj.put("type", results.get(i).getCvTerm().getName());
    			array.add(obj);
    		}
    		JSONObject obj = new JSONObject();
        	obj.put("total", results.size());
        	obj.put("results", array);
        	PrintWriter out = response.getWriter();
        	out.print(obj);
        	out.close();
 		}
		
    	return null;
    }
    
    
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
    	
        NameLookupBean nlb = (NameLookupBean) command;
        
        String orgs = nlb.getOrgs();
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        List<Feature> results = null;
        if(orgs == null) {

        }
        
        logger.info("Look up is not null calling getFeaturesByAnyNameAndOrganism");
        //logger.info("TaxonNode[0] is '"+taxonNode[0]+"'");
        //List<String> orgNames = taxonNode[0].getAllChildrenNames(); // FIXME 
        
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
            List<String> out = null;
            String pattern = "ID=" + feature.getUniqueName();
            grep.compile(pattern);
            out = grep.grep();
            model.put("modified", out);
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
    }*/

	public void setLuceneDao(LuceneDao luceneDao) {
		this.luceneDao = luceneDao;
	}

	public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    /*public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }*/

	public void setGrep(Grep grep) {
		this.grep = grep;
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

