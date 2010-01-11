package org.genedb.web.mvc.controller.download;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.genedb.querying.core.Query;
import org.genedb.querying.tmpquery.MockProteinLengthQuery;
import org.genedb.web.mvc.controller.WebConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

/**
 *
 * @author Larry Oke
 * @desc The QueryController test case
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration// "classpath:/com/example/QueryControllerTest-context.xml"
public class QueryControllerTest {

	@Autowired
	private QueryController queryController;

	@Autowired
	private Query proteinLengthQuery;

	/**
	 * Test to verify a successful ProteinLengthQuery submission
	 */
	@Test
	public void testMultipleResultProteinLengthQuery() throws Exception{

		//Initialise context
		ServletRequest request = createProteinLengthQueryServletRequest("proteinLength");
		String queryName = request.getParameter("q");
		HttpSession session = new MockHttpSession();
		Model model = new ExtendedModelMap();


		//Re- Initialise results to be returned by query
		List<String> results = new ArrayList<String>();
		results.add("resultItem1");
		results.add("resultItem2");
		((MockProteinLengthQuery)proteinLengthQuery).initResult(results);

		//test
		String view = queryController.processForm(queryName, null, request, session, model);

		//Check for incorrect view
		Assert.assertNotSame("Wrong view returned.", "redirect:/QueryList", view);

		//Check for error message absence
		Assert.assertNull(
				"Error returned when processing form is: " +
				session.getAttribute(WebConstants.FLASH_MSG),
				session.getAttribute(WebConstants.FLASH_MSG));

		//Check for correct view
		Assert.assertEquals("Wrong view returned.", "search/"+queryName, view);

	}

	/**
	 * Test to verify a successful ProteinLengthQuery submission
	 */
	@Test
	public void testSingleResultProteinLengthQuery() throws Exception{

		//Initialise context
		ServletRequest request = createProteinLengthQueryServletRequest("proteinLength");
		String queryName = request.getParameter("q");
		HttpSession session = new MockHttpSession();
		Model model = new ExtendedModelMap();


		//Re- Initialise results to be returned by query
		List<String> results = new ArrayList<String>();
		results.add("resultItem1");
		((MockProteinLengthQuery)proteinLengthQuery).initResult(results);

		//test
		String view = queryController.processForm(queryName, null, request, session, model);

		//Check for incorrect view
		Assert.assertNotSame("Wrong view returned.", "redirect:/QueryList", view);

		//Check for error message absence
		Assert.assertNull(
				"Error returned when processing form is: " +
				session.getAttribute(WebConstants.FLASH_MSG),
				session.getAttribute(WebConstants.FLASH_MSG));

		//Check for correct view
		Assert.assertEquals("Wrong view returned.", "redirect:/gene/"+results.get(0), view);

	}


	/**
	 * Test to verify a successful ProteinLengthQuery submission
	 */
	@Test
	public void testEmptyResultProteinLengthQuery() throws Exception{

		//Initialise context
		ServletRequest request = createProteinLengthQueryServletRequest("proteinLength");
		String queryName = request.getParameter("q");
		HttpSession session = new MockHttpSession();
		Model model = new ExtendedModelMap();


		//Re- Initialise to ensure results are empty
		List<String> results = new ArrayList<String>();
		((MockProteinLengthQuery)proteinLengthQuery).initResult(results);

		//test
		String view = queryController.processForm(queryName, null, request, session, model);

		//Check for incorrect view
		Assert.assertNotSame("Wrong view returned.", "redirect:/QueryList", view);

		//Check for correct view
		Assert.assertEquals("Wrong view returned.", "search/"+queryName, view);
	}

	/**
	 * Mock the ServletRequest
	 * @return
	 */
	private ServletRequest createProteinLengthQueryServletRequest(String queryName){
		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		servletRequest.addParameter("q", queryName);
		//servletRequest.addParameter("min", "50");
		servletRequest.addParameter("max", "500");
		return servletRequest;
	}
}
