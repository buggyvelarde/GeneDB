package org.genedb.web.mvc.controller;

import org.genedb.db.loading.TaxonNode;
import static org.genedb.web.mvc.controller.WebConstants.CRUMB;
import static org.genedb.web.mvc.controller.WebConstants.TAXON_NODE;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.ArrayList;
import java.util.HashMap;
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

	/**
	 * Custom handler for homepage
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@SuppressWarnings("unchecked")
    @Override
    // FIXME Change to PostOrGetFormController and use DataBinder
	public ModelAndView handleRequestInternal(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
	        List<String> answers = new ArrayList<String>();
            String viewName = DEFAULT_HOMEPAGE;
            Map model = new HashMap();
	        if (WebUtils.extractTaxonNodesFromRequest(request, answers, false, true)) {
	            if (answers.size() > 0) {
	                Taxon taxon = TaxonUtils.getTaxonFromList(answers, 0);
	                return new ModelAndView(HOMEPAGE + taxon.getHomepageViewName(), "taxon", taxon);
	            }
	        }
            List<NewsItem> news = checkNews();
            if (news.size() > 0) {
                model.put("news", news);
            }
            
            TaxonNode node = new TaxonNode(null); // FIXME should have from above
            model.put(TAXON_NODE, node);
            model.put(CRUMB, "Homepage");
	        return new ModelAndView(viewName, model);
	}

    private List<NewsItem> checkNews() {
        return new ArrayList<NewsItem>(0);
    }
    
}
