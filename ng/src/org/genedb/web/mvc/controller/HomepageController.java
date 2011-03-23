package org.genedb.web.mvc.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.core.QueryDetails;
import org.genedb.querying.core.QueryFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;


/**
 * Controller for selecting and forwarding to a homepage view. It uses the one set
 * as a phylonode property, otherwise a default one. An error, or no arguments,
 * returns the default homepage.
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/Homepage")
public class HomepageController extends BaseController {

    private static String HOMEPAGE = "jsp:homepages/";
    private static final String DEFAULT_HOMEPAGE = HOMEPAGE + "frontPage";
    private static final String DEFAULT_SINGLE = HOMEPAGE + "singleOrg";
    private static final String DEFAULT_SINGLE_CHROMSOME_MAP = HOMEPAGE + "chromosomeMap";
    private static final String DEFAULT_GROUP = HOMEPAGE + "group";

    private static final String APP_PREFIX = "app_www_homePage_";

    protected final Logger logger = Logger.getLogger(this.getClass());
    
    private QueryFactory queryFactory;

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setConversionService(conversionService);
        //binder.getFormatterRegistry().addFormatterByType(taxonNodeListFormatter); //TODO
    }


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView displayRootHomepage(HttpServletRequest request) throws Exception {
        TaxonNodeList root = new TaxonNodeList();
        root.add(getTaxonNodeManager().getTaxonNodeForLabel("Root"));
        return displayHomepage(request, root, null);
    }

    @RequestMapping(method = RequestMethod.GET, value="/{originalOrg}")
    public ModelAndView displayHomepage(HttpServletRequest request, 
    		@PathVariable("originalOrg") TaxonNodeList originalOrg,
    		@RequestParam(value="region", required=false) String region) throws Exception {

        if (originalOrg.getNodes().size() != 1) {
            return new ModelAndView(DEFAULT_HOMEPAGE);
        }

        TaxonNode node = originalOrg.getNodes().get(0); // Ignore more than one taxon request and use the first

        Map<String, String> map = Maps.newHashMap();

        for (Map.Entry<String, String> entry : node.getAppDetails().entrySet()) {
            if (entry.getKey().startsWith(APP_PREFIX)) {
                map.put(entry.getKey().substring(APP_PREFIX.length()), entry.getValue());
            }
        }

        // gv1
        // using the context path to replace "${baseUrl}" occurances in the home page text.
        // these modifications need to done before instantiating the ModelAndView
        String path = request.getContextPath() + "/";
        if (map.containsKey("content")) {
            map.put("content", map.get("content").replace("${baseUrl}", path));
        }
        if (map.containsKey("links")) {
            map.put("links", map.get("links").replace("${baseUrl}", path));
        }

        ModelAndView mav = new ModelAndView(DEFAULT_GROUP, map);
        mav.addObject("organismContext", node.getLabel());
        mav.addObject("taxonNodeName", node.getLabel());
        mav.addObject("showingHomepage", true);

        if (node.isRoot()) {
            return new ModelAndView(DEFAULT_HOMEPAGE);
        }

        if (node.isLeaf()) {
        	
        	if (region != null) {
        		mav.setViewName(DEFAULT_SINGLE_CHROMSOME_MAP);
        		mav.addObject("region", region);
        	} else {
        		
        		List<QueryDetails> queryDetails = queryFactory.listQueries("", NumericQueryVisibility.PUBLIC );
                mav.addObject("queries", queryDetails);
        		
        		mav.setViewName(DEFAULT_SINGLE);
        	}
        	
            mav.addObject("fulltext", node.getName(TaxonNameType.FULL));
            
            mav.addObject("node", node);
            mav.addObject("label", node.getName(TaxonNameType.LABEL));
            mav.addObject("full", node.getName(TaxonNameType.HTML_FULL));

            if (map.containsKey("app_www_homePage_content")) {
                mav.addObject("content", map.get("app_www_homePage_content"));
            }
            if (map.containsKey("app_www_homePage_links")) {
                mav.addObject("links", map.get("app_www_homePage_links"));
            }

            return mav;
        }

        //mav.addObject("label", node.getLabel());
        return mav;
    }

}

