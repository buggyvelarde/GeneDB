package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.spring.web.servlet.view.JsonView;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.utils.GeneNameOrganism;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class GenesByCvTermAndCvController extends AbstractController {

    private static final String NO_VALUE_SUPPLIED = "_NO_VALUE_SUPPLIED";

    private SequenceDao sequenceDao;
    private LuceneDao luceneDao;
    private String listResultsView;
    private JsonView jsonView;
    private HistoryManagerFactory historyManagerFactory;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String cvName = ServletRequestUtils.getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);
        String viewName = listResultsView;
        String cvTermName = ServletRequestUtils.getStringParameter(request, "cvTermName", NO_VALUE_SUPPLIED);
        String organism = ServletRequestUtils.getStringParameter(request, "organism", null);
        String json = ServletRequestUtils.getStringParameter(request, "json", null);
        boolean isJson = Boolean.parseBoolean(json);

        Map<String, Object> model = new HashMap<String, Object>();

        if (isJson) {

            List<GeneNameOrganism> features = sequenceDao
                    .getGeneNameOrganismsByCvTermNameAndCvName(cvTermName, cvName, organism);

            if (features == null || features.size() == 0) {
                try {
                    ServletOutputStream out = response.getOutputStream();
                    out.print("There is no Gene in the database coresponding to CvTerm "
                            + cvTermName);
                    out.close();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            HistoryManager historyManager = getHistoryManagerFactory().getHistoryManager(
                request.getSession());

            String args = String.format("organism:%s GO %s:%s ", organism, cvName, cvTermName);

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
            historyManager.addHistoryItems(String.format("GenesByCvTermNameAndCv-%s", args),
                ids);
            model.put("features", features);
            return new ModelAndView(jsonView, model);
        } else {
            model.put("args", String.format("?organism=%s&cvTermName=%s&cvName=%s&json=True",
                organism, cvTermName, cvName));
        }
        return new ModelAndView(viewName, model);
    }

    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    public JsonView getJsonView() {
        return jsonView;
    }

    public void setJsonView(JsonView jsonView) {
        this.jsonView = jsonView;
    }

    public HistoryManagerFactory getHistoryManagerFactory() {
        return historyManagerFactory;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }

    public LuceneDao getLuceneDao() {
        return luceneDao;
    }

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}
