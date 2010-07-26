package org.genedb.web.mvc.controller.download;

import java.util.List;

import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.core.QueryDetails;
import org.genedb.querying.core.QueryFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/QueryList")
public class QueryListController {

    //@Autowired
    private QueryFactory queryFactory;

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm(
            @RequestParam(value="filter", required=false) String filterName,
            Model model) {

        List<QueryDetails> queryDetails = queryFactory.listQueries(filterName, NumericQueryVisibility.PUBLIC );
        model.addAttribute("queries", queryDetails);
        return "list/queryList";
    }

}
