package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;


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

        Map<String, Query> queries = queryFactory.listQueries(filterName);
        Map<String, Query> results = new HashMap<String, Query>();
        for (Map.Entry<String, Query> entry : queries.entrySet()) {
            String key = StringUtils.delete(entry.getKey(), "Query");
            results.put(key, entry.getValue());
        }
        model.addAttribute("queries", results);
        return "list/queryList";
    }
}
