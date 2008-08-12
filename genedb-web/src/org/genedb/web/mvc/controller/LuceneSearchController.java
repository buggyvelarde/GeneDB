package org.genedb.web.mvc.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class LuceneSearchController extends TaxonNodeBindingFormController {

    private String listResultsView;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command,
            BindException be) throws Exception {

        LuceneSearch luceneSearch = (LuceneSearch) command;
        String queryString = null;
        queryString = luceneSearch.getQuery();

        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;

        IndexReader ir = IndexReader.open("/Users/cp2/external/lucene/index/gff/");
        Collection<String> c = ir.getFieldNames(IndexReader.FieldOption.INDEXED);
        List<String> fields = new ArrayList<String>();
        for (String object : c) {
            fields.add(object);
        }

        if (queryString.equals("")){
            be.reject("no.results");
            return showForm(request,response,be);
        }

        Query query = null;

        IndexSearcher searcher = null;
        Hits hits = null;

        searcher = new IndexSearcher(ir);
        /* we had the StopAnalyzer prior to Standard, but it didnt work properly
         * we had to put wildcard like '*' to make some queries work
         */
        Analyzer analyzer = new StandardAnalyzer();
        String field = luceneSearch.getField();
        String searchFields[] = new String[fields.size()];
        QueryParser qp = null;
        if ("ALL".equals(field)){
            for(int i=0; i<fields.size();i++){
                searchFields[i] = fields.get(i);
            }
            qp = new MultiFieldQueryParser(searchFields,analyzer);
        } else {
            qp = new QueryParser(field,analyzer);
        }
        String searchString = luceneSearch.getQuery();
        if(searchString.matches("\\d+") && searchString.length() < 11) {
            StringBuffer s = new StringBuffer();
            int length = 11 - searchString.length();
            for(int i=0;i<length;i++){
                s.append("0");
            }
            s.append(searchString);
            searchString = s.toString();
        }
        query = qp.parse(searchString);

        hits = searcher.search(query);
        if (hits.length() == 0) {
            be.reject("no.results");
            return showForm(request, response, be);
        }

        if(luceneSearch.isHistory()) {
            List<String> ids = new ArrayList<String>(hits.length());
            for (int i=0;i<hits.length();i++) {
                Document doc = hits.doc(i);
                ids.add(doc.get("ID"));
            }
            HistoryManager historyManager = getHistoryManagerFactory().getHistoryManager(request.getSession());
            historyManager.addHistoryItem("lucene search '"+luceneSearch+"'", HistoryType.QUERY,ids);

            return new ModelAndView("redirect:/History/View",null);
        }
        List<SearchHit> results = new ArrayList<SearchHit>();

        for (int i=0;i<hits.length();i++) {
            Document doc = hits.doc(i);
            SearchHit sh = new SearchHit();
            sh.setTitle(doc.get("ID"));
            sh.setUrl(doc.get("url"));
            sh.setChr(doc.get("chr"));
            sh.setStart(doc.get("start"));
            sh.setStop(doc.get("stop"));
            sh.setStrand(doc.get("strand"));
            results.add(sh);
        }


        model.put("results", results);
        viewName = listResultsView;
        return new ModelAndView(viewName,model);
    }


    public String getListResultsView() {
        return listResultsView;
    }
    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

}
