package org.genedb.querying.tmpquery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.genedb.query.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * A testcase setup for testing SQLQuery derived classes.
 *
 * @author gv1
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext-query.xml"})
public class SQLQueryTest {

	private static final Logger logger = Logger.getLogger(SQLQueryTest.class);

	@Autowired
	private ChangedGeneFeaturesQuery dateWithAncestors;

	private int count;

	@Test
	public void testProcessCallBack() throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		Date date = dateFormat.parse("2009-06-01");

		dateWithAncestors.setDate(date);
		dateWithAncestors.setOrganismId(14);

		logger.info("Running SQL ChangedGeneFeaturesQuery with " + dateFormat.format(date) + " and an organismID of " + 14);

		count = 0;

		dateWithAncestors.processCallBack(new RowCallbackHandler(){
            public void processRow(ResultSet rs) throws SQLException {
            	int colNum = rs.getMetaData().getColumnCount();
            	for (int i = 1; i <= colNum; i++)
                {
            		Assert.assertNotNull(rs.getObject(i));
                }
            	count++;
            }
		});
		logger.info("Counted " + count + " rows");
		Assert.assertTrue(count > 0);
	}

	@Test
	public void testProcess() throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		Date date = dateFormat.parse("2009-06-01");

		dateWithAncestors.setDate(date);
		dateWithAncestors.setOrganismId(14);

		logger.info("Running SQL ChangedGeneFeaturesQuery with " + dateFormat.format(date) + " and an organismID of " + 14);

		Result results = dateWithAncestors.process();

		count = 0;

		for (Object result : results)
		{
			Object[] objectArray = (Object[]) result;
			StringBuffer sb = new StringBuffer();
			for (Object obj : objectArray)
			{
				sb.append(obj);
				sb.append("\t");
			}
			Assert.assertTrue(sb.toString().length() > 0);
			logger.trace(sb);
			count++;
		}

		logger.info("Counted " + count + " rows");
		Assert.assertTrue(count > 0);

	}

}
