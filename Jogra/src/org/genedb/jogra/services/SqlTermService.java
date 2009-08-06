package org.genedb.jogra.services;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.jogra.domain.Term;

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

/**
 * This class is like a Data Access Object for terms in that it handles all the SQL needed for querying, updating and deleting terms.
 * We use the Spring JDBCTemplate here to have more control over the sql we execute rather than using the existing DAOs which use 
 * Hibernate. Also, Hibernate could make the rationaliser much slower.
 * 
 * @author nds
 *
 */

public class SqlTermService implements TermService {
    
    private static final Logger logger = Logger.getLogger(SqlTermService.class);

    /* Configured during runtime */
    private JdbcTemplate jdbcTemplate;
    private List<TaxonNode> selectedTaxons = new ArrayList<TaxonNode>();
    private TaxonNodeManager taxonNodeManager;  
     
    /**
     * Takes a list of TaxonNodes (corresponding to the selection of organisms) and a cv type
     * (e.g. genedb_products) and returns all the corresponding terms 
     */
    @Override
    public List<Term> getTerms(List<TaxonNode> selectedTaxons, final String cvType) {
      
        String SQL_TO_GET_TERMS =   
            "select distinct lower(cvt.name), cvt.name, cvt.cvterm_id " +
            "from feature_cvterm fcvt, cvterm cvt, cv cv, organism o, feature f " +
            "where fcvt.cvterm_id = cvt.cvterm_id " +
            "and cvt.cv_id = cv.cv_id " +
            "and cv.name='" + cvType + "' " +
            "and fcvt.feature_id = f.feature_id " +
            "and f.organism_id=o.organism_id " +
            "and o.common_name IN (" + getTaxonNamesInSQLFormat(selectedTaxons) +") " +
            "order by lower(cvt.name), cvt.name";
                
        RowMapper<Term> mapper = new RowMapper<Term>() {
            public Term mapRow(ResultSet rs, int rowNum) throws SQLException {
                Term term = new Term(rs.getInt("cvterm_id"), rs.getString("name"), cvType);
                return term;
            }
        };
     
        List<Term> terms = jdbcTemplate.query(SQL_TO_GET_TERMS, mapper);
        return terms;
    }
    
  

    /**
     * 10.7.2009: The previous logic for rationalising was changed since it was not possible to add a new cvterm with a similar name
     * (e.g. with a simple letter) to the database before removing the old one. In the new rationaliser, if the user wants to do this, 
     * she has to opt to change the name across all organisms. Therefore, now the rationalising is done as follows:
     * 
     * for each old term o
     *          if (user has forced change on all organisms)
     *                  update term name in-situ
     *          else
     *                  find or create a new term with the new name
     *                  update relevant feature_cvterms within scope
     *                  if(scope of o is contained within chosen scope)
     *                          delete o
     *                          
     */
    @Override

  
    public RationaliserResult rationaliseTerm(List<Term> oldTerms, String newText, boolean changeAllOrganisms, List<TaxonNode> selectedTaxons) throws SQLException {


        
        List<Term> termsAdded = new ArrayList<Term>();
        List<Term> termsDeleted = new ArrayList<Term>();
        final String type = oldTerms.get(0).getType(); /* Safe to assume that the type of all the terms sent to this method are of the same type */
        String message = new String(); 
        
        for(Term old: oldTerms){
            logger.info("Inside rationalise method: " + old.getName() + " to " + newText + " type: " + type);
            
            if(!old.getName().equals(newText)){ //Sanity check
                
                /*Storing these in new variables so that the names can be made SQL ready (by escaping single quotes)
                 *However, the 'original' values will be used in the user interface.
                 *This section needs to be improved (perhaps by using PreparedStatements) so that it can handle other types of special characters 
                 */
                Term old_sql = old; 
                old_sql.setName(old_sql.getName().replaceAll("'", "\\\\'"));
                String newText_sql =  newText.replaceAll("'", "\\\\'");
              
               
                
                if(changeAllOrganisms){ //Update CvTerm. This changes it for all organisms.
                    
                    String SQL_TO_UPDATE_NAME = "update cvterm set name=E'" + newText_sql + "' where name=E'" + old_sql.getName() + "'";
               
                    jdbcTemplate.execute(SQL_TO_UPDATE_NAME);
                    termsDeleted.add(old);
                    termsAdded.add(new Term(old.getId(),newText, type));
                    
                    logger.info("Executed: " + SQL_TO_UPDATE_NAME);
                    
                    message = message.concat("Term name updated to '" + newText + "' for all organisms.\n");

                }else{
                   
                     /* Here, we add or find the cvterm with the name==text and 
                      * update all the references within the set of selected taxons. */
                    
                    if(selectedTaxons!=null){ //Sanity check
                        
                        String taxonNamesInSQLFormat = getTaxonNamesInSQLFormat(selectedTaxons);
                        
                        String SQL_TO_GET_DBXREF = "select dbxref_id from dbxref where accession=E'" + newText_sql + "'"; 
                        List<Integer> dbxref = jdbcTemplate.queryForList(SQL_TO_GET_DBXREF, Integer.class);
        
                        if(dbxref.size()<1){ //There is no other cvterm with this new name, therefore we should create one
                            
                            int db_id = this.getDbIdByCvType(type);
                            
                            String INSERT_DBXREF = "insert into dbxref (db_id, accession) values ("+ db_id +",E'" + newText_sql + "')"; //Insert new dbxref
                            jdbcTemplate.execute(INSERT_DBXREF) ;
                            logger.info("Executed: " + INSERT_DBXREF);
                           
                            int newDbxref = jdbcTemplate.queryForInt("select dbxref_id from dbxref where accession=E'" + newText_sql + "'");//Get the new dbxref_id assigned to new entry
                            int cv_id = jdbcTemplate.queryForInt("select cv_id from cv where name='" + type + "'");
                            
                            jdbcTemplate.execute("insert into cvterm (cv_id, name, dbxref_id) values ("+ cv_id +",E'" + newText_sql + "'," + newDbxref + ")"); //Add new cvterm
                            //Add to termsAdded
                            message = message.concat("New term created: '" + newText + "'.\n");

                            logger.info("Executed: Inserted new cvterm") ;
                                            
                        }
                     
                        String SQL_TO_GET_CVTERM = "select cvterm_id, name from cvterm where name=E'" + newText_sql + "'"; 
                        
                        RowMapper<Term> mapper = new RowMapper<Term>() {
                            public Term mapRow(ResultSet rs, int rowNum) throws SQLException {
                                Term term = new Term(rs.getInt("cvterm_id"), rs.getString("name"), type);
                                return term;
                            }
                        };
                        
                        Term term = jdbcTemplate.queryForObject(SQL_TO_GET_CVTERM, mapper);
                        termsAdded.add(term);
                        logger.info("After inserting, query returns: " + term.getId() + " " + term.getName());

                        String SQL_TO_UPDATE_FC = "update feature_cvterm " + 
                                                  "set cvterm_id=" + term.getId() + " " +
                                                  "from feature, organism " +
                                                  "where feature_cvterm.cvterm_id=" + old.getId()  + " " +
                                                  "and feature_cvterm.feature_id = feature.feature_id " +
                                                  "and feature.organism_id=organism.organism_id " +
                                                  "and organism.common_name IN (" + taxonNamesInSQLFormat + ")"; 
    
                        jdbcTemplate.execute(SQL_TO_UPDATE_FC);
                        logger.info("Executed: " + SQL_TO_UPDATE_FC);
                        message = message.concat("All references to '" + old.getName() + "' within the selected set of organisms successfully changed to '" + newText + "'.\n");
    
                        /* We only retrieve the scope of terms when we need to. Otherwise it would get really slow */
                        old.setScope(getTermScope(old));
    
                        if(selectedTaxons.containsAll(old.getScope())){
    
                            String SQL_TO_DELETE_CVTERM = "delete from cvterm where cvterm_id=" + old.getId(); 
                            jdbcTemplate.execute(SQL_TO_DELETE_CVTERM); 
                            logger.info("Executed: " + SQL_TO_DELETE_CVTERM);
    
                            termsDeleted.add(old);
                            message = message.concat("Term '" + old.getName() + "' deleted.\n");
                        }
                    }else{
                        message = message.concat("The set of organisms selected by the user is not visible");
                    }


                }
            }
        }
        
        RationaliserResult rationaliserResult  = new RationaliserResult(message, termsAdded, termsDeleted);
        return rationaliserResult;
    }
    
    /**
     * This method takes a term and a list of taxons that the user has selected,
     * and returns the list of systematic IDs for this term (within the scope 
     * prescribed by the taxons)
     */
   public List<String> getSystematicIDs(Term term, List<TaxonNode> selectedTaxons){

       String namesInSQLFormat = getTaxonNamesInSQLFormat(selectedTaxons);
       
       String SQL_TO_GET_SYS_IDS = "select feature.uniquename from feature, feature_cvterm, organism" +
                                  " where feature.feature_id=feature_cvterm.feature_id" +
                                  " and feature_cvterm.cvterm_id=" + term.getId() +
                                  " and feature.organism_id=organism.organism_id" +
                                  " and organism.common_name IN (" + namesInSQLFormat +")";
      
       List<String> idList = jdbcTemplate.queryForList(SQL_TO_GET_SYS_IDS, String.class);     
       return idList;
    }
   
    
    /**
     *  This method takes a term and returns a list of evidence codes, if any.
     *  The evidence code cvterm is gotten by querying the cvterm with name 'evidence' 
     */
    public List<String> getEvidenceCodes(Term term){
        
        String SQL_TO_GET_EV_CODES = "select distinct feature_cvtermprop.value from " +
                                     "feature_cvtermprop, feature_cvterm, cvterm  where " +
                                     "feature_cvtermprop.type_id=cvterm.cvterm_id and " +
                                     "cvterm.name='evidence' and " +
                                     "feature_cvtermprop.feature_cvterm_id=feature_cvterm.feature_cvterm_id and " +
                                     "feature_cvterm.cvterm_id =" + term.getId(); 

        List<String> evcList = jdbcTemplate.queryForList(SQL_TO_GET_EV_CODES, String.class);
        return evcList;
     }
    
   
    /**
     * Private helper method to get the taxon scope for a given term. 
     * Returns the set of taxons that this term is 'connected' to
     */
    private Set<TaxonNode> getTermScope(Term term){
        String SQL_TO_FETCH_SCOPE = "select distinct o.common_name " +
                                    "from feature_cvterm fcvt, organism o, feature f " +
                                    "where fcvt.cvterm_id = " + term.getId() + " " +
                                    "and fcvt.feature_id = f.feature_id " +
                                    "and f.organism_id=o.organism_id ";

        List<String> taxonNames = jdbcTemplate.queryForList(SQL_TO_FETCH_SCOPE, String.class);
        Set<TaxonNode> scope = new HashSet<TaxonNode>();
        for(String name: taxonNames){
            scope.add(taxonNodeManager.getTaxonNodeForLabel(name));
        }
        return scope;
    }
    
    /** PRIVATE HELPER METHODS **/
    
    /** 
     *  Takes a list of taxons and returns their names in a comma-separated string. 
     *  This is different to the taxonNode.getAllChildrenNamesInSQLFormat() because the 
     *  taxons here can be in various places in the phylogeny tree.
     */
    private String getTaxonNamesInSQLFormat(List<TaxonNode> taxons){
        
        List<String> temp = new ArrayList<String>();
        for(TaxonNode t: taxons){
            temp.addAll(t.getAllChildrenNamesInSQLFormat());
        }
        return StringUtils.collectionToCommaDelimitedString(temp);
    }
    
    /**
     * Takes the cv name like 'genedb_products' and returns the corresponding database id
     */
    private int getDbIdByCvType(String type){
     
        String SQL_TO_GET_DB_ID = "select db.db_id " +
        		          "from db, dbxref, cvterm, cv " +
        		          "where cv.name='" + type + "' " +
        		          "and cvterm.cv_id=cv.cv_id " +
        		          "and cvterm.dbxref_id=dbxref.dbxref_id " +
        		          "and dbxref.db_id=db.db_id limit 1";
        
        logger.info(SQL_TO_GET_DB_ID);
        int db_id = jdbcTemplate.queryForInt(SQL_TO_GET_DB_ID);
        
        return db_id;
        
    }
    

  
    /** INJECTED **/
    
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }
    
    
    
    
    
   
    
}
