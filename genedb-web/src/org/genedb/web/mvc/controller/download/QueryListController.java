package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.QueryDetails;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.core.QueryVisibility;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;



@Controller
@RequestMapping("/QueryList")
public class QueryListController {

    private static final Logger logger = Logger.getLogger(QueryListController.class);

    //@Autowired
    private QueryFactory queryFactory;

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm(
            @RequestParam(value="filter", required=false) String filterName,
            Model model) {
    	logger.info("fetching query list");
        
        try
        {
        	List<QueryDetails> queryDetails = queryFactory.listQueries(filterName, QueryVisibility.PUBLIC );
        	model.addAttribute("queries", queryDetails);
        } catch (Exception e)
        {
        	logger.error(e);
        	logger.error(e.getMessage());
        	e.printStackTrace();
        }
        
        logger.info("done?");
        logger.info(model.containsAttribute("queries"));
        
        return "list/queryList";
    }

}
