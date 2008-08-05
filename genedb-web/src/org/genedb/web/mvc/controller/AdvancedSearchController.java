package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.spring.web.servlet.view.JsonView;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class AdvancedSearchController extends PostOrGetFormController{

    private JsonView jsonView;
    private String resultsView;
    private String geneView;
    private LuceneDao luceneDao;
    private HistoryManagerFactory historyManagerFactory;

    private static final BooleanQuery geneOrPseudogeneQuery = new BooleanQuery();
    static {
        geneOrPseudogeneQuery.add(new TermQuery(new Term("cvTerm.name","gene")), Occur.SHOULD);
        geneOrPseudogeneQuery.add(new TermQuery(new Term("cvTerm.name","pseudogene")), Occur.SHOULD);
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        AdvanceSearchBean aSearch = (AdvanceSearchBean)command;
        Map<String,Object> model = new HashMap<String,Object>();
        String organism = aSearch.getOrganism();
        String term = aSearch.getTerm();
        String category = aSearch.getCategory().toString();
        String viewName = null;

        if(aSearch.isJson()) {
            Collection<String> orgNames = TaxonUtils.getOrgNames(organism);
            Hits hits = lookupInLucene(term,category,orgNames);
            HistoryManager historyManager = historyManagerFactory.getHistoryManager(
                    request.getSession());

            String args = String.format("organism:%s %s:%s ", organism, category,
                               term);

            String internalName = String.format("Organism:%s;;Category:%s;;Term:%s ", organism,
                                    category, term);

            List<String> ids = new ArrayList<String>();

            logger.info("found results -> " + hits.length());

            switch (hits.length()) {
            case 0: {
                logger.warn(String.format("Failed to find term '%s' in '%s'", term,category));
                be.reject("no.results");
                return showForm(request, response, be);
            }
            case 1: {
                model.put("name", hits.doc(0).get("uniqueName"));
                viewName = geneView;
                ids.add(hits.doc(0).get("uniqueName"));
                logger.info("found one result -> ");
                break;
            }
            default:
                List<ResultHit> results = new ArrayList<ResultHit>();
                for (int i = 0; i < hits.length(); i++) {
                    Document doc = hits.doc(i);
                    ResultHit rh = new ResultHit();
                    rh.setName(doc.get("uniqueName"));
                    rh.setType("gene");
                    rh.setProduct(doc.get("product"));
                    rh.setOrganism(doc.get("organism.commonName"));
                    results.add(rh);

                    ids.add(doc.get("uniqueName"));
                }
                model.put("features", results);
                return new ModelAndView(jsonView,model);
            }

            HistoryItem historyItem = historyManager.addHistoryItem(String.format("Browse Term -%s", args),
                    HistoryType.QUERY, ids);

            historyItem.setInternalName(internalName);

            logger.info("added history item " + internalName);

        } else {
            model.put("organism", organism);
            model.put("term", term);
            model.put("category", category);
            model.put("controller", "AdvanceSearch");
            viewName = resultsView;
        }

        return new ModelAndView(viewName,model);
    }

    private Hits lookupInLucene(String term, String category,
            Collection<String> orgNames) throws IOException {

        IndexReader ir = luceneDao.openIndex("org.gmod.schema.mapped.Feature");

        BooleanQuery advQuery = new BooleanQuery();
        if(term.indexOf("*") == -1) {
            advQuery.add(new TermQuery(new Term(category,term.toLowerCase())),Occur.SHOULD);
        } else {
            advQuery.add(new WildcardQuery(new Term(category,term.toLowerCase())),Occur.SHOULD);
        }

        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(new BooleanClause(geneOrPseudogeneQuery, Occur.MUST));
        booleanQuery.add(new BooleanClause(advQuery, Occur.MUST));

        Iterator<String> iterator = orgNames.iterator();
        BooleanQuery organismQuery = new BooleanQuery();

        while(iterator.hasNext()) {
            String organism = iterator.next();
            organismQuery.add(new TermQuery(new Term("organism.commonName",organism)), Occur.SHOULD);
        }

        booleanQuery.add(new BooleanClause(organismQuery,Occur.MUST));

        logger.debug(String.format("Lucene query is '%s'", booleanQuery.toString()));
        Hits hits = luceneDao.search(ir, booleanQuery);
        return hits;
    }

    public void setJsonView(JsonView jsonView) {
        this.jsonView = jsonView;
    }

    public void setResultsView(String resultsView) {
        this.resultsView = resultsView;
    }

    public void setGeneView(String geneView) {
        this.geneView = geneView;
    }

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }

    public static class AdvanceSearchBean {

        private String term;
        private String organism;
        private AdvancedSearchCategory category;
        private boolean json = false;

        public String getTerm() {
            return term;
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
        public AdvancedSearchCategory getCategory() {
            return category;
        }
        public void setCategory(AdvancedSearchCategory category) {
            this.category = category;
        }
        public boolean isJson() {
            return json;
        }
        public void setJson(boolean json) {
            this.json = json;
        }
    }
}
