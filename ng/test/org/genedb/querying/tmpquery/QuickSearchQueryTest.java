package org.genedb.querying.tmpquery;

import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.QuickSearchQuery.QuickSearchQueryResults;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext-query.xml"})
public class QuickSearchQueryTest {
		
	private static final Logger logger = Logger.getLogger(QuickSearchQueryTest.class);
	
 	@Autowired
    private QuickSearchQuery quickSearchQuery;
 	
 	@Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testBasic() throws QueryException {
    	
    	String searchText = "m";
    	String taxonLabel = "Bacteria";
    	
    	this.query(searchText, taxonLabel);
    	
    }
    
//    @BeforeClass
//    public static void setupAndLoad() {
//    	ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});
//    }
//    
    
    private void query(String searchText, String taxonLabel) throws QueryException {
    	
    	
    	logger.info("Running basic test");
    	
    	TaxonNodeManager tnm = (TaxonNodeManager) applicationContext.getBean("taxonNodeManager", TaxonNodeManager.class);
    	
    	TaxonNode taxonNode = tnm.getTaxonNodeForLabel(taxonLabel);
    	TaxonNode[] taxons = new TaxonNode[] {taxonNode};
    	
    	logger.info("Nodes in " + taxonNode);
    	for (TaxonNode taxon : taxons) {
    		logger.info(taxon);
    	}
    	
    	quickSearchQuery.setAllNames(true);
    	quickSearchQuery.setProduct(true);
    	quickSearchQuery.setPseudogenes(true);
    	
    	//quickSearchQuery.setTaxons(taxons);
    	
    	quickSearchQuery.setSearchText(searchText);
    	
    	//System.out.println("Fetching results for "+searchText+"???????");
    	
    	
    	
    	QuickSearchQueryResults queryResults = quickSearchQuery.getQuickSearchQueryResults();
    	
    	logger.info(queryResults);
    	
    	List<String> results = quickSearchQuery.getResults(0, 10);
    	
    	logger.info(queryResults.getQuickResultType());
    	
    	logger.info("Fetching results in " + results + "???????");
        
    	for (String result : results) {
    		logger.info(result);
    	}
    }

	
}
