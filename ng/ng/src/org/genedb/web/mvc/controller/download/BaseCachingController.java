package org.genedb.web.mvc.controller.download;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;
import com.sleepycat.collections.StoredMap;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNode;
//import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.TaxonQuery;
import org.genedb.web.mvc.controller.WebConstants;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


public class BaseCachingController {

//
//        switch (results.size()) {
//        case 0:
//            logger.debug("No results found for query");
//            model.addAttribute("taxonNodeName", taxonName);
//            return "search/"+queryName;
//        case 1:
//            List<GeneSummary> gs = possiblyConvertList(results);
//            resultsKey = cacheResults(gs, query, queryName, session.getId());
//            return "redirect:/NamedFeature?name=" + gs.get(0).getSystematicId();
//        default:
//            List<GeneSummary> gs2 = possiblyConvertList(results);
//            resultsKey = cacheResults(gs2, query, queryName, session.getId());
//            model.addAttribute("key", resultsKey);
//            model.addAttribute("taxonNodeName", taxonName);
//            logger.debug("Found results for query (Size: '"+gs2.size()+"' key: '"+resultsKey+"')- redirecting to Results controller");
//            return "redirect:/Results";
//        }
//    }


    //@Autowired
    private ResultsCacheFactory resultsCacheFactory;

    public void setResultsCacheFactory(ResultsCacheFactory resultsCacheFactory) {
        this.resultsCacheFactory = resultsCacheFactory;
    }


    protected List<GeneSummary> possiblyConvertList(List results) {
        List<GeneSummary> gs;
        Object firstItem =  results.get(0);
        if (firstItem instanceof GeneSummary) {
            gs = results;
        } else {
            gs = Lists.newArrayListWithExpectedSize(results.size());
            for (Object o  : results) {
                gs.add(new GeneSummary((String) o));
            }
        }
        return gs;
    }


    protected String cacheResults(List<GeneSummary> gs, Query q, String queryName, String sessionId) {
        String key = sessionId + Integer.toString(System.identityHashCode(gs)); // CHECKME
        StoredMap<String, ResultEntry> map = resultsCacheFactory.getResultsCacheMap();
        ResultEntry re = new ResultEntry();
        re.numOfResults = gs.size();
        re.query = q;
        re.results = gs;
        re.queryName = queryName;
        map.put(key, re);
        return key;
    }

}
