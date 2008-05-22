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

import org.apache.log4j.Logger;
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
	
	private static final Logger logger = Logger.getLogger(NamedFeatureController.class);
	
    private String listResultsView;
    private SequenceDao sequenceDao;
    private LuceneDao luceneDao;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        NameLookupBean nlb = (NameLookupBean) command;
        String orgs = nlb.getOrgs();
        String name = nlb.getName();
        String type = nlb.getFeatureType();
        Map<String, Object> model = new HashMap<String, Object>(2);
        String viewName = listResultsView;
        List<ResultHit> results = new ArrayList<ResultHit>();

        IndexReader ir = luceneDao.openIndex("org.gmod.schema.sequence.Feature");
        String query = null;
        if (orgs == null) {
            query = "uniqueName:" + name + " AND cvTerm.name:gene";
            Hits hits = luceneDao.search(ir, new StandardAnalyzer(), "uniqueName", query);
            switch (hits.length()) {
            case 0:
                // Temporary check as the Lucene db isn't automatically
                // up-to-date
                if (directDbCheck(name)) {
                    model = GeneDBWebUtils.prepareGene(name, model);
                    viewName = "features/gene";
                    break;
                }
                be.reject("No Result");
                return showForm(request, response, be);
            case 1:
            	model = GeneDBWebUtils.prepareGene(hits.doc(0).get("uniqueName"), model);
                viewName = "features/gene";
                break;
            default:
                for (int i = 0; i < hits.length(); i++) {
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

            if (nlb.isHistory()) {
                List<String> ids = new ArrayList<String>(results.size());
                for (ResultHit feature : results) {
                    ids.add(feature.getName());
                }
                HistoryManager historyManager = getHistoryManagerFactory().getHistoryManager(
                        request.getSession());
                historyManager.addHistoryItems("name lookup '" + nlb + "'", ids);
            }
        }
        return new ModelAndView(viewName, model);
    }

    /**
     * Look up a featurename directly in the database, as the Lucene indices
     * aren't automatically up-to-date
     * 
     * @param name
     *                the uniquename of the gene
     * @return whether it exists in the db
     */
    private boolean directDbCheck(String name) {
        Feature f = sequenceDao.getFeatureByUniqueName(name, "gene");
        if (f != null) {
            return true;
        }
        return false;
    }

    

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
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
            if (!(ret.charAt(0) == '*')) {
                ret.insert(0, '*');
            }
            if (!(ret.charAt(ret.length() - 1) == '*')) {
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
        return getName() + "," + getOrgs();
    }

}
