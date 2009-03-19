package org.genedb.web.mvc.controller.download;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;
import com.sleepycat.collections.StoredMap;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.TaxonQuery;
import org.genedb.web.mvc.controller.WebConstants;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/Query")
public class QueryController {

    private static final Logger logger = Logger.getLogger(QueryController.class);

    private static final String RESULTS_ATTR = "results";

    @Autowired
    private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;

    @Autowired
    private ResultsCacheFactory resultsCacheFactory;

    @Autowired
    private QueryFactory queryFactory;

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        return "redirect:/QueryList";
    }

    @RequestMapping(method = RequestMethod.GET , params= "q")
    public String setUpForm(
            @RequestParam(value="q") String queryName,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

        logger.error("This is the setupform method");

        if (!StringUtils.hasText(queryName)) {
            session.setAttribute(WebConstants.FLASH_MSG, "Unable to identify which query to use");
            return "redirect:/QueryList";
        }

        Query query = queryFactory.retrieveQuery(queryName);
        if (query == null) {
            session.setAttribute(WebConstants.FLASH_MSG, String.format("Unable to find query called '%s'", queryName));
            return "redirect:/QueryList";
        }
        model.addAttribute("query", query);

        populateModelData(model, query);
        return "search/"+queryName;
    }

    @RequestMapping(method = RequestMethod.POST , params= "q")
    public String processForm(
            @RequestParam(value="q") String queryName,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

        if (!StringUtils.hasText(queryName)) {
               session.setAttribute(WebConstants.FLASH_MSG, "Unable to identify which query to use");
            return "redirect:/QueryList";
        }

        Query query = queryFactory.retrieveQuery(queryName);
        if (query == null) {
            session.setAttribute(WebConstants.FLASH_MSG, String.format("Unable to find query called '%s'", queryName));
            return "redirect:/QueryList";
        }

        model.addAttribute("query", query);
        logger.debug("The number of parameters is '" + request.getParameterMap().keySet().size() + "'");
        populateModelData(model, query);


        // Attempt to fill in form
        ServletRequestDataBinder binder = new ServletRequestDataBinder(query);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), false, 10));
        binder.registerCustomEditor(TaxonNode[].class, taxonNodeArrayPropertyEditor);

        binder.bind(request);

        Errors errors = binder.getBindingResult();
        if (errors.hasErrors()) {
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            logger.error("Returning due to binding error");
            return "search/"+queryName;
        }

        query.validate(query, errors);

        if (errors.hasErrors()) {
            logger.debug("Validator found errors");
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            return "search/"+queryName;
        }

        logger.error("Validator found no errors");
        List results = query.getResults();
        
        String taxonName = null;
        if (query instanceof TaxonQuery) {
            TaxonNode[] nodes = ((TaxonQuery) query).getTaxons();
            if (nodes != null && nodes.length > 0) {
                taxonName = nodes[0].getLabel();
            } // FIXME
        }
        
        String resultsKey = null;
        if (results.size() > 0) {
//            Object firstItem =  results.get(0);
//            if (! (firstItem instanceof GeneSummary)) {
//                results = convertIdsToGeneSummaries(results);
//            }

        }

        switch (results.size()) {
        case 0:
            logger.error("No results found for query");
            model.addAttribute("taxonNodeName", taxonName);
            return "search/"+queryName;
        case 1:
        	List<GeneSummary> gs = possiblyConvertList(results);
            resultsKey = cacheResults(gs);
            return "redirect:/NamedFeature?name=" + gs.get(0).getSystematicId();
        default:
        	List<GeneSummary> gs2 = possiblyConvertList(results);
            resultsKey = cacheResults(gs2);
            model.addAttribute(RESULTS_ATTR, resultsKey);
            model.addAttribute("taxonNodeName", taxonName);
            logger.error("Found results for query - redirecting to ResultsCache controller");
            return "forward:/ResultsCache";
        }
    }


    private List<GeneSummary> possiblyConvertList(List results) {
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


    private String cacheResults(List<GeneSummary> gs) {
        String key = Integer.toString(System.identityHashCode(gs)); // CHECKME
        StoredMap<String, List> map = resultsCacheFactory.getResultsCacheMap();
        map.put(key, gs);
        return key;
    }


    private void populateModelData(Model model, Query query) {
        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
    }

}
