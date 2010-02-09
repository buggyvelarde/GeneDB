package org.genedb.web.mvc.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private static final String DEFAULT_GROUP = HOMEPAGE + "group";

    private static final String APP_PREFIX = "app_www_homePage_";

    protected final Logger logger = Logger.getLogger(HomepageController.class);


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView displayRootHomepage(HttpServletRequest request) throws Exception {
        TaxonNodeList root = new TaxonNodeList();
        root.add(getTaxonNodeManager().getTaxonNodeForLabel("Root"));
        return displayHomepage(request, root);
    }

    @RequestMapping(method = RequestMethod.GET, value="/{originalOrg}")
    public ModelAndView displayHomepage(HttpServletRequest request, @PathVariable("originalOrg") TaxonNodeList originalOrg) throws Exception {

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

        if (node.isRoot()) {
            return new ModelAndView(DEFAULT_HOMEPAGE);
        }

        if (node.isLeaf()) {
            mav.setViewName(DEFAULT_SINGLE);
            mav.addObject("node", node);
            mav.addObject("label", node.getName(TaxonNameType.LABEL));
            mav.addObject("full", node.getName(TaxonNameType.HTML_FULL));
            
            return mav;
        }
        
        return mav;
    }

}

