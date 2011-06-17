package org.genedb.web.mvc.controller.download;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.genedb.querying.core.PagedQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.QueryHistoryItem;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.IdsToGeneSummaryQuery;
import org.genedb.querying.tmpquery.QuickSearchQuery;
import org.genedb.querying.tmpquery.SuggestQuery;
import org.genedb.util.Pair;
import org.genedb.web.mvc.controller.HistoryManagerFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/Query")
public class QueryController extends AbstractGeneDBFormController{

    private static final Logger logger = Logger.getLogger(QueryController.class);

    @SuppressWarnings("unchecked")
	private QueryFactory queryFactory;

    private HistoryManagerFactory hmFactory;
    
    public static final int DEFAULT_LENGTH = 30;


    public void setHmFactory(HistoryManagerFactory hmFactory) {
        this.hmFactory = hmFactory;
    }

    @SuppressWarnings("unchecked")
	public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        return "redirect:/QueryList";
    }

    @SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET , value="/{queryName}")
    public String chooseFormHandling(
            @PathVariable(value="queryName") String queryName,
            @RequestParam(value="suppress", required=false) String suppress,
            HttpServletRequest request,
            HttpSession session,
            Model model) throws QueryException {
    	
    	logger.info(queryName);
    	
    	model.addAttribute("actionName" , request.getContextPath() + "/Query/" + queryName);
    	
    	
    	Map<String, String[]> parameters = request.getParameterMap();
    	for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
    		for (String value : entry.getValue()) {
    			logger.info(entry.getKey() + " : " + value);
    		}
    	}
    	
    	logger.debug("The number of parameters is '" + request.getParameterMap().keySet().size() + "'");
    	
    	// FIXME determine if this check for parameter count is the best way to decide whether or not to perform the query 
        if (request.getParameterMap().size() > 1) {
            return processForm(queryName, suppress, request, session, model);
        } else {
            return displayForm(queryName, request, session, model);
        }
    }


    public String displayForm(
            String queryName,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {
    	
    	logger.info("DISPLAYING FORM");
    	
        Query query = findQueryType(queryName, session);
        if (query==null){
        	logger.warn("No query, redirecting to the list");
            return "redirect:/QueryList";
        }
        
        //Initialise model data somehow
        model.addAttribute("query", query);
        populateModelData(model, query);

        String taxonNodeName = findTaxonName(query);
        
        if (taxonNodeName == null || taxonNodeName.length() == 0) {
        	if (request.getParameter("taxonNodeName") != null) {
        		taxonNodeName = request.getParameter("taxonNodeName"); 
        	}
        }
        
        logger.info("TaxonNodeName is "+taxonNodeName);
        
        model.addAttribute("taxonNodeName", taxonNodeName);
        return "search/"+queryName;
    }


    @SuppressWarnings("unchecked")
	public String processForm(
            String queryName,
            String suppress,
            HttpServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

    	logger.info("PROCESSING FORM");
    	
    	HistoryManager hm = hmFactory.getHistoryManager(session);
    	
        String key = generateKey(session.getId(), (Map<String, String[]>) request.getParameterMap());
        logger.info("Query key: " + key);
        
        PagedQuery query = null;
        QueryHistoryItem item = hm.getQueryHistoryItem(key);
        
        if (item == null) {
        	logger.info("Could not find existing query for this session, creating ...");
        	
        	query = findQueryType(queryName, session);
            if (query==null){
                logger.error(String.format("Unable to find query of name '%s'", queryName));
                return "redirect:/Query";
            }
            
        } else {
        	query = item.getQuery();
        }
        
        logger.info("Using query " + query.getQueryName());
        
        // Initialise query form
        Errors errors = initialiseQueryForm(query, request);
        if (errors.hasErrors()) {
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            logger.debug("Returning due to binding error");
            for (ObjectError error : errors.getAllErrors()) {
            	logger.error(error);
            }
            return "search/"+queryName;
        }
        
        // Validate initialised form
        query.validate(query, errors);
        if (errors.hasErrors()) {
            logger.debug("Validator found errors");
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "query", errors);
            return "search/"+queryName;
        }
        logger.debug("Validator found no errors");
        
        // Initialise model data 
        populateModelData(model, query);
        
        item = hm.addQueryHistoryItem(key, query );
        
        model.addAttribute("query", query);
        
        
        Bounds bounds = getQueryBounds(request);
        logger.info("Query page " + bounds.page + ", length " + bounds.length);
        
        int start = bounds.page * bounds.length;
        int end = start + bounds.length;
        
        List<String> ids = query.getResults(start, end);
        
        model.addAttribute("bounds",bounds);
        
        logger.info("Size :: ");
        
        logger.info(ids.size());
        
        for (String id : ids) {
    		logger.info(id);
    	}
        
        List<GeneSummary> results = summaries(ids);
        
        logger.info(results.size());
        
        // Suppress item in results
        // suppressResultItem(suppress, results);
        
        String taxonName = findTaxonName(query);
        logger.info("TaxonNodeName is " + taxonName);
        model.addAttribute("taxonNodeName", taxonName);
        
        int totalResultsSize = query.getTotalResultsSize();
        logger.info("Total result size " + totalResultsSize);
    	model.addAttribute("resultsSize", totalResultsSize);
        
    	if (queryName.equals("quickSearch")) {
        	QuickSearchQuery quickSearchQuery = (QuickSearchQuery) query;
        	logger.info("Fetching quick search taxons");
        	model.addAttribute("taxonGroup", quickSearchQuery.getQuickSearchQueryResults().getTaxonGroup());
        }
    	
    	if (results.size() == 1) {
    		
    		GeneSummary geneSummary = results.get(0);
    		return "redirect:/gene/" + geneSummary.getSystematicId();
    		
    	} else if (results.size() > 0) {
        	
        	model.addAttribute("results", results);
            model.addAttribute("queryName", queryName);
            model.addAttribute("actionName" , request.getContextPath() + "/Query/" + queryName);
            
            return "search/"+ queryName;
            
        } 
    	
    	// no point in hanging onto this
        hm.removeItem(key);
        logger.warn("No results found for query");
		model.addAttribute("noResultFound", Boolean.TRUE);
		
    	if (queryName.equals("quickSearch")) {
        	
        	QuickSearchQuery quickSearchQuery = (QuickSearchQuery) query;
        	logger.info("Running suggest query as no result found");
        	SuggestQuery squery = (SuggestQuery) queryFactory.retrieveQuery("suggest", NumericQueryVisibility.PRIVATE);
        	squery.setSearchText(quickSearchQuery.getSearchText());
        	squery.setTaxons(quickSearchQuery.getTaxons());
        	squery.setMax(30);
        	
			List<String> sResults = squery.getResults();
			model.addAttribute("suggestions", sResults);
			
        }
        
		return "search/" + queryName;
        

    }
    
    private List<GeneSummary> summaries (List<String> ids) throws QueryException {
    	IdsToGeneSummaryQuery idsToGeneSummary = (IdsToGeneSummaryQuery) queryFactory.retrieveQuery("idsToGeneSummary", NumericQueryVisibility.PRIVATE);
    	idsToGeneSummary.setIds(ids);
    	List<GeneSummary> summaries = idsToGeneSummary.getResultsSummaries();
    	for (GeneSummary summary : summaries) {
    		logger.info(summary.getDisplayId());
    	}
    	return summaries;
    }
    
    public class Bounds {
    	private int page;
    	private int length;
    	public Bounds(int page, int length) {
    		this.page = page;
    		this.length = length;
    	}
    	public int getPage(){return page;}
    	public int getLength(){return length;}
    }
    
    private Bounds getQueryBounds(HttpServletRequest request) {
        String startString = request.getParameter((new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_PAGE)));
        // Use 1-based index for start and end
        int page = 0;
        if (startString != null) {
            //start = (Integer.parseInt(startString) - 1) * DEFAULT_LENGTH + 1;
        	page = Integer.parseInt(startString) - 1 ;
        }
        
        return new Bounds(page,DEFAULT_LENGTH);
    }
    
    /*private final String displayResults (
            HttpServletRequest request,
            Model model,
            HistoryItem item,
            Bounds bounds,
            List<GeneSummary> results) throws QueryException {
    	
        String pName = new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_PAGE);
        logger.debug("pName is '"+pName+"'");
        String startString = request.getParameter((new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_PAGE)));
        logger.debug("The start string is '"+startString+"'");
        
        // Use 1-based index for start and end
        int start = 1;
        if (startString != null) {
            start = (Integer.parseInt(startString) - 1) * DEFAULT_LENGTH + 1;
        }
        
//        MyPaginatedList summaries = new MyPaginatedList<GeneSummary>();
//        summaries.fullListSize = results.size();
//        summaries.list = results;
//        summaries.objectsPerPage = DEFAULT_LENGTH;
//        summaries.pageNumber = bounds.getFirst();
//        summaries.searchId = "xxx";
//        summaries.sortDirection = SortOrderEnum.ASCENDING;
//        summaries.sortCriterion = "systematicId";
        
        //logger.info(summaries.list);
        
        model.addAttribute("results", results);
        model.addAttribute("resultsSize", item.getQuery().getResultsSize());
        
        int end = start + DEFAULT_LENGTH;
        
        int resultSize = item.getQuery().getResultsSize();
        
        logger.debug("The number of results retrieved is '"+resultSize+"'");
        logger.debug("The end marker, before adjustment, is '"+end+"'");

        if (end > resultSize + 1) {
            end = resultSize + 1;
        }
        
        model.addAttribute("firstResultIndex", start);
        
        Query query = item.getQuery();
        
        	
    	String queryName = queryFactory.getRealName(query);
    	
    	model.addAttribute("queryName", queryName);
    	
        populateModelData(model, query);
        
        model.addAttribute("query", query);
        
        
        if (queryName.equals("quickSearch")) {
        	
        	QuickSearchQuery quickSearchQuery = (QuickSearchQuery) query;
        	//model.addAttribute("query", quickSearchQuery);
        	logger.info("taxonGroup : " + quickSearchQuery.getQuickSearchQueryResults(bounds.page, bounds.length).getTaxonGroup());
        	model.addAttribute("taxonGroup", quickSearchQuery.getQuickSearchQueryResults(bounds.page, bounds.length).getTaxonGroup());
        	
        	if (resultSize == 0) {
            	SuggestQuery squery = (SuggestQuery) queryFactory.retrieveQuery("suggest", NumericQueryVisibility.PRIVATE);
            	squery.setSearchText(quickSearchQuery.getSearchText());
            	squery.setTaxons(quickSearchQuery.getTaxons());
            	squery.setMax(30);
            	
				List sResults = squery.getResults();
				model.addAttribute("suggestions", sResults);
        	}
        	
        } 

        
        model.addAttribute("actionName" , request.getContextPath() + "/Query/" + queryName);
        
        return "search/"+ queryName;
        //return "list/results2";
    }
    */
    
    private String generateKey(String sessionId, Map<String, String[]> parameters) {
    	
    	int hashcode = 0;
    	
    	for (Entry<String, String[]> entry : parameters.entrySet()) {
    		
    		// ignore display tag pagination parameters
    		if (entry.getKey().equals("d-16544-p")) {
    			continue;
    		}
    		
    		hashcode += entry.getKey().hashCode();
    		for (String value : entry.getValue()) {
    			hashcode += value.hashCode();
    		}
    	}
    	
    	String key = "query:"+ sessionId + ":"+ hashcode; 
    	return key;
    }
    
    private void populateModelData(Model model, Query query) {
        Map<String, Object> modelData = query.prepareModelData();
        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
        	logger.info(entry.getKey() +" --- " + entry.getValue());
            model.addAttribute(entry.getKey(), entry.getValue());
        }
    }

    protected PagedQuery findQueryType(String queryName, HttpSession session){
        if (!StringUtils.hasText(queryName)) {
        	WebUtils.setFlashMessage("Unable to identify which query to use", session);
        	logger.error("Unable to identify which query to use");
        	return null;
        }
        
        /*
         * Queries that the QueryController can handle must be public. PUBLIC_BUT_NO_FORMS is a special case 
         * where links on gene pages (or elsewhere) are pointing here, rather than forms themeselves. 
         */
        PagedQuery query = (PagedQuery) queryFactory.retrieveQuery(queryName, NumericQueryVisibility.PUBLIC_BUT_NO_FORMS);
        if (query == null) {
        	WebUtils.setFlashMessage("Unable to find query called '" + queryName + "'", session);
        	logger.error("Unable to find query called '" + queryName + "'");
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
    
    class MyPaginatedList<T> implements PaginatedList {
    	
    	private int fullListSize;
    	private List<T> list;
    	private int objectsPerPage;
    	private int pageNumber;
    	private String searchId;
    	private String sortCriterion;
    	private SortOrderEnum sortDirection;
    	
		@Override
		public int getFullListSize() {
			return fullListSize;
		}

		@Override
		public List<T> getList() {
			return list;
		}

		@Override
		public int getObjectsPerPage() {
			return objectsPerPage;
		}

		@Override
		public int getPageNumber() {
			return pageNumber;
		}

		@Override
		public String getSearchId() {
			return searchId;
		}

		@Override
		public String getSortCriterion() {
			return sortCriterion;
		}

		@Override
		public SortOrderEnum getSortDirection() {
			return sortDirection;
		}
    	
    }
    
}
