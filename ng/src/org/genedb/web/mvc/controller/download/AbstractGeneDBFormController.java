package org.genedb.web.mvc.controller.download;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.querying.tmpquery.QuickSearchQuery;
import org.genedb.querying.tmpquery.TaxonQuery;
import org.genedb.web.mvc.model.ResultsCacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.google.common.collect.Lists;
import com.sleepycat.collections.StoredMap;

public class AbstractGeneDBFormController {

    private Logger logger = Logger.getLogger(AbstractGeneDBFormController.class);

    //@Autowired
    private ResultsCacheFactory resultsCacheFactory;

    private ConversionService conversionService;

    //private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;

    public void setResultsCacheFactory(ResultsCacheFactory resultsCacheFactory) {
        this.resultsCacheFactory = resultsCacheFactory;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setConversionService(conversionService);
        //binder.setValidator(new FooValidator());
    }



//    public void setTaxonNodeArrayPropertyEditor(TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor) {
//        this.taxonNodeArrayPropertyEditor = taxonNodeArrayPropertyEditor;
//    }
//
//    //@Autowired
//    private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;

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
        String key = sessionId + ":"+ Integer.toString(System.identityHashCode(gs)); // CHECKME
        StoredMap<String, ResultEntry> map = resultsCacheFactory.getResultsCacheMap();
        ResultEntry re = new ResultEntry();
        re.numOfResults = gs.size();
        re.query = q;
        re.results = gs;
        re.queryName = queryName;
        if (q instanceof LuceneQuery){
            re.expanded = true;
        }
        map.put(key, re);
        return key;
    }

    protected String cacheResults(List<GeneSummary> gs, Query q, String queryName, TreeMap<String, Integer> taxonGroup, String sessionId) {
        String key = sessionId + ":"+ Integer.toString(System.identityHashCode(gs)); // CHECKME
        StoredMap<String, ResultEntry> map = resultsCacheFactory.getResultsCacheMap();
        ResultEntry re = new ResultEntry();
        re.numOfResults = gs.size();
        re.query = q;
        re.results = gs;
        re.queryName = queryName;
        if (q instanceof LuceneQuery){
            re.expanded = true;
        }
        if (taxonGroup != null) {
            re.taxonGroup = taxonGroup;
        }
        map.put(key, re);
        return key;
    }

    protected String findTaxonName(Query query) {
        String taxonName = null;
        if (query instanceof TaxonQuery) {
            System.err.println("query is a TaxonQuery");
            TaxonNodeList nodes = ((TaxonQuery) query).getTaxons();
            if (nodes != null && nodes.getNodeCount() > 0) {
                TaxonNode tn = nodes.getNodes().get(0);
                System.err.println("Got a TaxonNodeList out tn="+tn);
                taxonName = tn.getLabel();
            } // FIXME
        }
        System.err.println("Returning a name of '"+taxonName+"'");
        return taxonName;
    }

    protected Errors initialiseQueryForm(Query query, ServletRequest request){
        // Attempt to fill in form
        ServletRequestDataBinder binder = new ServletRequestDataBinder(query);
        //binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), false, 10));
        //binder.registerCustomEditor(TaxonNode[].class, taxonNodeArrayPropertyEditor);
        //ConversionService conversionService;
        binder.setConversionService(conversionService);
        //logger.error("About to start binder");
        binder.bind(request);
        //logger.error("Should have finished binder");

        BindingResult br = binder.getBindingResult();

        return br;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

}
