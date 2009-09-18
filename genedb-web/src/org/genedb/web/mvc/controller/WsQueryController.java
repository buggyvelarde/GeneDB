package org.genedb.web.mvc.controller;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.TaxonQuery;
import org.genedb.util.MutableInteger;
import org.genedb.web.mvc.controller.WebConstants;
import org.genedb.web.mvc.controller.download.ResultEntry;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sleepycat.collections.StoredMap;


@Controller
@RequestMapping("/ws")
public class WsQueryController {

    private static final Logger logger = Logger.getLogger(WsQueryController.class);

    //@Autowired
    private QueryFactory queryFactory;

    private ResultsCacheFactory resultsCacheFactory;

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    private Map<String, MutableInteger> numQueriesRun = Maps.newHashMap();


    @RequestMapping(method = RequestMethod.GET)
    public String process(
            HttpServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

        Map.Entry<Class<Query>, String> entry = lookup(request);

        if (entry == null) {
            // 404
        }

        Map<String, String> params = extractParams(request, entry.getValue());

        if (params == null) {
            // ? 500
        }

        Query query = null;
        String queryName = null;
        populateModelData(model, query);

        //Initialise query form
        Errors errors = initialiseQueryForm(query, request);
        if (errors.hasErrors()) {
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            logger.debug("Returning due to binding error");
            return "search/"+queryName;
        }
        populateQuery(entry.getKey(), params);

        //Validate initialised form
        query.validate(query, errors);
        if (errors.hasErrors()) {
            logger.debug("Validator found errors");
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            return "search/"+queryName;
        }

        logger.debug("Validator found no errors");
        @SuppressWarnings("unchecked") List<Object> results = query.getResults();

        //Dispatch request to appropriate view
        return findDestinationView(queryName, query, model, results, session);
    }


    private Errors initialiseQueryForm(Query query, HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }


    private void populateModelData(Model model, Query query) {
        // TODO Auto-generated method stub

    }


    private void populateQuery(Class<Query> key, Map<String, String> params) {
        // TODO Auto-generated method stub

    }


    private Map<String, String> extractParams(HttpServletRequest request, String value) {
        // TODO Auto-generated method stub
        return null;
    }


    private Entry<Class<Query>, String> lookup(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Work out the correct view destination
     * @param queryName
     * @param query
     * @param taxonName
     * @param model
     * @param results
     * @param session
     * @return
     */
    private String findDestinationView(
            String queryName, Query query, Model model, List<Object> results, HttpSession session){

        //Get the current taxon name
        String taxonName = findTaxonName(query);
        String resultsKey = null;

        switch (results.size()) {
        case 0:
            logger.debug("No results found for query");
            model.addAttribute("taxonNodeName", taxonName);
            return "search/"+queryName;

        case 1:
            List<GeneSummary> gs = possiblyConvertList(results);
            resultsKey = cacheResults(gs, query, queryName, session.getId());
            return "redirect:/NamedFeature?name=" + gs.get(0).getSystematicId();

        default:
            List<GeneSummary> gs2 = possiblyConvertList(results);
            resultsKey = cacheResults(gs2, query, queryName, session.getId());
            model.addAttribute("key", resultsKey);
            model.addAttribute("taxonNodeName", taxonName);
            logger.debug("Found results for query (Size: '"+gs2.size()+"' key: '"+resultsKey+"')- redirecting to Results controller");
            return "redirect:/Results";
        }
    }

    @ManagedAttribute(description="The no. of times each query has been attempted to be run")
    public Map<String, MutableInteger> getNumQueriesRun() {
        return numQueriesRun;
    }

    protected List<GeneSummary> possiblyConvertList(List results) {
        List<GeneSummary> gs;
        Object firstItem =  results.get(0);
        if (firstItem instanceof GeneSummary) {
            gs = results;
        } else {
            gs = Lists.newArrayListWithExpectedSize(results.size());
            for (Object o  : results) {
                gs.add(new GeneSummary((String) o));
            }
        }
        return gs;
    }

    protected String cacheResults(List<GeneSummary> gs, Query q, String queryName, String sessionId) {
        String key = sessionId + ":"+ Integer.toString(System.identityHashCode(gs)); // CHECKME
        StoredMap<String, ResultEntry> map = resultsCacheFactory.getResultsCacheMap();
        ResultEntry re = new ResultEntry();
        re.numOfResults = gs.size();
        re.query = q;
        re.results = gs;
        re.queryName = queryName;
        if (q instanceof LuceneQuery){
            re.expanded = true;
        }
        map.put(key, re);
        return key;
    }


    protected String findTaxonName(Query query){
        String taxonName = null;
        if (query instanceof TaxonQuery) {
            TaxonNode[] nodes = ((TaxonQuery) query).getTaxons();
            if (nodes != null && nodes.length > 0) {
                taxonName = nodes[0].getLabel();
            } // FIXME
        }
        return taxonName;
    }

}
