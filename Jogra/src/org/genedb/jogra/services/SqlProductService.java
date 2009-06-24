/**
 * Class to handle the SQL needed to retrieve list of products and rationalise names
 */

package org.genedb.jogra.services;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.jogra.domain.Product;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

public class SqlProductService implements ProductService {
    
    private static final Logger logger = Logger.getLogger(SqlProductService.class);
    private JdbcTemplate jdbcTemplate;
    private List<TaxonNode> selection = new ArrayList<TaxonNode>(); 
    private List<Product> add = new ArrayList<Product>();
    private List<Product> remove = new ArrayList<Product>(); 
    private int dbxref_id = 0;
   
    /* Setting datasource. Datasource details in configuration file */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /* *
     * Method to return a list of products. If the user has selected an organism, only return products pertaining to the selected organism. If not, return all products. 
     * */
    @Override
    public List<Product> getProductList(List<TaxonNode> taxonList) {
        logger.info("SQLProductService: getProductList for taxons ->" + StringUtils.collectionToCommaDelimitedString(taxonList));
        selection = taxonList;
        String sql = new String();
        if(taxonList==null || taxonList.size()==0){ 
            /* SQL query to retrieve all products from database. In reality, this should never happen as an empty selection should return the Root taxonnode */
            sql = "select distinct" +
            "    lower(cvterm.name) as lc_name" +
            "  , cvterm.name" +
            "  , cvterm.cvterm_id" +
            " from feature_cvterm" +
            " join cvterm using (cvterm_id)" +
            " join cv using (cv_id)" +  
            " where cv.name='genedb_products'" + 
            " order by lower(cvterm.name), cvterm.name"; 
        }else{
            /* SQL query to retrieve products pertaining to selected organisms or class of organisms */
      
            List<String> temp = new ArrayList<String>();
        
            for(TaxonNode t: taxonList){
                temp.addAll(t.getAllChildrenNamesInSQLFormat());
            }
            String namesInSQLFormat = StringUtils.collectionToCommaDelimitedString(temp);
            logger.info("Names in SQL format for SQL query to retrieve products: " + namesInSQLFormat);
            
            sql = 
            "select distinct lower(cvt.name), cvt.name, cvt.cvterm_id " +
            "from feature_cvterm fcvt, cvterm cvt, cv cv, organism o, feature f " +
            "where fcvt.cvterm_id = cvt.cvterm_id " +
            "and cvt.cv_id = cv.cv_id " +
            "and cv.name='genedb_products' " +
            "and fcvt.feature_id = f.feature_id " +
            "and f.organism_id=o.organism_id " +
            "and o.common_name IN (" + namesInSQLFormat +")" +
            " order by lower(cvt.name), cvt.name";
        }        
        RowMapper<Product> mapper = new RowMapper<Product>() {
            public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
                Product product = new Product(rs.getString("name"), rs.getInt("cvterm_id"));
                return product;
            }
        };

        List<Product> products = jdbcTemplate.query(sql, mapper);
        return products;
    }

    /*
     * Method to rationalise a set of products using following rules:
     * 1. If there is a corrected product name & nothing like that already exists:
     *          Add a new cvterm to the database (this becomes new product). 
     *          Delete previous product if within scope (e.g., within bacteria) [not yet doing this]
     * 2. For each old product:
     *          if(old product within scope)
     *                  Change all references to old product to new product
     *                  Delete old product
     *          else
     *                  Change only references within scope
     * 
     */
    @Override
    public MethodResult rationaliseProduct(Product newProduct, List<Product> oldProducts, String correctedText) {
        add.clear();
        remove.clear();
        String sql = new String(); //Used to contain long sql statements
        String message = new String(); //This string will contain information about which products have been rationalised and which have not
        //If there is a corrected product name, create new cvterm and delete previous product if it is within selected scope
        if(correctedText!=null && !correctedText.equals("") && !correctedText.equals(newProduct.toString())){
            
            int cv_id = jdbcTemplate.queryForInt("select cv_id from cv where name='genedb_products'"); //Getting cv_id
            int db_id = jdbcTemplate.queryForInt("select db_id from db where name='PRODUCT'"); //Getting db_id
            
            RowMapper<Integer> mapper = new RowMapper<Integer>() { //Is there a better way to deal with SQL queries that may return 0 rows? Can queryforList be used?
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Integer(rs.getInt("dbxref_id"));
                }
            };
            sql = "select dbxref_id from dbxref where accession='" + correctedText + "'"; //Does a dbxref row exist for this cvterm? (Highly unlikely but check anyway)   
            List<Integer> dbxref_id_list = jdbcTemplate.query(sql, mapper);
            logger.info("Executed: " + sql);
            
            if(dbxref_id_list.size()<1){ //There is no other cvterm with this new name
                sql = "insert into dbxref (db_id, accession) values ("+ db_id +",'" + correctedText + "')"; //Insert new dbxref row
                jdbcTemplate.execute(sql) ;
                logger.info("Executed: " + sql);
                 
                dbxref_id = jdbcTemplate.queryForInt("select dbxref_id from dbxref where accession='" + correctedText + "'");//Get the new dbxref_id assigned to new entry
                logger.info("Executed: " + sql);
                
                jdbcTemplate.execute("insert into cvterm (cv_id, name, dbxref_id) values ("+ cv_id + ",'" + correctedText + "'," + dbxref_id + ")"); //Add new cvterm
                add.add(new Product(correctedText,cv_id));
                logger.info("Executed: " + sql);
                                
            }else if(dbxref_id_list.size()==1){ //Should only contain one result, but check anyway
                logger.debug("There is a product with the name " + correctedText + " already.");
                message = message.concat("A product with name '" + correctedText + "' exists already. That will be used to rationalise products. \n");
            }else if(dbxref_id_list.size()>1){ //Possible error in database
                logger.debug("Database error: There appears to be more than one dbxref for " + correctedText);
            }
            
            RowMapper<Product> mapper2 = new RowMapper<Product>() {
                public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Product product = new Product(rs.getString("name"), rs.getInt("cvterm_id"));
                    return product;
                }
            };
            sql = "select cvterm.name, cvterm.cvterm_id from cvterm where name='" + correctedText + "'"; 
            newProduct = jdbcTemplate.queryForObject(sql, mapper2); //The new cvterm becomes 'new product' for rest of this method
            
            //to do: delete previous product if it is within chosen scope
        }
        
        Set<String> scopeNewProduct = new HashSet<String>(getScope(newProduct)); //Using a set gets rid of duplicates
        logger.info("Scope of new product: " + scopeNewProduct.toString());
        List<String> chosenOrganisms = new ArrayList<String>();
        if(selection!=null){ //Should always be either Root or organism-name
            for(TaxonNode s : selection){
                chosenOrganisms.addAll(s.getAllChildrenNamesInSQLFormat());
                logger.info("Organisms in scope: " + StringUtils.collectionToCommaDelimitedString(chosenOrganisms));
            }
        }else{
            logger.debug("Error: User selection is null (sqlProductService)");
        }
       
        for(Product oldProduct: oldProducts){
            if(!(oldProduct.toString().equals(newProduct.toString()))){ //Sanity check
                Set<String> scopeOldProduct = new HashSet<String>(getScope(oldProduct));
                if(chosenOrganisms.containsAll(scopeOldProduct)){ //Change all references to old product and delete old product
                    sql = "update feature_cvterm set cvterm_id=" + newProduct.getId() + 
                          " where feature_cvterm.cvterm_id=" + oldProduct.getId() ;
                    jdbcTemplate.execute(sql);
                    logger.info("Executed: " + sql);
                    sql = "delete from cvterm where cvterm_id=" + oldProduct.getId(); 
                    jdbcTemplate.execute(sql); 
                    logger.info("Executed: " + sql);
                    message = message.concat("All references to '" + oldProduct.toString() + "' successfully changed and product deleted. \n");
                    
                }else{ //Only change feature_cvterms within scope and do not delete old product
                    sql = "update feature_cvterm " + 
                    "set cvterm_id=" + newProduct.getId() + 
                    " from feature, organism" +
                    " where feature_cvterm.cvterm_id=" + oldProduct.getId()  +
                    " and feature_cvterm.feature_id = feature.feature_id " +
                    "and feature.organism_id=organism.organism_id " +
                    "and organism.common_name IN (" + StringUtils.collectionToCommaDelimitedString(chosenOrganisms) +")";
                    jdbcTemplate.execute(sql); 
                    logger.info("Executed: " + sql);
                    Set <String> outsideScope = scopeOldProduct;
                    outsideScope.removeAll(chosenOrganisms); //outsideScope should now contain a list of organisms not in chosenOrganisms
                    message = message.concat("!References changed within scope. '" + oldProduct.toString() + "' affects the following organisms outside scope: " + StringUtils.collectionToCommaDelimitedString(outsideScope) + "\n");
                }
                remove.add(oldProduct);
            }else{
                message = message.concat("Old product (" + oldProduct.toString() + ") and new product are the same. \n");
            }
        }
        MethodResult successfulMR  = new MethodResult();
        successfulMR.setSuccessMsg(message);
        return successfulMR;
    }
    
    /*
     * Private helper methods to get the organism scope for a given product or list of products
     * Each word is enclosed within single quotes to make SQL queries easier
     */
    private List<String> getScope(Product product){
        String sql = "select distinct o.common_name " +
        "from feature_cvterm fcvt, organism o, feature f " +
        "where fcvt.cvterm_id = " + product.getId() + " " +
        "and fcvt.feature_id = f.feature_id " +
        "and f.organism_id=o.organism_id ";
        List<String> temp = jdbcTemplate.queryForList(sql, String.class);
        List<String> scope = new ArrayList<String>();
        for(Object org: temp){
            scope.add("'" + (String)org + "'");
        }
        return scope;
    }
    
    /* Method to retrieve systematic IDs for a given product  */
    public List<String> getSystematicIDs(Product product, List<TaxonNode> taxonList){

        List<String> temp = new ArrayList<String>();
        for(TaxonNode t: taxonList){
            temp.addAll(t.getAllChildrenNamesInSQLFormat());
        }
        String namesInSQLFormat = StringUtils.collectionToCommaDelimitedString(temp);
        
       String sql = "select feature.uniquename from feature, feature_cvterm, organism" +
       		" where feature.feature_id=feature_cvterm.feature_id" +
       		" and feature_cvterm.cvterm_id=" + product.getId() +
       		" and feature.organism_id=organism.organism_id" +
       		" and organism.common_name IN (" + namesInSQLFormat +")";
       		
       
       RowMapper<String> mapper = new RowMapper<String>() { //Is there a better way to deal with SQL queries of this nature?
           public String mapRow(ResultSet rs, int rowNum) throws SQLException {
               return new String(rs.getString("uniquename"));
           }
       };
      
       List<String> idList = jdbcTemplate.query(sql, mapper);
       return idList;
    }
    
    /* Method to retrieve evidence codes for a given product */
    public List<String> getEvidenceCodes(Product product){
        String sql = "select distinct fcp.value from " +
                     "feature_cvtermprop fcp, feature_cvterm fc where " +
                     "fcp.type_id=26761 and " +
                     "fcp.feature_cvterm_id=fc.feature_cvterm_id and " +
                     "fc.cvterm_id =" + product.getId(); //Check if this sql query is right?
        RowMapper<String> mapper = new RowMapper<String>() { //Is there a better way to deal with SQL queries of this nature?
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new String(rs.getString("value"));
            }
        };
       
        List<String> evcList = jdbcTemplate.query(sql, mapper);
        return evcList;
     }
    
    /* */
    public List<Product> getProductsToAdd(){
        return add;
    }
    
    public List<Product> getProductsToRemove(){
        return remove;
    }
    
    
    
     
    
    
    
}
