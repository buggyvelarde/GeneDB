package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class SearchAllController extends TaxonNodeBindingFormController {

    private String listResultsView;

    private LuceneDao luceneDao;

    public LuceneDao getLuceneDao() {
        return luceneDao;
    }

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
    }

    public String getListResultsView() {
        return listResultsView;
    }

    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        SearchAllBean sab = (SearchAllBean) command;
        String orgs = sab.getOrgs();
        String field = sab.getField();
        String input = sab.getQuery();
        Map<String, Object> model = new HashMap<String, Object>(2);
        List<SearchAllHits> results = new ArrayList<SearchAllHits>();
        String viewName = listResultsView;

        IndexReader ir = luceneDao.openIndex("org.gmod.schema.sequence.FeatureCvTerm");
        String query = "";

        if (field.equals("ALL")) {
            query = "cvTerm.name:" + input + " AND feature.organism.commonName:" + orgs;
        } else {
            query = "cvTerm.name:" + input + " AND cvTerm.cv.name:" + field
                    + " AND feature.organism.commonName:" + orgs;
        }

        Hits hits = luceneDao.search(ir, new StandardAnalyzer(), "cvTerm.name", query);
        if (hits.length() == 0) {
            be.reject("No Result");
            return showForm(request, response, be);
            // return new ModelAndView(viewName,null);
        } else {
            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                SearchAllHits sah = new SearchAllHits();
                sah.setCvName(doc.get("cvTerm.cv.name"));
                sah.setCvTermName(doc.get("cvTerm.name"));
                sah.setFeatureName(doc.get("feature.uniqueName"));
                sah.setOrganismName(doc.get("feature.organism.commonName"));
                results.add(sah);
            }
            model.put("results", results);
            if (sab.isHistory()) {
                List<String> ids = new ArrayList<String>(results.size());
                for (SearchAllHits feature : results) {
                    ids.add(feature.getFeatureName());
                }
                HistoryManager historyManager = getHistoryManagerFactory().getHistoryManager(
                    request.getSession());
                historyManager.addHistoryItems("SearcAll '" + sab + "'", ids);
            }
            return new ModelAndView(viewName, model);
        }
    }
}

class SearchAllBean {

    private String query;
    private String orgs;
    private String field;
    private boolean history = false;

    public boolean isHistory() {
        return history;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrgs() {
        return orgs;
    }

    public void setOrgs(String orgs) {
        this.orgs = orgs;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
