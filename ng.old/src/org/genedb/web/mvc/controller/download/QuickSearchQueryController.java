package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.core.QueryVisibility;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.QuickSearchQuery;
import org.genedb.querying.tmpquery.QuickSearchQuery.QuickSearchQueryResults;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


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

    public static final String QUERY_NAME = "quickSearch";

    Logger logger = Logger.getLogger(QuickSearchQueryController.class);

    //@Autowired
    private QueryFactory queryFactory;

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    @RequestMapping(method = RequestMethod.GET)
    public String processRequest(ServletRequest request, HttpSession session, Model model) throws QueryException {

        QuickSearchQuery query = (QuickSearchQuery) queryFactory.retrieveQuery(QUERY_NAME, NumericQueryVisibility.PRIVATE);

        if (query==null){
            return "redirect:/Query";
        }

        // Initialise model data somehow
        model.addAttribute("query", query);
        logger.error("The number of parameters is '" + request.getParameterMap().keySet().size() + "'");
        initialiseQueryForm(query, request);

        if (query != null && StringUtils.isEmpty(query.getSearchText())){
            logger.error("The query isn't null but no search terms. Returning to Query list");
            return "redirect:/Query";
        }

        QuickSearchQueryResults quickSearchQueryResults = query.getQuickSearchQueryResults();

        // AddTaxonGroupToSession
        model.addAttribute("taxonGroup", quickSearchQueryResults.getTaxonGroup());

        return findDestinationView(query, model, quickSearchQueryResults, session);
    }
//
//    @RequestMapping(method = RequestMethod.GET, params = "q=none")
//    public String displayTaxonGroup(ServletRequest request, @RequestParam("searchText") String searchText,
//            @RequestParam("pseudogenes") String pseudogenes, @RequestParam("allNames") String allNames,
//            @RequestParam("product") String product, Model model) {
//
//        model.addAttribute("searchText", searchText.trim());
//        model.addAttribute("pseudogenes", pseudogenes);
//        model.addAttribute("allNames", allNames);
//        model.addAttribute("product", product);
//        String view = "search/quickSearchTaxons";
//        return view;
//    }

    /**
     *
     * @param queryName
     * @param query
     * @param model
     * @param quickSearchQueryResults
     * @param session
     * @return
     */
    private String findDestinationView(Query query, Model model,
            QuickSearchQueryResults quickSearchQueryResults, HttpSession session) {
        // Get the current taxon name
        String taxonName = findTaxonName(query);
        String resultsKey = null;

        logger.error("The number of results is '"+quickSearchQueryResults.getResults().size());



        switch (quickSearchQueryResults.getQuickResultType()) {
        case NO_EXACT_MATCH_IN_CURRENT_TAXON:
            logger.error("No results found for query");
            model.addAttribute("taxonNodeName", taxonName);
            return "search/" + QUERY_NAME;

        case SINGLE_RESULT_IN_CURRENT_TAXON:
            List<GeneSummary> gs = quickSearchQueryResults.getResults();
            cacheResults(gs, query, QUERY_NAME, quickSearchQueryResults.getTaxonGroup(), session.getId());
            logger.error("The result is "+gs.get(0));
            return "redirect:/gene/" + gs.get(0).getSystematicId();

        case MULTIPLE_RESULTS_IN_CURRENT_TAXON:
            List<GeneSummary> gs2 = quickSearchQueryResults.getResults();
            resultsKey = cacheResults(gs2, query, QUERY_NAME, quickSearchQueryResults.getTaxonGroup(), session.getId());
            //model.addAttribute("key", resultsKey);
            model.addAttribute("taxonNodeName", taxonName);
            logger.error("Found results for query (Size: '" + gs2.size() + "' key: '" + resultsKey
                    + "')- redirecting to Results controller");
            return "redirect:/Results/"+resultsKey;

        case ALL_ORGANISMS_IN_ALL_TAXONS:
            List<GeneSummary> gs3 = quickSearchQueryResults.getResults();
            resultsKey = cacheResults(gs3, query, QUERY_NAME, quickSearchQueryResults.getTaxonGroup(), session.getId());
            //model.addAttribute("key", resultsKey);
            model.addAttribute("taxonNodeName", taxonName);
            logger.error("Found results for query (Size: '" + gs3.size() + "' key: '" + resultsKey
                    + "')- redirecting to Results controller");
            return "redirect:/Results/"+resultsKey;

        default:
            return "";
        }
    }

}
