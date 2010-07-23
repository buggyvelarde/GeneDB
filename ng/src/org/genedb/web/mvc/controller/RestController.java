package org.genedb.web.mvc.controller;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.ChangedGeneFeaturesQuery;
import org.genedb.querying.tmpquery.DateCountQuery;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.QuickSearchQuery;
import org.genedb.querying.tmpquery.SuggestQuery;
import org.genedb.querying.tmpquery.QuickSearchQuery.QuickSearchQueryResults;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureCvTermProp;
import org.gmod.schema.mapped.FeatureRelationship;
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
import java.util.Collection;
import java.util.Date;
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
@RequestMapping("/service/")
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

    private final String viewName = "json:";

    @RequestMapping(method=RequestMethod.GET, value={"/test", "/test.*"})
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response)
    {
        ModelAndView mav = new ModelAndView(viewName);
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
    public ModelAndView getGenomesStatus(@RequestParam("since") String since)
    {
        DateCountQuery dateCountQuery = (DateCountQuery) applicationContext.getBean("dateCount", DateCountQuery.class);

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

        ChangedOrganismSetResult rs = new ChangedOrganismSetResult();
        rs.since = sinceDate.toString();
        rs.name = "genomes/changes";

        for (TaxonNode taxon : taxonList)
        {
            if (! taxon.isOrganism())
                continue;

            logger.debug(taxon);

            TaxonNodeList taxons = new TaxonNodeList(taxon);
            dateCountQuery.setTaxons( taxons );


            List<Object> results = new ArrayList<Object>();
            try {
                results = (List<Object>) dateCountQuery.getResults();
            } catch (QueryException e) {
                return new ErrorReport(viewName, ErrorType.QUERY_FAILURE, "The query for " + taxon.getLabel()  + " has failed.");
            }

            String taxonomyID = taxon.getTaxonId();

            OrganismStatus os = new OrganismStatus();
            os.features_changed = Integer.parseInt(results.get(0).toString());
            os.name = taxon.getName(TaxonNameType.FULL);
            os.taxonomyID = taxonomyID;

            rs.addResult(os);

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

    @RequestMapping(method=RequestMethod.GET, value={"/genome/changes", "/genome/changes.*"})
    public ModelAndView genomeStatusByTaxonomyID(@RequestParam("since") String since,  @RequestParam("taxonomyID") String taxonomyID)
    {

        if ((taxonomyID == null) || (taxonomyID.length() == 0 ))
            return new ErrorReport(viewName, ErrorType.MISSING_PARAMETER, "please supply a taxonomyID");

        TaxonNode taxon = getTaxonFromTaxonomyID(taxonomyID);
        if (taxon == null)
            return new ErrorReport(viewName, ErrorType.NO_RESULT, "The taxonomyID " + taxonomyID + " does not match any organism.");

        TaxonNode[] taxons = {taxon};


        Date sinceDate = Calendar.getInstance().getTime();
        try {
            sinceDate = getDateFromString(since);
        } catch (ParseException e) {
            return new ErrorReport(viewName, ErrorType.MISSING_PARAMETER, "Please supply a date as 'yyyy-mm-dd'.");
        }

        Organism o = organismDao.getOrganismByCommonName(taxons[0].getLabel());

        ChangedGeneFeaturesQuery changedGeneFeaturesQuery = (ChangedGeneFeaturesQuery) applicationContext.getBean("changedGeneFeatures", ChangedGeneFeaturesQuery.class);
        changedGeneFeaturesQuery.setDate(sinceDate);
        changedGeneFeaturesQuery.setOrganismId(o.getOrganismId());

        final ChangedFeatureSetResult organismSetResult = new ChangedFeatureSetResult();
        organismSetResult.since = sinceDate.toString();
        organismSetResult.name = "genome/changes";
        organismSetResult.taxonomyID = taxonomyID;
        organismSetResult.count = 0;


        changedGeneFeaturesQuery.processCallBack(new RowCallbackHandler(){
            public void processRow(ResultSet rs) throws SQLException {
                FeatureStatus fs = new FeatureStatus();

                fs.id = rs.getObject("id").toString();
                fs.uniquename = rs.getObject("uniquename").toString();
                fs.type = rs.getObject("type").toString();
                fs.timelastmodified = rs.getObject("time").toString();
                fs.rootID = rs.getObject("rootid").toString();
                fs.rootUniquename = rs.getObject("rootname").toString();
                fs.rootType = rs.getObject("roottype").toString();

                organismSetResult.addResult(fs);
                organismSetResult.count += 1;
            }
        });


        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject(organismSetResult);
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
    @RequestMapping(value={"/feature/", "/feature.*" })
    public ModelAndView getFeature( @RequestParam("uniqueName") String uniqueName )
    {

        RestResultSet result = new RestResultSet();
        result.name = "/feature";

        Feature feature = sequenceDao.getFeatureByUniqueName(uniqueName, Feature.class);

        Organism org = feature.getOrganism();

        Collection<FeatureCvTerm> fcv = feature.getFeatureCvTerms();

        FeatureSummary fsum = new FeatureSummary();

        result.addResult(fsum);

        OrganismSummary osum = new OrganismSummary();
        osum.genus = org.getGenus();
        osum.abbreviation = org.getAbbreviation();
        osum.commonName = org.getCommonName();
        osum.species = org.getSpecies();
        osum.taxonomyID = org.getPropertyValue("genedb_misc", "taxonId");

        fsum.isAnalysis = feature.isAnalysis();
        fsum.isObsolete = feature.isObsolete();
        fsum.id = Integer.toString(feature.getFeatureId());
        fsum.uniquename = feature.getUniqueName();
        fsum.organism = osum;

        Date timelastaccessioned = feature.getTimeAccessioned();
        Date timelastmodified = feature.getTimeLastModified();

        if (timelastaccessioned != null)
            fsum.timelastaccessioned = timelastaccessioned.toString();

        if (timelastmodified != null)
            fsum.timelastmodified = timelastmodified.toString();

        fsum.type = feature.getType().getName();

        for (FeatureRelationship relationship : feature.getFeatureRelationshipsForSubjectId())
        {
            Feature child = relationship.getObjectFeature();
            LinkedFeatureSummary csum = new LinkedFeatureSummary();
            csum.id = Integer.toString(child.getFeatureId());
            csum.uniquename = child.getUniqueName();
            csum.type = child.getType().getName();
            csum.relationship = relationship.getType().getName();
            fsum.parents.add(csum);
        }

        for (FeatureRelationship relationship : feature.getFeatureRelationshipsForObjectId())
        {
            Feature parent = relationship.getSubjectFeature();
            LinkedFeatureSummary psum = new LinkedFeatureSummary();
            psum.id = Integer.toString(parent.getFeatureId());
            psum.uniquename = parent.getUniqueName();
            psum.type = parent.getType().getName();
            psum.relationship = relationship.getType().getName();
            fsum.children.add(psum);
        }

        for (FeatureCvTerm  cv : fcv)
        {

            List<FeatureCvTermProp> props = cv.getFeatureCvTermProps();
            CVTermSummary csum = new CVTermSummary();

            for (FeatureCvTermProp prop : props)
            {
                String type = prop.getType().getName();
                String value = prop.getValue();

                CVTermPropSummary psum = new CVTermPropSummary();
                psum.type = type;
                psum.value = value;

                csum.props.add(psum);
            }

            csum.name = cv.getCvTerm().getName();
            fsum.cvterms.add(csum);
        }

        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject(result);
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

    	QuickSearchQuery query = (QuickSearchQuery) applicationContext.getBean("quickSearch", QuickSearchQuery.class);
    	query.setSearchText(term);

    	query.setAllNames(true);
    	query.setProduct(true);
    	query.setPseudogenes(true);


    	TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
    	TaxonNode taxonNode = tnm.getTaxonNodeForLabel(taxon);
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

        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("model", qsr);
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
class ChangedOrganismSetResult extends RestResultSet
{


    @XStreamAlias("since")
    @XStreamAsAttribute
    public String since;
}

@XStreamAlias("organism")
class OrganismStatus extends BaseResult
{
    @XStreamAlias("changed")
    @XStreamAsAttribute
    public int features_changed;

    @XStreamAlias("taxonomyID")
    @XStreamAsAttribute
    public String taxonomyID;

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

    @XStreamAlias("uniquename")
    @XStreamAsAttribute
    public String uniquename;

    @XStreamAlias("rootUniquename")
    @XStreamAsAttribute
    public String rootUniquename;

    @XStreamAlias("rootID")
    @XStreamAsAttribute
    public String rootID;

    @XStreamAlias("rootType")
    @XStreamAsAttribute
    public String rootType;

    @XStreamAlias("timelastmodified")
    @XStreamAsAttribute
    public String timelastmodified;

}



@XStreamAlias("feature")
class FeatureSummary extends BaseResult
{
    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type;

    @XStreamAlias("id")
    @XStreamAsAttribute
    public String id;

    @XStreamAlias("uniquename")
    @XStreamAsAttribute
    public String uniquename;

    @XStreamAlias("timelastmodified")
    @XStreamAsAttribute
    public String timelastmodified;

    @XStreamAlias("timelastaccessioned")
    @XStreamAsAttribute
    public String timelastaccessioned;

    @XStreamAlias("residues")
    public String residues;

    @XStreamAlias("isAnalysis")
    @XStreamAsAttribute
    public boolean isAnalysis;

    @XStreamAlias("isObsolete")
    @XStreamAsAttribute
    public boolean isObsolete;

    @XStreamAlias("organism")
    public OrganismSummary organism;

    @XStreamAlias("children")
    public List<LinkedFeatureSummary> children = new ArrayList<LinkedFeatureSummary>();

    @XStreamAlias("parents")
    public List<LinkedFeatureSummary> parents = new ArrayList<LinkedFeatureSummary>();

    @XStreamAlias("cvterms")
    public List<CVTermSummary> cvterms = new ArrayList<CVTermSummary>();

}

@XStreamAlias("feature")
class LinkedFeatureSummary extends BaseResult
{
    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type;

    @XStreamAlias("id")
    @XStreamAsAttribute
    public String id;

    @XStreamAlias("uniquename")
    @XStreamAsAttribute
    public String uniquename;

    @XStreamAlias("relationship")
    @XStreamAsAttribute
    public String relationship;
}

@XStreamAlias("cvterm")
class CVTermSummary extends BaseResult
{

    @XStreamAlias("props")
    public List<CVTermPropSummary> props = new ArrayList<CVTermPropSummary>();
}

@XStreamAlias("cvtermprop")
class CVTermPropSummary extends BaseResult
{
    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type;

    @XStreamAlias("value")
    @XStreamAsAttribute
    public String value;
}

@XStreamAlias("organism")
class OrganismSummary extends BaseResult
{
    @XStreamAlias("abbreviation")
    @XStreamAsAttribute
    public String abbreviation;

    @XStreamAlias("genus")
    @XStreamAsAttribute
    public String genus;

    @XStreamAlias("species")
    @XStreamAsAttribute
    public String species;

    @XStreamAlias("commonName")
    @XStreamAsAttribute
    public String commonName;

    @XStreamAlias("taxonomyID")
    @XStreamAsAttribute
    public String taxonomyID;
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

