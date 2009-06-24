package org.genedb.jogra.services;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.jogra.domain.Product;

import java.util.List;

public interface ProductService {

    //List<Product> getProductList(boolean restrictToGeneLinked);
    
    /* Modified method that takes the TaxonNode representing selected organism */
    //List<Product> getProductList(TaxonNode taxonNode);
    
    /* Modified again in order to accept multiple taxons */
    List<Product> getProductList(List<TaxonNode> taxonNode);

   // MethodResult rationaliseProduct(Product newProduct, List<Product> products);
    /* Modified method that also takes any corrected text provided by the user*/
    MethodResult rationaliseProduct(Product newProduct, List<Product> products, String correctedText);
    
    /* Method to retrieve systematic IDs (restricted to selected scope) for a given product */
    List<String> getSystematicIDs(Product product, List<TaxonNode> taxonList);
    
    /* Method to retrieve evidence codes for a given product */
    List<String> getEvidenceCodes(Product product);
    
    /* Improve later */
    List<Product> getProductsToAdd();
    
    List<Product> getProductsToRemove();

}
