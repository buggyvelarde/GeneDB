package org.genedb.jogra.services;

import java.util.List;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;

import org.genedb.jogra.domain.Product;

public interface ProductService {

    //List<Product> getProductList(boolean restrictToGeneLinked);
    
    /* Modified method that takes the TaxonNode representing selected organism */
    //List<Product> getProductList(TaxonNode taxonNode);
    
    /* Modified again in order to accept multiple taxons */
    List<Product> getProductList(List<TaxonNode> taxonNode);

   // MethodResult rationaliseProduct(Product newProduct, List<Product> products);
    /* Modified method that also takes any corrected text provided by the user*/
    MethodResult rationaliseProduct(Product newProduct, List<Product> products, String correctedText);

}
