package org.genedb.web.mvc.controller;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.ChangedGeneFeaturesQuery;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.QuickSearchQuery;
import org.genedb.querying.tmpquery.SuggestQuery;
import org.genedb.querying.tmpquery.QuickSearchQuery.QuickSearchQueryResults;
import org.genedb.querying.tmpquery.TopLevelFeaturesQuery;
import org.genedb.querying.tmpquery.TopLevelFeaturesQuery.TopLevelFeature;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 *
 * A controller for REST services, initially focused on EupathDB requirements.
 *
 *
 * @author gv1
 *
 */
@Controller
@RequestMapping("/service")
public class RestController {

    private static final Logger logger = Logger.getLogger(RestController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("organismDao")
    OrganismDao organismDao;

    @Autowired
    @Qualifier("sequenceDao")
    SequenceDao sequenceDao;
    
    @Autowired
    @Qualifier("cvDao")
    CvDao cvDao;

    private final String viewName = "json:";

    @RequestMapping(method=RequestMethod.GET, value={"/test", "/test.*"})
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response)
    {
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("model", "hello world");
        return mav;
    }
    
    @RequestMapping(method=RequestMethod.GET, value="/organisms")
    public ModelAndView organisms() {
    	ModelAndView mav = new ModelAndView(viewName);
    	
    	OrganismResults results = new OrganismResults();
    	
    	// using the taxon node manager to make sure any organism that isn't properly setup doesn't slip through here
    	TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
    	TaxonNode taxonNode = tnm.getTaxonNodeForLabel("Root");
        List<TaxonNode> childrens = taxonNode.getAllChildren();
        for (TaxonNode node : childrens)
        {
        	if (node.isOrganism() && node.isPopulated()) {
        		results.organisms.add(node.getName(TaxonNameType.NICKNAME));
        		System.out.println("NICKNAME :: " + TaxonNameType.NICKNAME);
        	}
        }
    	
        System.out.println(results.organisms);
        
    	Collections.sort(results.organisms);
    	
    	mav.addObject("model", results);
    	
    	return mav;
    }
    
    
    @RequestMapping(method=RequestMethod.GET, value="/top")
    public ModelAndView top(@RequestParam("commonName") String commonName) throws RestException {
    	ModelAndView mav = new ModelAndView(viewName);
    	
    	Organism org = organismDao.getOrganismByCommonName(commonName);
    	
    	TopLevelResults results = new TopLevelResults();
    	results.organism = commonName;
    	
    	//TopLevelFeaturesQuery topQuery = (TopLevelFeaturesQuery) queryFactory.retrieveQuery("topLevelFeatures", NumericQueryVisibility.PRIVATE); 
    	TopLevelFeaturesQuery topQuery = (TopLevelFeaturesQuery) applicationContext.getBean("topLevelFeatures", TopLevelFeaturesQuery.class);
    	
    	TaxonNode taxonNode = getTaxonNode(commonName);
    	TaxonNodeList taxons = new TaxonNodeList(taxonNode);
    	topQuery.setTaxons(taxons);
    	
    	try {
    		
    		
    		List<TopLevelFeature> tops = topQuery.getResults();
    		
    		for (TopLevelFeature top : tops) {
    			results.features.add(top);
    		}
    		
	    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error(e.getMessage());
    	}
    	
    	mav.addObject("model", results);
    	
    	return mav;
    }
    
    
    @RequestMapping(method=RequestMethod.GET, value={"/changesummary", "/changesummary.*"})
    public ModelAndView changesSummary(@RequestParam("since") String since,  @RequestParam("taxon") String taxon)
    {
    	
    	ModelAndView mav = new ModelAndView(viewName);
    	
    	try {
    		TaxonNode taxonNode = getTaxonNode(taxon);
	    	Organism o = getOrganism(taxonNode);
	    	Date sinceDate = getSinceDate(since);
	    	
	    	ChangedGeneFeaturesQuery changedGeneFeaturesQuery = (ChangedGeneFeaturesQuery) applicationContext.getBean("changedGeneFeatures", ChangedGeneFeaturesQuery.class);
	        changedGeneFeaturesQuery.setDate(sinceDate);
	        changedGeneFeaturesQuery.setOrganismId(o.getOrganismId());
	    	
	        final ChangedFeatureSetResultSummary organismSetResultSummary = new ChangedFeatureSetResultSummary();
	        organismSetResultSummary.since = sinceDate.toString();
	        organismSetResultSummary.name = "genome/changes";
	        organismSetResultSummary.taxonomyID = taxonNode.getTaxonId();
	        organismSetResultSummary.count = 0;
	        
	        final Hashtable<String,Integer> statistics = new Hashtable<String,Integer>(); 
	        
	        
	        
	    	changedGeneFeaturesQuery.processCallBack(new RowCallbackHandler(){
	            public void processRow(ResultSet rs) throws SQLException {
	            	String type = rs.getString("type");
	            	
	            	if (! statistics.containsKey(type)) {
	            		statistics.put(type, 0);
	            	}
	            	statistics.put(type, statistics.get(type) + 1);
	            	
	            	organismSetResultSummary.count += 1;
	            }
	    	});
	    	
	    	for (String key : statistics.keySet()) {
	    		ChangedFeatureSetResultSummaryStatistic statistic = new ChangedFeatureSetResultSummaryStatistic();
	    		statistic.annotation = key; 
	    		statistic.count = statistics.get(key);
	    		organismSetResultSummary.statistics.add(statistic);
	    		
	    		logger.info(key + " " + statistics.get(key));
	    	}
	    	
	    	
	    	mav.addObject("model", organismSetResultSummary);
    		
    	} catch (RestException re) {
    		mav.addObject("model", re.model);
	    } catch (Exception e) {
	    	logger.error(e.getMessage());
			e.printStackTrace();
			mav.addObject("model", new ErrorModel(e.getMessage()));
			
		}
	    
	    return mav;
	    
    }
    
    private Organism getOrganism(TaxonNode taxonNode) throws RestException {
    	return organismDao.getOrganismByCommonName(taxonNode.getLabel());
    }
    
    private TaxonNode getTaxonNode(String taxon) throws RestException {
    	TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
    	TaxonNode taxonNode = tnm.getTaxonNodeForLabel(taxon);
    	if (taxonNode == null) {
    		throw new RestException(ErrorType.INVALID_PARAMETER, "Could not find a taxonNode for taxon " + taxon );
    	}
    	logger.info(taxonNode);
    	return taxonNode;
    }
    
    private Date getSinceDate(String since) throws RestException {
    	Date sinceDate = Calendar.getInstance().getTime();
        try {
            return getDateFromString(since);
        } catch (ParseException e) {
            throw new RestException(ErrorType.MISSING_PARAMETER, "Please supply a date as 'yyyy-mm-dd'.");
        }
    }

    /**
     * Returns all features changed since a certain date, as determined by the DateQuery.
     *
     * @param since
     * @param since
     * @return
     */
    @RequestMapping(method=RequestMethod.GET, value={"/changes"})
    public ModelAndView changes(@RequestParam("since") String since,  @RequestParam("taxon") String taxon, @RequestParam(value="type", required=false) String type)
    {
    	logger.info(String.format("Searching for changes in %s since %s : ", taxon, since));
    	ModelAndView mav = new ModelAndView(viewName);
    	
    	try {
    		TaxonNode taxonNode = getTaxonNode(taxon);
	    	Organism o = getOrganism(taxonNode);
	    	Date sinceDate = getSinceDate(since);
	        
	        ChangedGeneFeaturesQuery changedGeneFeaturesQuery = (ChangedGeneFeaturesQuery) applicationContext.getBean("changedGeneFeatures", ChangedGeneFeaturesQuery.class);
	        changedGeneFeaturesQuery.setDate(sinceDate);
	        changedGeneFeaturesQuery.setOrganismId(o.getOrganismId());
	        
	        if (type != null) {
	        	changedGeneFeaturesQuery.setType(type);
	        }
	        
	        final ChangedFeatureSetResult organismSetResult = new ChangedFeatureSetResult();
	        organismSetResult.since = sinceDate.toString();
	        organismSetResult.name = "genome/changes";
	        organismSetResult.taxonomyID = taxonNode.getTaxonId();
	        organismSetResult.count = 0;
	        
	
	        changedGeneFeaturesQuery.processCallBack(new RowCallbackHandler(){
	            public void processRow(ResultSet rs) throws SQLException {
	                FeatureStatus fs = new FeatureStatus();
	
	                fs.type = rs.getString("type");
	                fs.changedetail = rs.getString("changedetail");
	                fs.changedate = rs.getString("changedate");
	                fs.geneuniquename = rs.getString("geneuniquename");
	                fs.mrnauniquename = rs.getString("mrnauniquename");
	                fs.transcriptuniquename = rs.getString("transcriptuniquename");
	                
	                logger.info(fs);
	                
	                organismSetResult.addResult(fs);
	                organismSetResult.count += 1;
	            }
	        });
	
	        
	        
	        mav.addObject("model", organismSetResult);
	    
    	} catch (RestException re) {
    		mav.addObject("model", re.model);
	    } catch (Exception e) {
	    	logger.error(e.getMessage());
			e.printStackTrace();
			mav.addObject("model", new ErrorModel(e.getMessage()));
			
		}
	    return mav;
	    
    }
    

    
    /**
     *
     * Wraps QuickSearchQuery, and falls back onto a SuggestQuery.
     *
     * @param term
     * @param taxon
     * @param max
     * @return
     * @throws QueryException
     */
	@RequestMapping(method=RequestMethod.GET, value={"/search", "/search.*"})
    public ModelAndView search ( @RequestParam("term") String term, @RequestParam("taxon") String taxon, @RequestParam("max") int max ) throws QueryException {
		
		logger.info(String.format("Searching %s for %s : ", taxon, term));
		ModelAndView mav = new ModelAndView(viewName);
		
		try {
			
	    	QuickSearchQuery query = (QuickSearchQuery) applicationContext.getBean("quickSearch", QuickSearchQuery.class);
	    	query.setSearchText(term);
	    	
	    	query.setAllNames(true);
	    	query.setProduct(true);
	    	query.setPseudogenes(true);
	    	
	    	TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
	    	TaxonNode taxonNode = tnm.getTaxonNodeForLabel(taxon);
	    	
	    	if (taxonNode == null) {
	    		throw new RestException(ErrorType.INVALID_PARAMETER, "Could not find a taxonNode for taxon " + taxon );
	    	}
	    	
	    	logger.info(taxonNode);
	    	
	    	TaxonNodeList taxons = new TaxonNodeList(taxonNode);
	
	    	query.setTaxons(taxons);
	
	    	QuickSearchQueryResults results = query.getReallyQuickSearchQueryResults(max);
	    	List<GeneSummary> geneResults = results.getResults();
	
	    	QuickSearchResults qsr = new QuickSearchResults();
	    	qsr.term = term;
	    	qsr.max = max;
	    	qsr.totalHits = results.getTotalHits();
	
	
	    	int i = 0;
	    	for (GeneSummary result : geneResults) {
	    		i++;
	
	    		QuickSearchResult q = new QuickSearchResult();
	    		q.systematicId = result.getSystematicId();
	    		q.product = result.getProduct();
	    		q.displayId = result.getDisplayId();
	    		q.taxonDisplayName = result.getTaxonDisplayName();
	    		q.topLevelFeatureName = result.getTopLevelFeatureName();
	
	    		qsr.addHit(q);
	    	}
	
	    	logger.info("Processed " + i + " results");
	
			SuggestQuery squery = (SuggestQuery) applicationContext.getBean("suggest", SuggestQuery.class);
	    	squery.setSearchText(term);
	    	squery.setMax(max);
	    	squery.setTaxons(taxons);
	
	    	@SuppressWarnings("unchecked")
	    	List<String> sResults = (List<String>) squery.getResults();
	
	    	for (Object sResult : sResults) {
	    		// logger.debug(sResult);
	    		Suggestion s = new Suggestion();
	    		s.name = (String) sResult;
	    		qsr.addSuggestion(s);
	
	    	}
	    	
	        mav.addObject("model", qsr);
		
		} catch (RestException re) {
    		mav.addObject("model", re.model);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			mav.addObject("model", new ErrorModel(e.getMessage()));
			
		}
		
		logger.info("returning " + mav);
		
		return mav;
    }


    /*
     *
     * Private utilities follow.
     *
     */


    /**
     *
     * Searches for a taxonnode given a taxonomy id.
     *
     * @param taxonomyID
     * @return
     */
    private TaxonNode getTaxonFromTaxonomyID(String taxonomyID)
    {
        List<Organism> organisms = organismDao.getOrganisms();
        for (Organism organism : organisms)
        {
            logger.debug(organism);
            String orgTaxonID = organism.getPropertyValue("genedb_misc", "taxonId");

            if (orgTaxonID == null)
                continue;

            logger.debug(orgTaxonID);

            if (orgTaxonID.equals(taxonomyID))
            {
                String organismTaxonName = organism.getGenus() + " " + organism.getSpecies();
                logger.debug(organismTaxonName);
                TaxonNode taxon = getTaxonFromID( organismTaxonName );
                logger.debug(taxon);
                return taxon;
            }

        }
        return null;
    }

    /**
     * The full taxon name is the genus and the species.
     * @param fullTaxonName equal to organism.getGenus() + " " + organism.getSpecies()
     * @return a taxon node if it finds one
     */
    private TaxonNode getTaxonFromID(String fullTaxonName)
    {
        TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
        TaxonNode taxonNode = tnm.getTaxonNodeForLabel("Root");
        List<TaxonNode> childrens = taxonNode.getAllChildren();
        for (TaxonNode node : childrens)
        {
            if (node.getName(TaxonNameType.FULL).equals(fullTaxonName))
            {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns a list of taxons.
     */
    private List<TaxonNode> getAllTaxons()
    {
        TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
        TaxonNode taxonNode = tnm.getTaxonNodeForLabel("Root");
        List<TaxonNode> childrens = taxonNode.getAllChildren();
        return childrens;
    }

    /**
     * Returns a date from a string, if formatted correctly.
     * @param since
     * @return
     * @throws ParseException
     */
    private Date getDateFromString(String since)
    throws ParseException
    {
        Date sinceDate = Calendar.getInstance().getTime();
        if (since != null)
        {
            logger.info("supplied since " + since);
            SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
            sinceDate = dateFormat.parse(since);
        }
        return sinceDate;
    }



}


/**
 * A list of error types that services can return. Error codes are autogenerated from the position in this enum.
 * @author gv1
 *
 */
enum ErrorType
{
    MISSING_PARAMETER,
    INVALID_PARAMETER,
    NO_RESULT,
    QUERY_FAILURE,
    MISC
}




/*
 *
 * Following are initial view-model definitions. Eventually these should be moved out.
 *
 */

@XStreamAlias("baseResult")
class BaseResult
{
    @XStreamAlias("name")
    @XStreamAsAttribute
    protected String name;
}

@XStreamAlias("results")
class RestResultSet extends BaseResult
{
    @XStreamImplicit()
    private List<BaseResult> results = new ArrayList<BaseResult>();

    public void addResult(BaseResult br)
    {
        results.add(br);
    }
}

@XStreamAlias("results")
class TopLevelResults extends BaseResult {
	@XStreamAlias("organism")
    @XStreamAsAttribute
    public String organism;
	
	@XStreamImplicit()
	@XStreamAlias("features")
    public List<TopLevelFeature> features = new ArrayList<TopLevelFeature>();
	
}


@XStreamAlias("results")
class OrganismResults extends BaseResult {
	
	@XStreamAlias("organisms")
    public List<String> organisms = new ArrayList<String>();
	
}

@XStreamAlias("results")
class QuickSearchResults extends BaseResult {

	@XStreamAlias("term")
    @XStreamAsAttribute
    public String term;

	@XStreamAlias("max")
    @XStreamAsAttribute
    public int max;

	@XStreamAlias("totalHits")
    @XStreamAsAttribute
    public int totalHits;

    @XStreamImplicit()
    private List<BaseResult> suggestions = new ArrayList<BaseResult>();
    public void addSuggestion(BaseResult br)
    {
    	suggestions.add(br);
    }

    @XStreamImplicit()
    private List<BaseResult> hits = new ArrayList<BaseResult>();
    public void addHit(BaseResult br)
    {
    	suggestions.add(br);
    }

}

@XStreamAlias("suggestions")
class Suggestion extends BaseResult { }

@XStreamAlias("hits")
class QuickSearchResult extends BaseResult
{
    @XStreamAlias("systematicId")
    @XStreamAsAttribute
    public String systematicId;

    @XStreamAlias("displayId")
    @XStreamAsAttribute
    public String displayId;

    @XStreamAlias("taxonDisplayName")
    @XStreamAsAttribute
    public String taxonDisplayName;

    @XStreamAlias("product")
    @XStreamAsAttribute
    public String product;

    @XStreamAlias("topLevelFeatureName")
    @XStreamAsAttribute
    public String topLevelFeatureName;

    @XStreamAlias("left")
    @XStreamAsAttribute
    public String left;
}




@XStreamAlias("results")
class ChangedFeatureSetResult extends RestResultSet
{
    @XStreamAlias("since")
    @XStreamAsAttribute
    public String since;

    @XStreamAlias("taxonomyID")
    @XStreamAsAttribute
    public String taxonomyID;

    @XStreamAlias("count")
    @XStreamAsAttribute
    public int count;
}

@XStreamAlias("results")
class ChangedFeatureSetResultSummary extends ChangedFeatureSetResult {
	
	@XStreamAlias("summary")
	public List<ChangedFeatureSetResultSummaryStatistic> statistics = new ArrayList<ChangedFeatureSetResultSummaryStatistic>();
	
}

@XStreamAlias("statistics")
class ChangedFeatureSetResultSummaryStatistic  {
	
	@XStreamAlias("Annotation type")
    public String annotation;
	
	@XStreamAlias("Count")
    public int count;
}



@XStreamAlias("feature")
class FeatureStatus extends BaseResult
{
    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type;
    
    @XStreamAlias("geneuniquename")
    @XStreamAsAttribute
    public String geneuniquename;
    
    @XStreamAlias("mrnauniquename")
    @XStreamAsAttribute
    public String mrnauniquename;
    
    @XStreamAlias("transcriptuniquename")
    @XStreamAsAttribute
    public String transcriptuniquename;

    @XStreamAlias("changedate")
    @XStreamAsAttribute
    public String changedate;
    
    @XStreamAlias("changedetail")
    @XStreamAsAttribute
    public String changedetail;

}




@XStreamAlias("error")
class ErrorModel
{
    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type = ErrorType.MISC.toString();

    @XStreamAlias("code")
    @XStreamAsAttribute
    public int code = ErrorType.MISC.ordinal();
    
    @XStreamImplicit(itemFieldName="message")
    private List<String> msgs = new ArrayList<String>();
    
    public ErrorModel() {
    	//
    }
    
    public ErrorModel(String message) {
    	addMessage(message);
    }
    
    public void addMessage(String msg)
    {
        msgs.add(msg);
    }
}

class RestException extends Exception {
	
	public ErrorModel model = new ErrorModel();;
	
	public RestException(ErrorType type, String[] messages) {
		
		model.type = type.toString();
		model.code = type.ordinal() + 1;
		for (String message : messages) {
			model.addMessage(message);
		}
	}
	
	public RestException(ErrorType type, String message) {
		model.type = type.toString();
		model.code = type.ordinal() + 1;
		model.addMessage(message);
	}
	
}

