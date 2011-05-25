package org.genedb.web.mvc.controller;

//import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
//import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.TaxonQuery;
import org.genedb.util.MutableInteger;
//import org.genedb.web.mvc.controller.WebConstants;
//import org.genedb.web.mvc.controller.download.ResultEntry;
//import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
//import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
//import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;

//import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
//import com.sleepycat.collections.StoredMap;


@Controller
@RequestMapping("/ws")
public class WsQueryController implements ApplicationContextAware {

    private static final Logger logger = Logger.getLogger(WsQueryController.class);

    private ApplicationContext applicationContext;

    //@Autowired
    private QueryFactory queryFactory;

    //private ResultsCacheFactory resultsCacheFactory;

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    private static Map<String, String> maps = Maps.newLinkedHashMap();

    private PathMatcher pathMatcher = new AntPathMatcher();

    static {
        maps.put("proteinLengthQuery", "/from/{min}/to/{max}");
        maps.put("b", "/{min}/to/{max}");
        maps.put("c", "/{min}/{max}");
        maps.put("d", "/size/{min}/to/{max}");
        maps.put("e", "/size/{min}");
    }

    private Map<String, MutableInteger> numQueriesRun = Maps.newHashMap();


    @RequestMapping(method = RequestMethod.GET, value="/**")
    public String process(
            HttpServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

        String path = request.getPathInfo();
        logger.error(path);

        String queryName = lookupQuery(path);

        if (queryName == null) {
            // 404
            return null;
        }

        Map<String, String> params = pathMatcher.extractUriTemplateVariables(maps.get(queryName), path);

        logger.error("Map is "+params);


        if (params == null) {
            // ? 500
            return null;
        }

        //Query query = queryFactory.retrieveQuery(queryName);
        Query query = applicationContext.getBean(queryName, Query.class);
        logger.error(String.format("The '%s' has generated '%s'", queryName, query));

        BindingResult errors = populateQuery(query, params);
        logger.error("Errors are "+errors);

        //Validate initialised form
        //query.validate(query, errors);
        if (errors.hasErrors()) {
            logger.error("Validator found errors");
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            return "search/"+queryName;
        }

        logger.debug("Validator found no errors");
        @SuppressWarnings("unchecked") List<Object> results = query.getResults();

        logger.error(String.format("The number of results is '%d'", results.size()));

        //Dispatch request to appropriate view
        return findDestinationView(queryName, query, model, results, session);
    }


    private BindingResult populateQuery(Query query, Map<String, String> params) {
        DataBinder db = new DataBinder(query);
        MutablePropertyValues mpvs = new MutablePropertyValues(params);
        db.bind(mpvs);
        return db.getBindingResult();
    }



    private String lookupQuery(String path) {

        List<String> matches = Lists.newArrayList();

        for (Map.Entry<String, String> entry : maps.entrySet()) {
            String template = entry.getValue();
            if (pathMatcher.matchStart(template, path)) {
                matches.add(entry.getKey());
                logger.error(String.format("Match '%s' fits", (Object) entry.getValue()));
            }

        }

        if (matches.size() > 1) {
            logger.error("Ambiguous match");
            return null;
        }

        if (matches.size() == 0) {
            logger.error("No match");
            return null;
        }

        return matches.get(0);
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

        switch (results.size()) {
        case 0:
            logger.debug("No results found for query");
            model.addAttribute("taxonNodeName", taxonName);
            return "search/"+queryName;

        case 1:
            List<GeneSummary> gs = possiblyConvertList(results);
            return "redirect:/gene/" + gs.get(0).getSystematicId();

        default:
            List<GeneSummary> gs2 = possiblyConvertList(results);
            model.addAttribute("results", results);
            //model.addAttribute("taxonNodeName", taxonName);
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

    protected String findTaxonName(Query query){
        String taxonName = null;
        if (query instanceof TaxonQuery) {
            TaxonNodeList nodes = ((TaxonQuery) query).getTaxons();
            if (nodes != null && nodes.getNodeCount() > 0) {
                taxonName = nodes.getNodes().get(0).getLabel();
            } // FIXME
        }
        return taxonName;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
