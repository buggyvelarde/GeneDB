package org.genedb.jogra.services;

import java.util.List;

import org.genedb.jogra.domain.Product;

public interface ProductService {

    List<Product> getProductList(boolean restrictToGeneLinked);

    MethodResult rationaliseProduct(Product newProduct, List<Product> products);

}
