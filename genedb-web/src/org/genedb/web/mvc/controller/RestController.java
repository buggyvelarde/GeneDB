package org.genedb.web.mvc.controller;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.DateAndTypeQuery;
import org.genedb.querying.tmpquery.DateCountQuery;
import org.genedb.querying.tmpquery.DateQuery;
import org.gmod.schema.mapped.Organism;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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
@RequestMapping("/rest/")
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
	
	private final String viewName = "serviceView";
	
	@RequestMapping(method=RequestMethod.GET, value={"/test", "/test.*"})
	public ModelAndView test(HttpServletRequest request, HttpServletResponse response)
	{
		ModelAndView mav = new ModelAndView("rest");
		mav.addObject("hello", "world");
		return mav;
	}
	
	
	
	/**
     * Returns a list of genomes, with the number of changed features for each one.
     * 
     * @param request
     * @param response
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(method=RequestMethod.GET, value={"/genomes/changes", "/genomes/changes.*"})
    public ModelAndView getGenomesStatus(
    		HttpServletRequest request, 
            HttpServletResponse response)
    {
    	DateCountQuery dateCountQuery = (DateCountQuery) applicationContext.getBean("dateCountQuery", DateCountQuery.class);
    	
    	String since = request.getParameter("since");
    	Date sinceDate = Calendar.getInstance().getTime();
    	try {
			sinceDate = getDateFromString(since);
		} catch (ParseException e) {
			return new ErrorReport("serviceView", ErrorType.MISSING_PARAMETER, "Please supply a date as 'yyyy-mm-dd'.");
		}
		
		dateCountQuery.setDate(sinceDate);
		dateCountQuery.setAfter(true);
		dateCountQuery.setCreated(false);
		
		List<TaxonNode> taxonList = getAllTaxons();
    	
		ResultSet rs = new ResultSet();
		rs.since = sinceDate.toString();
		rs.name = "genomes/changes";
		
		OrganismStatusList oList = new OrganismStatusList();
		rs.addResult(oList);
		
		for (TaxonNode taxon : taxonList)
		{
			if (! taxon.isOrganism())
				continue;
			
			logger.debug(taxon);
			
			TaxonNode[] taxons = {taxon};
			dateCountQuery.setTaxons( taxons );
			
			List<Object> results = new ArrayList<Object>();
			try {
				results = (List<Object>) dateCountQuery.getResults();
			} catch (QueryException e) {
				return new ErrorReport(viewName, ErrorType.QUERY_FAILURE, "The query for " + taxon.getLabel()  + " has failed.");
			}
			
			String taxonomyID = taxon.getTaxonId();
			String extension = getExtension(request);
			String url = "http://" + request.getServerName() + ":8080/rest/genome/changes"+ extension + "?taxonomyID=" + taxonomyID;
			
			if ((since!= null) && (since.length() > 0))
				url += "&since=" + since ;
			
			OrganismStatus os = new OrganismStatus();
			os.features_changed = Integer.parseInt(results.get(0).toString());
			os.name = taxon.getName(TaxonNameType.FULL);
			os.taxonomyID = taxonomyID;
			os.url = url;
			
			oList.addOrganism(os);
			
		}
		
    	ModelAndView mav = new ModelAndView(viewName);
    	mav.addObject(rs);
        return mav;
    }
    
    
    /**
     * Returns all features changed since a certain date, as determined by the DateQuery.
     * 
     * @param request
     * @param response
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping("/genome/changes")
    public ModelAndView genomeStatusByTaxonomyID( 
    		HttpServletRequest request, 
            HttpServletResponse response) 
    {
    	
    	String taxonomyID = request.getParameter("taxonomyID");
    	if ((taxonomyID == null) || (taxonomyID.length() == 0 ))
    		return new ErrorReport(viewName, ErrorType.MISSING_PARAMETER, "please supply a taxonomyID");
    	
    	TaxonNode taxon = getTaxonFromTaxonomyID(taxonomyID);
    	if (taxon == null)
    		return new ErrorReport(viewName, ErrorType.NO_RESULT, "The taxonomyID " + taxonomyID + " does not match any organism.");
    	
    	TaxonNode[] taxons = {taxon};
    	
    	
    	String since = request.getParameter("since");
    	Date sinceDate = Calendar.getInstance().getTime();
    	try {
			sinceDate = getDateFromString(since);
		} catch (ParseException e) {
			return new ErrorReport(viewName, ErrorType.MISSING_PARAMETER, "Please supply a date as 'yyyy-mm-dd'.");
		}
		
		DateAndTypeQuery dateQuery = (DateAndTypeQuery) applicationContext.getBean("dateAndTypeQuery", DateQuery.class); 
    	dateQuery.setDate(sinceDate);
        dateQuery.setAfter(true);
        dateQuery.setCreated(false);
        dateQuery.setTaxons( taxons );
    	
        List<Object> results = new ArrayList<Object>();
		try {
			results = (List<Object>) dateQuery.getResults();
		} catch (Exception e) {
			return new ErrorReport(viewName, ErrorType.QUERY_FAILURE, "The query has failed : " + e.getMessage());
		}
        int count = results.size();
        
        OrganismSetResult rs = new OrganismSetResult();
		rs.since = sinceDate.toString();
		rs.name = "genome/changes";
		rs.taxonomyID = taxonomyID;
		rs.count = count;
		
		// remember to cast the result Objects to Object[]s
		// Java can't tell these are lists until you force 
		// the cast
		for (Object result : results)
		{
			Object[] resultArray = (Object[]) result;
			FeatureStatus fs = new FeatureStatus();
			fs.id = (String) resultArray[0];
			fs.type = (String) resultArray[1];
			rs.addResult(fs);
		}
		
		
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject(rs);
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
    
    /**
     * Generates and appropriate extension based on the existing http request.
     * 
     * @param request
     * @return
     */
    private String getExtension(HttpServletRequest request)
    {
    	String extension = "";
		if (request.getRequestURI().contains(".json"))
		{
			extension = ".json";
		} else if (request.getRequestURI().contains(".xml"))
		{
			extension = ".xml";
		}
		return extension;
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
	QUERY_FAILURE
}


/**
 * 
 * Extends ModelAndView for generating a friendly error message.
 * 
 * @author gv1
 *
 */
class ErrorReport extends ModelAndView {
	
	public ErrorReport(String viewName, ErrorType errorType, String[] messages)
	{
		super(viewName);
		ErrorModel em = new ErrorModel();
		em.type = errorType.toString().toLowerCase();
		em.code = errorType.ordinal() + 1;
		for (String message : messages)
		{
			em.addMessage(message);
		}
		addObject(em);
	}
	
	public ErrorReport(String viewName, ErrorType errorType, String message)
	{
		this(viewName, errorType, new String[] {message});
	}
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
class ResultSet extends BaseResult
{
	@XStreamAlias("since")
	@XStreamAsAttribute
	public String since;
	
	@XStreamImplicit()
	private List<BaseResult> results = new ArrayList<BaseResult>();
	
	public void addResult(BaseResult br)
	{
		results.add(br);
	}
}

@XStreamAlias("result")
class OrganismSetResult extends ResultSet
{
	@XStreamAlias("count")
	@XStreamAsAttribute
	public int count;
	
	@XStreamAlias("taxonomyID")
	@XStreamAsAttribute
	public String taxonomyID;
}

@XStreamAlias("organism")
class OrganismStatus extends BaseResult
{
	@XStreamAlias("changed")
	@XStreamAsAttribute
	public int features_changed;
	
	@XStreamAlias("url")
	@XStreamAsAttribute
	public String url;
	
	@XStreamAlias("taxonomyID")
	@XStreamAsAttribute
	public String taxonomyID;
	
}

@XStreamAlias("organisms")
class OrganismStatusList extends BaseResult
{
	@XStreamImplicit(itemFieldName="organism")
	private List<OrganismStatus> oStatuses = new ArrayList<OrganismStatus>();
	
	public void addOrganism(OrganismStatus os)
	{
		oStatuses.add(os);
	}
}

@XStreamAlias("feature")
class FeatureStatus extends BaseResult
{
	@XStreamAlias("type")
	@XStreamAsAttribute
	public String type;
	
	@XStreamAlias("id")
	@XStreamAsAttribute
	public String id;
}



@XStreamAlias("error")
class ErrorModel
{
	@XStreamAlias("type")
	@XStreamAsAttribute
	public String type;
	
	@XStreamAlias("code")
	@XStreamAsAttribute
	public int code;
	
	@XStreamImplicit(itemFieldName="message")
	private List<String> msgs = new ArrayList<String>();
	
	public void addMessage(String msg)
	{
		msgs.add(msg);
	}
}

