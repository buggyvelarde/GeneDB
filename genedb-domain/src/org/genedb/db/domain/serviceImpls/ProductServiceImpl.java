package org.genedb.db.domain.serviceImpls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.genedb.db.domain.misc.MethodResult;
import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.services.ProductService;

public class ProductServiceImpl implements ProductService {

	@Override
	public MethodResult rationaliseProduct(Product newProduct,
			List<Product> products) {
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Product> getProductList() {
		List<Product> products = new ArrayList<Product>();
		products.add(new Product("peri", 1));
		products.add(new Product("rose", 2));
		products.add(new Product("donna", 3));
		products.add(new Product("martha", 4));
		return products;
	}

}
