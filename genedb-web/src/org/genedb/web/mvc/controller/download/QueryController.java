package org.genedb.web.mvc.controller.download;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.genedb.querying.tmpquery.IdsToGeneSummaryQuery;
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
import org.springframework.web.bind.annotation.SessionAttributes;


@Controller
@RequestMapping("/Query")
@SessionAttributes("results")
public class QueryController {

    private static final Logger logger = Logger.getLogger(QueryController.class);

    private static final String IDS_TO_GENE_SUMMARY_QUERY = "idsToGeneSummary";

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private QueryFactory queryFactory;

    @Autowired
    private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;


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
        // TODO Do we want form submission via GET?
        //return processForm(queryName, request, session, model);

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
        if (results.size() > 0) {
            Object firstItem =  results.get(0);
            if (! (firstItem instanceof GeneSummary)) {
                results = convertIdsToGeneSummaries(results);
            }
        }

        model.addAttribute("runQuery", Boolean.TRUE);

        switch (results.size()) {
        case 0:
            logger.error("No results found for query");
            model.addAttribute("results", results);
            return "search/"+queryName;
        case 1:
            return "redirect:/NamedFeature?name="+((GeneSummary)results.get(0)).getSystematicId();
        default:
            model.addAttribute("results", results);
            logger.error("Found results for query");
            return "search/"+queryName;
        }
    }


    private List convertIdsToGeneSummaries(List results) throws QueryException {
        logger.error(results.size()+ " results for HQL query");
        List<String> ids = new ArrayList<String>(results.size());
        List<GeneSummary> ret = new ArrayList<GeneSummary>();


        for (String id : (List<String>) results) {
            if (id.endsWith(":pep")) {
                id = id.replace(":pep", "");
            }
            if (id.endsWith(":mRNA")) {
                id = id.replace(":mRNA", "");
            }
            ids.add(id);
        }
        IdsToGeneSummaryQuery idsToGeneSummary = (IdsToGeneSummaryQuery) queryFactory.retrieveQuery(IDS_TO_GENE_SUMMARY_QUERY);
        if (idsToGeneSummary == null) {
            throw new RuntimeException("Internal error - unable to find ids to gene summary query");
        }
        idsToGeneSummary.setIds(ids);
        ret.addAll((List<GeneSummary>)idsToGeneSummary.getResults());

//        int top = 0;
//        int max = 3;
//
//        while (top < ids.size()) {
//            List<String> ids2 = new ArrayList<String>();
//            max = ((ids.size() - top) >= BATCH_SIZE) ? BATCH_SIZE : ids.size() - top;
//            for (int i=0; i < max ; i++) {
//                String id = ids.get(i+top);
//                if (id.endsWith(":pep")) {
//                    id.replace(":pep", "");
//                }
//                if (id.endsWith(":mRNA")) {
//                    id.replace(":mRNA", "");
//                }
//                ids2.add(id);
//                IdsToGeneSummaryQuery idsToGeneSummary = (IdsToGeneSummaryQuery) queryFactory.retrieveQuery(IDS_TO_GENE_SUMMARY_QUERY);
//                if (idsToGeneSummary == null) {
//                    throw new RuntimeException("Internal error - unable to find ids to gene summary query");
//                }
//                idsToGeneSummary.setIds(ids2);
//                ret.addAll((List<GeneSummary>)idsToGeneSummary.getResults());
//            }
//            top += max;
//        }
        return ret;
    }

    private void populateModelData(Model model, Query query) {
        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
    }

}
