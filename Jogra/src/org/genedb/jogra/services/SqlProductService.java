package org.genedb.jogra.services;

import java.util.Arrays;
import java.util.List;

import org.genedb.jogra.domain.Product;

public class SqlProductService implements ProductService {

	@Override
	public List<Product> getProductList(boolean restrictToGeneLinked) {
		Product[] products = new Product[3];
		products[0] = new Product("foo", 2);
		products[1] = new Product("bar", 5);
		products[2] = new Product("wibble", 7);
		return Arrays.asList(products);
	}

	@Override
	public MethodResult rationaliseProduct(Product newProduct,
			List<Product> products) {
		// TODO Auto-generated method stub
		return null;
	}

}
