package org.genedb.querying.tmpquery;

import static org.junit.Assert.*;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.QueryException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext-query.xml"})
public class SimpleNameQueryTest {

    @Autowired
    private SimpleNameQuery simpleNameQuery;
    
    @Autowired
	private ApplicationContext applicationContext;
    

    @SuppressWarnings("unchecked")
    @Test
    public void basic() throws QueryException {
    	
    	TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
    	TaxonNode taxonNode = tnm.getTaxonNodeForLabel("Root");
    	TaxonNodeList taxons = new TaxonNodeList(taxonNode);
    	
    	simpleNameQuery.setTaxons(taxons);
    	
        simpleNameQuery.setSearch("PF14_*");
        List results = simpleNameQuery.getResults();
        assertTrue(results.size() > 0);
        
        
    }

}
