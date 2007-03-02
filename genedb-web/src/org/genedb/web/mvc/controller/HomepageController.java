package org.genedb.web.mvc.controller;

import org.genedb.db.loading.TaxonNode;
import org.genedb.db.loading.TaxonNodeArrayPropertyEditor;

import static org.genedb.web.mvc.controller.WebConstants.CRUMB;
import static org.genedb.web.mvc.controller.WebConstants.TAXON_NODE;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 
 *
 * @author Adrian Tivey
 */
public class HomepageController extends AbstractController {

    private static String HOMEPAGE = "homepages/";
    private static String DEFAULT_HOMEPAGE = HOMEPAGE + "frontPage";
    private static String DEFAULT_STYLE = "wibble";
   
    private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;
    

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        TaxonNode[] nodes = new TaxonNode[1];
        
        ServletRequestDataBinder binder = new ServletRequestDataBinder(nodes);
        binder.registerCustomEditor(TaxonNode[].class, taxonNodeArrayPropertyEditor);
        binder.bind(request);
        //BindingResult br = binder.getBindingResult();
        
        if (nodes == null || nodes.length == 0) {
            return new ModelAndView(DEFAULT_HOMEPAGE);
        }
        if (nodes.length > 1) {
            // TODO Add error message
            System.err.println("Got too many taxon nodes");
            return new ModelAndView(DEFAULT_HOMEPAGE);
        }
        
        TaxonNode node = nodes[0];
        String viewName = HOMEPAGE + DEFAULT_STYLE;
        
        Map props = node.getAppDetails("WEB");
        if (props.containsKey("HOMEPAGE_STYLE")) {
            viewName = HOMEPAGE + props.get("HOMEPAGE_STYLE");
        }
        
//      List<NewsItem> news = checkNews();
//      if (news.size() > 0) {
//          model.put("news", news);
//      }
        
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject(TAXON_NODE, node);
        mav.addObject(CRUMB, "Homepage");  
        
        return mav;
    }


    private List<NewsItem> checkNews(TaxonNode node) {
        return new ArrayList<NewsItem>(0);
    }


    
}
