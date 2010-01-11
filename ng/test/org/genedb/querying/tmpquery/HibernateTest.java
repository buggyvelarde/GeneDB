package org.genedb.querying.tmpquery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.QueryException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * A testcase set up for testing HQLQuery derived classes. 
 * 
 * @author gv1
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext.xml"})
@TransactionConfiguration
@Transactional
public class HibernateTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	private static final Logger logger = Logger.getLogger(HibernateTest.class);
	
	@Autowired
	private TaxonNodeManager taxonNodeManager;
	
	@Autowired
	private DateCountQuery dateCountQuery;
	
	@Test
	public void testTaxonNodeManager() 
	{
		logger.info("Running taxon test");
		TaxonNode[] taxons = { taxonNodeManager.getTaxonNodeByString("Lbraziliensis", true) };
		Assert.assertTrue(taxons.length == 1);
		Assert.assertNotNull(taxons[0]);		
		logger.debug(taxons[0].getLabel());
	}
	
	
	@Test
	public void testDateCountQuery() throws ParseException, QueryException
	{
		logger.info("Running date count test");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		Date date = dateFormat.parse("2009-06-01");
		
		TaxonNode taxonNode = taxonNodeManager.getTaxonNodeForLabel("Root");
	    List<TaxonNode> taxonList = taxonNode.getAllChildren();
	    
	    Assert.assertTrue(taxonList.size() > 0);
	    
	    dateCountQuery.setDate(date);
		dateCountQuery.setAfter(true);
	    
		int numberOfOrganismsWithChanges = 0;
		
	    for (TaxonNode taxon : taxonList)
		{
			if (! taxon.isOrganism())
				continue;
			
			TaxonNode[] taxons = {taxon};
			
			dateCountQuery.setTaxons(taxons);
			
			@SuppressWarnings("unchecked")
			List results = dateCountQuery.getResults();
			
			long count = (Long) results.get(0); 
			
			logger.trace(count);
			if (count > 0)
				numberOfOrganismsWithChanges++;
			
		}
		logger.debug("Number of organisms with changes = " + numberOfOrganismsWithChanges + " / " + taxonList.size());
		Assert.assertTrue(numberOfOrganismsWithChanges > 0);
		
	}
}
