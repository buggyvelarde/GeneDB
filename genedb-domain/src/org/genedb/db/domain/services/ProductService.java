package org.genedb.db.domain.services;

import java.util.List;

import org.genedb.db.domain.misc.MethodResult;
import org.genedb.db.domain.objects.Product;

public interface ProductService {
	
	List<Product> getProductList();
	
	MethodResult rationaliseProduct(Product newProduct, List<Product> products);

}
