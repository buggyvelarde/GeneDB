package org.genedb.db.domain.serviceImpls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.genedb.db.domain.misc.MethodResult;
import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.services.ProductService;

public class ProductServiceImpl implements ProductService {

	private List<Product> products = new ArrayList<Product>();
	
	public ProductServiceImpl() {
		products.add(new Product("peri", 1));
		products.add(new Product("rose", 2));
		products.add(new Product("donna", 3));
		products.add(new Product("martha", 4));
		products.add(new Product("susan", 5));
		products.add(new Product("sarah-jane", 6));
		products.add(new Product("leela", 7));
		products.add(new Product("romanadvertrelundar", 8));
		products.add(new Product("tegan", 9));
		products.add(new Product("nyssa", 10));
		products.add(new Product("mel", 11));
		products.add(new Product("ace", 12));
		products.add(new Product("jo", 13));
	}

	
	@Override
	public MethodResult rationaliseProduct(Product newProduct,
			List<Product> oldProducts) {
		
		boolean success = true;
		for (Product product : oldProducts) {
			success &= products.remove(product);
		}
		if (success) {
			return MethodResult.SUCCESS;
		}
		return new MethodResult("Failed");
	}

	
	@Override
	public List<Product> getProductList() {
		System.err.println("The number of products is '"+products.size()+"'");
		return products;
	}

}
