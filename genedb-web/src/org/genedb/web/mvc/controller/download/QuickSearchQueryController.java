package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.QuickSearchQuery;
import org.genedb.querying.tmpquery.QuickSearchQuery.QuickSearchQueryResults;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author larry@sangerinstitute
 */

@Controller
@RequestMapping("/QuickSearchQuery")
public class QuickSearchQueryController extends AbstractGeneDBFormController {

    Logger logger = Logger.getLogger(QuickSearchQueryController.class);

    @Autowired
    private QueryFactory queryFactory;

    @RequestMapping(method = RequestMethod.GET, params = "q=quickSearchQuery")
    public String processRequest(ServletRequest request, HttpSession session, Model model) throws QueryException {

        String queryName = "quickSearch";
        QuickSearchQuery query = (QuickSearchQuery) queryFactory.retrieveQuery(queryName);

        // Initialise query form

        // Initialise model data somehow
        model.addAttribute("query", query);
        logger.debug("The number of parameters is '" + request.getParameterMap().keySet().size() + "'");
        initialiseQueryForm(query, request);

        QuickSearchQueryResults quickSearchQueryResults = query.getQuickSearchQueryResults();

        // AddTaxonGroupToSession
        session.setAttribute("taxonGroup", quickSearchQueryResults.getTaxonGroup());

        return findDestinationView(queryName, query, model, quickSearchQueryResults, session);
    }

    @RequestMapping(method = RequestMethod.GET, params = "q=none")
    public String displayTaxonGroup(ServletRequest request, @RequestParam("searchText") String searchText,
            @RequestParam("pseudogenes") String pseudogenes, @RequestParam("allNames") String allNames,
            @RequestParam("product") String product, Model model) {

        model.addAttribute("searchText", searchText.trim());
        model.addAttribute("pseudogenes", pseudogenes);
        model.addAttribute("allNames", allNames);
        model.addAttribute("product", product);
        String view = "search/quickSearchTaxons";
        return view;
    }

    /**
     *
     * @param queryName
     * @param query
     * @param model
     * @param quickSearchQueryResults
     * @param session
     * @return
     */
    private String findDestinationView(String queryName, Query query, Model model,
            QuickSearchQueryResults quickSearchQueryResults, HttpSession session) {
        // Get the current taxon name
        String taxonName = findTaxonName(query);
        String resultsKey = null;
        switch (quickSearchQueryResults.getQuickResultType()) {
        case NO_EXACT_MATCH_IN_CURRENT_TAXON:
            logger.debug("No results found for query");
            model.addAttribute("taxonNodeName", taxonName);
            return "search/" + queryName;

        case SINGLE_RESULT_IN_CURRENT_TAXON:
            List<GeneSummary> gs = quickSearchQueryResults.getResults();
            cacheResults(gs, query, queryName, session.getId());
            return "redirect:/NamedFeature?name=" + gs.get(0).getSystematicId();

        case MULTIPLE_RESULTS_IN_CURRENT_TAXON:
            List<GeneSummary> gs2 = quickSearchQueryResults.getResults();
            resultsKey = cacheResults(gs2, query, queryName, session.getId());
            model.addAttribute("key", resultsKey);
            model.addAttribute("taxonNodeName", taxonName);
            logger.debug("Found results for query (Size: '" + gs2.size() + "' key: '" + resultsKey
                    + "')- redirecting to Results controller");
            return "redirect:/Results";

        case ALL_ORGANISMS_IN_ALL_TAXONS:
            List<GeneSummary> gs3 = quickSearchQueryResults.getResults();
            resultsKey = cacheResults(gs3, query, queryName, session.getId());
            model.addAttribute("key", resultsKey);
            model.addAttribute("taxonNodeName", taxonName);
            logger.debug("Found results for query (Size: '" + gs3.size() + "' key: '" + resultsKey
                    + "')- redirecting to Results controller");
            return "redirect:/Results";

        default:
            return "";
        }
    }

}
