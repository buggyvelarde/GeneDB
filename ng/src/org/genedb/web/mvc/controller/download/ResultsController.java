package org.genedb.web.mvc.controller.download;

import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.IdsToGeneSummaryQuery;
import org.genedb.web.mvc.controller.WebConstants;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/Results")
public class ResultsController {

    private static final Logger logger = Logger.getLogger(ResultsController.class);

    private static final String IDS_TO_GENE_SUMMARY_QUERY = "idsToGeneSummary";

    public static final int DEFAULT_LENGTH = 30;

    public static final int ID_TO_GENE_SUMMARY_EXPANSION_BATCH = 10;

    //@Autowired
    private ResultsCacheFactory resultsCacheFactory;

    public void setResultsCacheFactory(ResultsCacheFactory resultsCacheFactory) {
        this.resultsCacheFactory = resultsCacheFactory;
    }

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    //@Autowired
    private QueryFactory queryFactory;

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        return "redirect:/QueryList";
    }

    @RequestMapping(method = {RequestMethod.GET} , value="/{key}")
    public String setUpForm(
            @PathVariable(value="key") String key,
            @RequestParam(value="taxonNodeName", required=false) String taxonNodeName,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {
        // TODO Do we want form submission via GET?

        if (!StringUtils.hasText(key)) {
            session.setAttribute(WebConstants.FLASH_MSG, "Unable to identify which query to use");
            return "redirect:/QueryList";
        }

        String pName = new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_PAGE);
        logger.debug("pName is '"+pName+"'");
        String startString = request.getParameter((new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_PAGE)));
        logger.debug("The start string is '"+startString+"'");

        // Use 1-based index for start and end
        int start = 1;
        if (startString != null) {
            start = (Integer.parseInt(startString) - 1) * DEFAULT_LENGTH + 1;
        }

        int end = start + DEFAULT_LENGTH;

        if (!resultsCacheFactory.getResultsCacheMap().containsKey(key)) {
            session.setAttribute(WebConstants.FLASH_MSG, "Unable to retrieve results for this key");
            logger.error("Unable to retrieve results for key '"+key+"'");
            return "redirect:/QueryList";
        }

        ResultEntry resultEntry = resultsCacheFactory.getResultsCacheMap().get(key);
        List<GeneSummary> results = resultEntry.results;


        logger.debug("The number of results retrieved from cache is '"+results.size()+"'");
        logger.debug("The end marker, before adjustment, is '"+end+"'");

        if (end > results.size() + 1) {
            end = results.size() + 1;
        }

        List<GeneSummary> possiblyExpanded = null;
        if (!resultEntry.expanded){
            possiblyExpanded = possiblyExpandResults(results);
            resultEntry.expanded = true;
        }

        if (possiblyExpanded == null) {
            possiblyExpanded = results;
            logger.debug("The subset is already expanded");
        } else {
            // Need to update cache
            logger.debug("We've expanded the systematic ids");
            resultEntry.results = possiblyExpanded;
            resultsCacheFactory.getResultsCacheMap().put(key, resultEntry);
            logger.debug("And stored the set back");
        }

        model.addAttribute("results", possiblyExpanded);
        model.addAttribute("resultsSize", results.size());
        model.addAttribute("key", key);
        model.addAttribute("firstResultIndex", start);
        if (resultEntry.query != null) {
            model.addAttribute("isMaxResultsReached", Boolean.valueOf(resultEntry.query.isMaxResultsReached()));
        }
        if (taxonNodeName != null) {
            model.addAttribute("taxonNodeName", taxonNodeName);
        }
        if (resultEntry.taxonGroup != null) {
            model.addAttribute("taxonGroup", resultEntry.taxonGroup);
        }

        if (resultEntry.query != null) {
            model.addAttribute("query", resultEntry.query);
            populateModelData(model, resultEntry.query);
            return "search/"+resultEntry.queryName;
        }
        return "list/results2";
    }

    /**
     * Expand the current resultset, i.e. to initialise more fields in the list of GeneSummary instances
     * @param results The un-expanded resultset
     * @return expanded The expanded resultset
     * @throws QueryException
     */
    private List<GeneSummary> possiblyExpandResults(List<GeneSummary> results) throws QueryException {
        List<String> subset = new ArrayList<String>();
        List<GeneSummary> expanded = new ArrayList<GeneSummary>();

        for(int i=0; i<results.size(); ++i){

            subset.add(results.get(i).getSystematicId());
            if (i % ID_TO_GENE_SUMMARY_EXPANSION_BATCH == 0
                    || i+1 == results.size()){
                //expand current batch
                List<GeneSummary> converts = convertIdsToGeneSummaries(subset);
                expanded.addAll(converts);
                subset.clear();
            }
        }

        //Sort overall result
        Collections.sort(expanded);
        return expanded;
    }

    private List<GeneSummary> convertIdsToGeneSummaries(List<String> ids) throws QueryException {
        IdsToGeneSummaryQuery idsToGeneSummary =
            (IdsToGeneSummaryQuery) queryFactory.retrieveQuery(IDS_TO_GENE_SUMMARY_QUERY, NumericQueryVisibility.PRIVATE);

        if (idsToGeneSummary == null) {
            throw new RuntimeException("Internal error - unable to find ids to gene summary query");
        }
        idsToGeneSummary.setIds(ids);
        return (List<GeneSummary>)idsToGeneSummary.getResults();
    }

    private void populateModelData(Model model, Query query) {
        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
    }

}
