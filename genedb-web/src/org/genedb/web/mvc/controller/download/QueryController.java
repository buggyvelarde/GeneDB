package org.genedb.web.mvc.controller.download;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.web.mvc.controller.WebConstants;

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
public class QueryController extends AbstractGeneDBFormController{

    private static final Logger logger = Logger.getLogger(QueryController.class);

    @Autowired
    private QueryFactory queryFactory;

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        return "redirect:/QueryList";
    }

    @RequestMapping(method = RequestMethod.GET , params= "q")
    public String processForm(
            @RequestParam(value="q") String queryName,
            @RequestParam(value="suppress", required=false) String suppress,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

        //Find query for request
        Query query = findQueryType(queryName, session);
        if (query==null){
            return "redirect:/QueryList";
        }

        //Initialise model data somehow
        model.addAttribute("query", query);
        logger.debug("The number of parameters is '" + request.getParameterMap().keySet().size() + "'");
        populateModelData(model, query);

        //Initialise query form
        Errors errors = initialiseQueryForm(query, request);
        if (errors.hasErrors()) {
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            logger.debug("Returning due to binding error");
            return "search/"+queryName;
        }

        //Validate initialised form
        query.validate(query, errors);
        if (errors.hasErrors()) {
            logger.debug("Validator found errors");
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            return "search/"+queryName;
        }

        logger.debug("Validator found no errors");
        List results = query.getResults();

        //Suppress item in results
        suppressResultItem(suppress, results);

        //Dispatch request to appropriate view
        return findDestinationView(queryName, query, model, results, session);
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
            String queryName, Query query, Model model, List results, HttpSession session){

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


    private void populateModelData(Model model, Query query) {
        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
    }

    protected Query findQueryType(String queryName, HttpSession session){
        if (!StringUtils.hasText(queryName)) {
               session.setAttribute(WebConstants.FLASH_MSG, "Unable to identify which query to use");
        }
        Query query = queryFactory.retrieveQuery(queryName);
        if (query == null) {
            session.setAttribute(WebConstants.FLASH_MSG, String.format("Unable to find query called '%s'", queryName));
        }
        return query;
    }

    /**
     * Remove an item in the result list
     * @param suppress
     * @param results
     */
    @SuppressWarnings("unchecked")
    protected void suppressResultItem(String suppress, List results){

        if (StringUtils.hasLength(suppress)) {
            int index = results.indexOf(suppress);
            if (index != -1) {
                results.remove(index);
            } else {
                logger.warn("Trying to remove '" + suppress + "' from results (as a result of an n-others call but it isn't present");
            }
        }
    }

}
