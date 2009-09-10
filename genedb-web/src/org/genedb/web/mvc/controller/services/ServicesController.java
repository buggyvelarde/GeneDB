package org.genedb.web.mvc.controller.services;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.DateCountQuery;
import org.genedb.querying.tmpquery.DateQuery;
import org.genedb.web.mvc.controller.services.ErrorReport.ErrorType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.Organism;

/**
 * 
 * A 'controller' with method stubs mapping to for handling REST service requests. 
 * @author gv1
 *
 */
@Controller
@RequestMapping("/Services")
public class ServicesController 
{
    private static final Logger logger = Logger.getLogger(ServicesController.class);
    private final String viewName = "serviceView";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("organismDao")
    OrganismDao organismDao;
    
    @Autowired
    @Qualifier("sequenceDao")
    SequenceDao sequenceDao;
    
    /**
     * 
     * Returns a list of organisms or 'genomes', with taxonomyIDs.
     * 
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/genomes/list")
	public ModelAndView organisms(HttpServletRequest request, HttpServletResponse response)
    {
        logger.debug(organismDao);
        
        List<Organism> organisms = organismDao.getOrganisms();
        GenericObject genericOrganisms = new GenericObject("organisms");
        
        for (Organism organism : organisms)
        {
            logger.debug(organism);
            
            if (! organism.isPopulated())
                continue;
            
            GenericObject genericOrganism = new GenericObject("organism");
            
            genericOrganism.add(new GenericObject("genus", organism.getGenus()));
            genericOrganism.add(new GenericObject("species", organism.getSpecies()));
            genericOrganism.add(new GenericObject("taxonomyid", organism.getPropertyValue("genedb_misc", "taxonId")));
            
            genericOrganisms.add(genericOrganism);
            
        }
        
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        
        logger.info("Returning organism list");
        
        ModelAndView mav = new ModelAndView(viewName);
        
        mav.addObject(new GenericObject("api_version", "1"));
        mav.addObject(new GenericObject("data_provider", "GeneDB"));
        mav.addObject(new GenericObject("data_version", "GDB2009_08"));
        
        mav.addObject(genericOrganisms);
        
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
	@RequestMapping("/genomes/changes")
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
    	
		GenericObject genericOrganisms = new GenericObject("organisms");
		
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
				return new ErrorReport("serviceView", ErrorType.QUERY_FAILURE, "The query for " + taxon.getLabel()  + " has failed.");
			}
			
			GenericObject organism = new GenericObject("organism");
			
			organism.add(new GenericObject("organism", taxon.getName(TaxonNameType.FULL)));
			organism.add(new GenericObject("features_changed", results.get(0)));
			
			String taxonomyID = taxon.getTaxonId();
			organism.add(new GenericObject("taxonomyID", taxonomyID));
			
			String extension = getExtension(request);
			
			String url = "http://" + request.getServerName() + ":8080/Services/genome/changes"+ extension + "?taxonomyID=" + taxonomyID + "&since=" + since ;
			
			organism.add(new GenericObject("url", url));
			
			genericOrganisms.add(organism);
			
		}
		
    	ModelAndView mav = new ModelAndView("serviceView");
    	mav.addObject(genericOrganisms);
        return mav;
    }
    
    
    
    /**
     * Returns all features changed since a certain date.
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
    		return new ErrorReport("serviceView", ErrorType.MISSING_PARAMETER, "please supply a taxonomyID");
    	
    	TaxonNode taxon = getTaxonFromTaxonomyID(taxonomyID);
    	if (taxon == null)
    		return new ErrorReport("serviceView", ErrorType.NO_RESULT, "The taxonomyID " + taxonomyID + " does not match any organism.");
    	
    	TaxonNode[] taxons = {taxon};
    	
    	
    	String since = request.getParameter("since");
    	Date sinceDate = Calendar.getInstance().getTime();
    	try {
			sinceDate = getDateFromString(since);
		} catch (ParseException e) {
			return new ErrorReport("serviceView", ErrorType.MISSING_PARAMETER, "Please supply a date as 'yyyy-mm-dd'.");
		}
		
    	DateQuery dateQuery = (DateQuery) applicationContext.getBean("dateQuery", DateQuery.class); 
    	dateQuery.setDate(sinceDate);
        dateQuery.setAfter(true);
        dateQuery.setCreated(false);
        dateQuery.setTaxons( taxons );
    	
        List<Object> results = new ArrayList<Object>();
		try {
			results = (List<Object>) dateQuery.getResults();
		} catch (QueryException e) {
			return new ErrorReport("serviceView", ErrorType.QUERY_FAILURE, "The query has failed.");
		}
        int count = results.size();
        
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("feature", (String[]) results.toArray(new String[0]));
        mav.addObject("count", count);
        return mav;
    }
    
    
    /**
     * 
     *  Returns all the features modified since a certain date.
     *  
     * @param request
     * @param response
     * @param genusAndSpecies
     * @param year
     * @param month
     * @param day
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping("/features/{genusAndSpecies}/{day}/{month}/{year}")
    public ModelAndView featuresModifiedSince(
        HttpServletRequest request, 
        HttpServletResponse response, 
        @PathVariable("genusAndSpecies") String genusAndSpecies,
        @PathVariable("year") int year,
    	@PathVariable("month") int month,
        @PathVariable("day") int day)
    {
    	
        DateQuery dateQuery = (DateQuery) applicationContext.getBean("dateQuery", DateQuery.class); //(DateQuery) qFact.retrieveQuery("dateQuery");
        logger.info(dateQuery);
        
        logger.info("genusAndSpecies :: " + genusAndSpecies);
        
        
        SimpleDateFormat df1 = new SimpleDateFormat( "dd/MM/yy" );
        Date d;
		try {
			d = df1.parse( day + "/" + month + "/" + year );
		} catch (ParseException e) {
			return new ErrorReport("serviceView", ErrorType.INVALID_PARAMETER, "Please supply a date in dd/mm/yy format.");
		}
        
        logger.info(d);
        
        dateQuery.setDate(d);
        dateQuery.setAfter(true);
        dateQuery.setCreated(false);
        
        List<TaxonNode> taxons = new ArrayList<TaxonNode>();
        TaxonNode taxon = getTaxonFromID(genusAndSpecies);
        taxons.add(taxon);
        if (taxon != null)
        {
            logger.info("using a taxon :: " + taxon.getTaxonId());
            dateQuery.setTaxons( (TaxonNode[]) taxons.toArray( new TaxonNode[0] ) );
        } else
        {
            logger.warn("could not find a taxon!");
        }
        
        List<Object> results = new ArrayList<Object>();
		try {
			results = (List<Object>) dateQuery.getResults();
		} catch (QueryException e) {
			return new ErrorReport("serviceView", ErrorType.QUERY_FAILURE, "The query has failed.");
		}
        int count = results.size();
        
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("feature", (String[]) results.toArray(new String[0]));
        mav.addObject("count", count);
        return mav;
        
    }
    
    /**
     * 
     * Returns a some details for a feature.
     * 
     * @param request
     * @param response
     * @param uniqueName
     * @return
     */
	@RequestMapping("/feature/{uniqueName}")
    public ModelAndView getFeature(
        HttpServletRequest request, 
        HttpServletResponse response, 
        @PathVariable("uniqueName") String uniqueName)
    {
        
        Feature feature = sequenceDao.getFeatureByUniqueName(uniqueName, Feature.class);
        
        Collection<FeatureCvTerm> fcv = feature.getFeatureCvTerms();
        
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("Name",feature.getName());
        mav.addObject("Residues",feature.getResidues());
        mav.addObject("isAnalysis",feature.isAnalysis());
        mav.addObject("isObsolete",feature.isObsolete());
        mav.addObject("TimeAccessioned",feature.getTimeAccessioned());
        mav.addObject("TimeLastModified",feature.getTimeLastModified());
        
        mav.addObject("CVTerms", fcv.toArray());
        return mav;
        
    }
    
	
	
	
	
	
    
    /*
     * Private utilities...
     */
	
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