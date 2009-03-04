package org.genedb.web.mvc.controller;

import static org.genedb.web.mvc.controller.WebConstants.CRUMB;
import static org.genedb.web.mvc.controller.WebConstants.TAXON_NODE;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Controller for selecting and forwarding to a homepage view. It uses the one set
 * as a phylonode property, otherwise a default one. An error, or no arguments,
 * returns the default homepage.
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/Homepage")
public class HomepageController {

    public static final String DUMMY_UNKNOWN = "XXX";

    private static String HOMEPAGE = "homepages/";
    private static String DEFAULT_HOMEPAGE = HOMEPAGE + "topPage";
    private static String DEFAULT_STYLE = "childListing"; // FIXME

    private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;

    protected final Logger logger = Logger.getLogger(this.getClass());

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String originalOrg = ServletRequestUtils.getStringParameter(request, "organism", DUMMY_UNKNOWN);

//        TaxonNodeArrayHolder tnah = new TaxonNodeArrayHolder();
//
//        ServletRequestDataBinder binder = new ServletRequestDataBinder(tnah);
//        binder.initDirectFieldAccess();
//        binder.registerCustomEditor(TaxonNode[].class, taxonNodeArrayPropertyEditor);
//        binder.bind(request);
//        BindingResult bindResult = binder.getBindingResult();

        String organism = null;
        String pageName = null;

//        if (bindResult.hasGlobalErrors()) {
//          // TODO Add error message
//            logger.debug("Binding errors - go home");
//            return new ModelAndView(DEFAULT_HOMEPAGE);
//        }

//        TaxonNode[] nodes = tnah.org;
//        if (nodes == null || nodes.length == 0) {
//            logger.debug("No taxon nodes - go home");
//            return new ModelAndView(DEFAULT_HOMEPAGE);
//        }
//        if (nodes.length > 1) {
//            // TODO Add error message
//            logger.debug("Got too many taxon nodes");
//            return new ModelAndView(DEFAULT_HOMEPAGE);
//        }

//        TaxonNode node = nodes[0];
        String viewName = DEFAULT_HOMEPAGE;
        //String viewName = HOMEPAGE + DEFAULT_STYLE;
        if (!originalOrg.equals(DUMMY_UNKNOWN)) {
            pageName = originalOrg;
            organism = originalOrg;
        //pageName = node.getName(TaxonNameType.HTML_SHORT); // TODO Pass in taxon node
        //logger.warn(String.format("Retrieved a page name of '%s'", pageName));
        //organism = node.getName(TaxonNameType.LABEL);
        //logger.warn(String.format("Retrieved an organism name of '%s'", pageName));
        }

//        Map props = node.getAppDetails("WEB");
//        if (props.containsKey("HOMEPAGE_STYLE")) {
//            viewName = HOMEPAGE + props.get("HOMEPAGE_STYLE");
//        }

//      List<NewsItem> news = checkNews();
//      if (news.size() > 0) {
//          model.put("news", news);
//      }

        ModelAndView mav = new ModelAndView(viewName);
        //mav.addObject(TAXON_NODE, node);
        mav.addObject(CRUMB, "Homepage");
        if (organism != null) {
            mav.addObject("organism", organism);
        }
        if (pageName != null) {
            mav.addObject("pageName", pageName);
        } else {
            mav.addObject("pageName", "GeneDB");
        }

        return mav;
    }


    private List<NewsItem> checkNews(TaxonNode node) {
        return new ArrayList<NewsItem>(0);
    }

    public void setTaxonNodeArrayPropertyEditor(
            TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor) {
        this.taxonNodeArrayPropertyEditor = taxonNodeArrayPropertyEditor;
    }

}


/**
 * Simple struct to make databinding easier
 *
 * @author A. Tivey (art)
 */
class TaxonNodeArrayHolder {

    /**
     * Field, directly accessed by databinder and for reading
     */
    TaxonNode[] org;
}