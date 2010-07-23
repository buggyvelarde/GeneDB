package org.genedb.querying.tmpquery;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;

/**
 *
 * @author sangerinstitute
 * @desc ProteinLengthQuery test case
 *
 */
public class ProteinLengthQueryTest {
	@Test
	public void testValidLengthQuery(){
		ProteinLengthQuery query = new ProteinLengthQuery();
		query.setMin(50);
		query.setMax(500);
		Errors errors = new BindException(query, "query");
		query.validate(query, errors);

		Assert.assertFalse(errors.hasErrors());
	}

//	@Test
//	public void testMaxLengthValidation(){
//		ProteinLengthQuery query = new ProteinLengthQuery();
//		query.setMax(501);
//		Errors errors = new BindException(query, "query");
//		query.validate(query, errors);
//
//		Assert.assertTrue(errors.hasErrors());
//		Assert.assertTrue(errors.hasFieldErrors());
//		Assert.assertFalse(errors.hasGlobalErrors());
//		Assert.assertEquals(errors.getFieldError("max").getField(), "max");
//		Assert.assertEquals(errors.getFieldError("max").getDefaultMessage(),
//		"Max must be less than or equal to 500. Bla bla bla");
//	}


	//@Test
//	public void testMinLengthValidation(){
//		ProteinLengthQuery query = new ProteinLengthQuery();
//		query.setMin(0);
//		Errors errors = new BindException(query, "query");
//		query.validate(query, errors);
//
//		Assert.assertTrue(errors.hasErrors());
//		Assert.assertTrue(errors.hasFieldErrors());
//		Assert.assertFalse(errors.hasGlobalErrors());
//		Assert.assertEquals(errors.getFieldError("min").getField(), "min");
//		Assert.assertEquals(errors.getFieldError("min").getDefaultMessage(),
//				"Min must be greater than or equal to 1. Bla bla bla");
//	}

	//@Test
//	public void testPositiveRangeValidation(){
//		ProteinLengthQuery query = new ProteinLengthQuery();
//		query.setMin(500);
//		query.setMax(400);
//		Errors errors = new BindException(query, "query");
//		query.validate(query, errors);
//
//		Assert.assertTrue(errors.hasErrors());
//		Assert.assertFalse(errors.hasFieldErrors());
//		Assert.assertTrue(errors.hasGlobalErrors());
//		Assert.assertEquals(errors.getGlobalError().getCode(), "min.greater.than.max");
//	}
}
