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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.spring.web.servlet.view.JsonView;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;

import org.gmod.schema.utils.GeneNameOrganism;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Looks up a feature by uniquename, and possibly synonyms
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class BrowseTermController extends PostOrGetFormController {

    private static final Logger logger = Logger.getLogger(BrowseTermController.class);
    private SequenceDao sequenceDao;
    private LuceneDao luceneDao;
    private String geneView;
    private JsonView jsonView;
    private HistoryManagerFactory historyManagerFactory;

    @Override
    protected Map<?,?> referenceData(@SuppressWarnings("unused") HttpServletRequest request) throws Exception {
        Map<String,Object> reference = new HashMap<String,Object>();
        reference.put("categories", BrowseCategory.values());
        return reference;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        BrowseTermBean btb = (BrowseTermBean) command;
        Map<String, Object> model = new HashMap<String, Object>();

        if(!btb.isJson()) {
            model.put("category", btb.getCategory().toString());
            model.put("term",btb.getTerm());
            model.put("organism", btb.getOrganism());
            model.put("controller", "BrowseTerm");
            return new ModelAndView(getSuccessView(),model);
        }

        String orgNames = TaxonUtils.getOrgNamesInHqlFormat(btb.getOrganism());

        String category = btb.getCategory().toString();

        /* This is to include all the cvs starting with CC.
         * In future when the other cvs have more terms in,
         * this can be removed and the other cvs starting
         * with CC can be added to BrowseCategory
         */
        List<GeneNameOrganism> features;

        if(category.equals("ControlledCuration")) {
            features = sequenceDao
                .getGeneNameOrganismsByCvTermNameAndCvNamePattern(btb.getTerm(), "CC\\_%", orgNames);
        } else {
            features = sequenceDao
                .getGeneNameOrganismsByCvTermNameAndCvName(btb.getTerm(), category, orgNames);
        }

        if (features == null || features.size() == 0) {
            logger.info("result is null");
            be.reject("no.results");
            return showForm(request, response, be);
        }

        if (features.size() == 1) {
            model.put("name", features.get(0).getGeneName());
            return new ModelAndView(geneView, model);
        }

        HistoryManager historyManager = historyManagerFactory.getHistoryManager(
                request.getSession());

        String args = String.format("organism:%s %s:%s ", btb.getOrganism(), btb.getCategory(),
                           btb.getTerm());

        String internalName = String.format("Organism:%s;;Category:%s;;Term:%s ", btb.getOrganism(),
                                btb.getCategory(), btb.getTerm());

        List<String> ids = new ArrayList<String>();

        IndexReader ir = luceneDao.openIndex("org.gmod.schema.mapped.Feature");

        for (GeneNameOrganism feature : features) {
            ids.add(feature.getGeneName());

            TermQuery query = new TermQuery(new Term("uniqueName", feature.getGeneName()));
            Hits hits = luceneDao.search(ir, query);

            if (hits.length() > 0) {
                feature.setProduct(hits.doc(0).get("product"));
            }
        }

        HistoryItem historyItem = historyManager.addHistoryItem(String.format("Browse Term -%s", args),
            HistoryType.QUERY, ids);

        if(historyItem != null) {
            historyItem.setInternalName(internalName);
        }

        model.put("features", features);

        return new ModelAndView(jsonView, model);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public String getGeneView() {
        return geneView;
    }

    public void setGeneView(String geneView) {
        this.geneView = geneView;
    }

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
    }

    public void setJsonView(JsonView jsonView) {
        this.jsonView = jsonView;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }
}

class BrowseTermBean {

    private BrowseCategory category;
    private String term;
    private String organism;
    private boolean json = false;

    public boolean isJson() {
        return json;
    }

    public void setJson(boolean json) {
        this.json = json;
    }

    public BrowseCategory getCategory() {
        return this.category;
    }

    public void setCategory(BrowseCategory category) {
        this.category = category;
    }

    public String getTerm() {
        return this.term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

}
