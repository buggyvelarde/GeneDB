package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.genedb.db.dao.SequenceDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ProductController extends SimpleFormController {
	
	private SequenceDao sequenceDao;
	private String listProductsView;
	
	public void setListProductsView(String listProductsView) {
		this.listProductsView = listProductsView;
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	@Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }
	
	@SuppressWarnings("unchecked")
	@Override
    protected ModelAndView onSubmit(Object command) throws Exception {
		HashMap hm = sequenceDao.getProducts();
		Map<String, Object> model = new HashMap<String, Object>(2);
		List<String> products = new ArrayList<String>();
		List<String> numbers = new ArrayList<String>();
		String viewName = listProductsView;
		int count = 0;
		Set mappings = hm.entrySet();
		for (Iterator i = mappings.iterator(); i.hasNext();) {
	           Map.Entry me = (Map.Entry)i.next();
	           Object product = me.getKey();
	           Object number = me.getValue();
	           products.add(count, product.toString());
	           numbers.add(count,number.toString());
	           count++;
	        }
		model.put("products", products);
		model.put("numbers", numbers);
		return new ModelAndView(viewName,model);
	}
}
