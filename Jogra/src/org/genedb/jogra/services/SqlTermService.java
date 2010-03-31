/*
 * Copyright (c) 2009 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB. If not, write to the Free
 * Software Foundation Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307
 * USA
 */

package org.genedb.jogra.services;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.jogra.domain.Term;
import org.genedb.jogra.domain.FeatureCvTerm;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

/**
 * This class implements the Data Access Layer for the Term Rationaliser. It contains all the SQL needed for 
 * querying, updating and deleting terms. We use the Spring JDBCTemplate here to have more control over the 
 * sql we execute rather than using the existing DAOs which use Hibernate. It also does make db access faster.
 * There isn't any SQL anywhere else in the classes related to the Rationaliser.
 * 
 * @author nds
 */

public class SqlTermService implements TermService {
    
    private static final Logger logger = Logger.getLogger(SqlTermService.class);

    /* Configured during runtime */
    private JdbcTemplate jdbcTemplate;
    private TaxonNodeManager taxonNodeManager; 
  
     
    /** 
     * Takes a list of taxonnodes (organisms) and a cv name 
     * and returns all the corresponding terms. This is the
     * list of terms for the left hand side of the 
     * rationaliser.
     **/

    @Override
    public List<Term> getTerms(List<TaxonNode> selectedTaxons, final String cvType) {

        RowMapper<Term> mapper = new RowMapper<Term>() {
            public Term mapRow(ResultSet rs, int rowNum) throws SQLException {
                Term term = new Term(rs.getInt("cvterm_id"), rs.getString("name"), cvType);
                return term;
            }
        };
        
        List<Term> terms = new ArrayList<Term>();
        String commaSeparatedNames = this.getTaxonNamesInSQLFormat(selectedTaxons);
        
        String SQL_TO_GET_TERMS =   "select distinct cvterm.name, cvterm.cvterm_id " +
                                    "from cvterm " +
                                    "join cv on cvterm.cv_id=cv.cv_id " +
                                    "join feature_cvterm on feature_cvterm.cvterm_id=cvterm.cvterm_id " +
                                    "join feature on feature.feature_id=feature_cvterm.feature_id " +
                                    "join organism on organism.organism_id=feature.organism_id " +
                                    "where cv.name= ? and organism.common_name IN (" + commaSeparatedNames + ");"; //TODO: Bind inside IN doesn't work. Find alternative.
       
        logger.info(SQL_TO_GET_TERMS);
        
        terms = jdbcTemplate.query(SQL_TO_GET_TERMS, new Object[]{new String(cvType) /*, new String(commaSeparatedNames) */}, mapper);

        return terms;
    }
    
    
    
    /**
     * Gets all the terms in the cv specified. These are the terms
     * that will be displayed in the right side of the rationaliser
     */    
    public List<Term> getAllTerms(final String cvName){
        List<Term> terms = new ArrayList<Term>();
        
        RowMapper<Term> mapper = new RowMapper<Term>() {
            public Term mapRow(ResultSet rs, int rowNum) throws SQLException {
                Term term = new Term(rs.getInt("cvterm_id"), rs.getString("name"), cvName);
                return term;
            }
        };
              
        String SQL_TO_GET_TERMS =   "select distinct cvterm.name, cvterm.cvterm_id " +
                                    "from cvterm " +
                                    "join cv on cvterm.cv_id=cv.cv_id " +
                                    "where cv.name=? ;"; 
        
        logger.info(SQL_TO_GET_TERMS);  
        terms = jdbcTemplate.query(SQL_TO_GET_TERMS, new Object[]{new String(cvName)}, mapper);       
        return terms;
    }
    
  

    /**
     * This method rationalises terms. If there is no cvterm corresponding to the newtext, one is created. 
     * For each of the old terms, if the changeAll boolean is set to true, then the term is just updated
     * in-situ. If not, then just the annotations for the selected organisms are changed to the new term. 
     * If the scope of the old term is within the selected organisms, the old term is also deleted along 
     * with its dbxref. This method returns a RationaliserResult. See documentation in the Rationaliser-
     * Results class for more details of what it is.                          
     */
   
    public RationaliserResult rationaliseTerm(List<Term> oldTerms, String newText,
                                              List<TaxonNode> selectedTaxons) throws SQLException{
             
   
        RationaliserResult result = new RationaliserResult();
        String cvName = oldTerms.get(0).getType();  
        
        /*...................*/
        
      for(Term old: oldTerms){
            
            if(!old.getName().equals(newText)){ //Quick sanity check here,
               
                if(old.getName().equalsIgnoreCase(newText)){
            
                    /* If the terms just differ in case, we don't bother
                     * fetching anything from the database. We just
                     * update the cvterm and the dbxref in-situ and
                     * record these activities in our rationaliser
                     * result object.
                     */
                      jdbcTemplate.update("update cvterm " + 
                                          "set name=? where cvterm_id=?;", 
                                           new Object[]{newText, old.getId()});
        
                      jdbcTemplate.update("update dbxref " + 
                                          "set accession= ? where dbxref_id=?;", 
                                          new Object[]{newText, 
                                          this.getDbxrefId(newText, cvName)});
        
                      result.added(new Term(old.getId(), newText, cvName));
                      result.deletedGeneral(old);
                      result.deletedSpecific(old);
                      result.setMessage(String.format("Changed all annotations from '%s' to '%s'. \n", old.getName(), newText));
                      logger.info(String.format("Changed the case of the cvterm name and dbxref accession to %s", newText));
         
                }else{
                    /* However, if the terms are different, then we should
                     * check if the new term already exists in the cv and
                     * fetch it. If it does not exist, we create one and
                     * record it in the rationaliser result.
                     */ 
                    
                    Term newTerm = getTerm(newText, cvName);        
                    if(newTerm == null){ 
                          newTerm = addTerm(newText, cvName);
                          result.added(newTerm);
                          logger.info("Just added term into set in result: " + newTerm.getName());
                          result.setMessage(String.format("Created '%s' in cv. \n", newText));
                          
                    }
                     
                     /* Then we get all the annotations within the scope of the 
                     * chosen organisms for this term. For each feature_id,
                     * check if an annotation with the new correct term
                     * already exists. If not, add a new feature_cvterm
                     * with the details of the old one and delete the
                     * old one.
                     */
                    
                    if(newTerm!=null){ //Another sanity check as something may have gone wrong with sql insert
                          List<FeatureCvTerm> annotations = this.getAnnotations(selectedTaxons, old);
                          
                          for (FeatureCvTerm fcvt: annotations){             
                              if(!existsFeatureCvterm(fcvt.getFeatureId(), newTerm.getId())){
                                  addAnnotation(new FeatureCvTerm(fcvt.getFeatureId(), /*feature_id*/
                                                                  newTerm.getId(),     /*cvterm_id*/
                                                                  fcvt.getPubId(),     /*pub_id*/
                                                                  fcvt.getRank(),      /*rank*/
                                                                  fcvt.getIsNot()));   /*is_not*/
     
                              }
                              deleteAnnotation(fcvt);
                              
                          }
            
                          
                          /* After changing all the annotations, 
                           * this term is no longer relevant to 
                           * the selected organisms but is still a
                           * term in the cv, and hence not deleted
                           * from the general list. */
            
                          result.deletedSpecific(old);
                          result.setMessage(String.format("Changed annotations within selected organisms from '%s' to '%s'.\n",
                                                           old.getName(), newText));
                          logger.info(String.format("Changed relevant annotations from '%s' to '%s'",old.getName(), newText));
                           
                          /* If scope of old term is within the user's chosen organisms, 
                           * we delete the old term and corresponding dbxref
                           */
                          if(selectedTaxons.containsAll(getTermScope(old))){
                              jdbcTemplate.execute("delete from cvterm where cvterm_id=" + old.getId()); 
                              jdbcTemplate.update("delete from dbxref where accession=?", new Object[]{old.getName()} );
                              /* Now the term is gone from the cv. This can often
                               * happen with terms that are, for instance,
                               * misspelt.
                               */
                              result.deletedGeneral(old);
                              result.setMessage(String.format("Deleted term '%s'. \n", old.getName()));
                              logger.info(String.format("Deleted old cvterm '%s' and dbxref.", old.getName()));
                                               
                          }
                    }else{
                        result.setMessage(String.format("Something went wrong with inserting a new cvterm called %s! Hence, skipped. \n", newText));
                    }
                }
            }else{
                result.setMessage(String.format("Error: Oops! Both the old term and new term are the same (%s)! Hence, skipped. \n", newText));
            }
        }
        return result;
    }
    
    

    /**
     * This method takes a term and a list of taxons that the user has selected,
     * and returns the list of systematic IDs for this term (within the scope 
     * of the selected taxons). If selectedTaxons is null, then it returns all
     * the associated systematic IDs.
     */
   public List<String> getSystematicIDs(Term term, List<TaxonNode> selectedTaxons){
       
       String SQL_TO_GET_SYS_IDS;
       
       if(selectedTaxons!=null){
           String namesInSQLFormat = getTaxonNamesInSQLFormat(selectedTaxons);
           
           SQL_TO_GET_SYS_IDS = " select distinct feature.uniquename from feature, feature_cvterm, organism" +
                                " where feature_cvterm.cvterm_id=" + term.getId() +
                                " and feature.feature_id=feature_cvterm.feature_id " +
                                " and feature.organism_id=organism.organism_id" +
                                " and organism.common_name IN (" + namesInSQLFormat +")";
       }else{
           SQL_TO_GET_SYS_IDS = " select distinct feature.uniquename from feature, feature_cvterm" +
                                " where feature_cvterm.cvterm_id=" + term.getId() +
                                " and feature.feature_id=feature_cvterm.feature_id " ;
        
       }
      
       logger.info(SQL_TO_GET_SYS_IDS);
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

        //logger.info(SQL_TO_GET_EV_CODES);
        List<String> evcList = jdbcTemplate.queryForList(SQL_TO_GET_EV_CODES, String.class);
        return evcList;
     }
    
    
    
    /**
     * Returns a term with the given name if it exists or 
     * null if it doesn't
     */
    public Term getTerm(String name, final String type){
        
        RowMapper<Term> mapper = new RowMapper<Term>() {
            public Term mapRow(ResultSet rs, int rowNum) throws SQLException {
                Term term = new Term(rs.getInt("cvterm_id"), rs.getString("name"), type);
                return term;
            }
        };
        
        int cv_id = getCvIdByCvType(type);
        Term term;
        
        try{
           //Does this cvterm exist in this cv?
           term = (Term)jdbcTemplate.queryForObject(  "select cvterm.cvterm_id," +
                        		              "cvterm.name     " +
                                                      "from cvterm where name=? and cv_id=? ", 
                                                       new Object[]{name, cv_id},
                                                       mapper);
           
           logger.info(String.format("Term %s already exists in cv %s.", name, type));
           return term;
  
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){
            //Grr! Wish queryforobject just returned null when no data was found!
            return null;
        }
    }
    
    
    
   
    /** PRIVATE HELPER METHODS **/
    
    /**
     * Add a new cvterm to the specified cv and return
     * the corresponding Term object
     */
    private Term addTerm(String name, final String type){
        
        RowMapper<Term> mapper = new RowMapper<Term>() {
            public Term mapRow(ResultSet rs, int rowNum) throws SQLException {
                Term term = new Term(rs.getInt("cvterm_id"), rs.getString("name"), type);
                return term;
            }
        };
        
        Term term;
        int cv_id = getCvIdByCvType(type);
        int dbxref_id = getDbxrefId(name, type); //Get the dbxref_id for this term or create one if there isn't one            
        jdbcTemplate.update("insert into cvterm (cv_id, name, dbxref_id) values (?,?,?)", 
                             new Object[]{cv_id, name, dbxref_id});
        
        logger.info(String.format("Created term %s in cv %s.", name, type));
        
        try{
            term = (Term)jdbcTemplate.queryForObject(  "select cvterm.cvterm_id," +
                                                        "cvterm.name     " +
                                                        "from cvterm where name=? and cv_id=? ", 
                                                         new Object[]{name, cv_id},
                                                         mapper);
            return term;
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){
          //Grr! Wish queryforobject just returned null when no data was found!
            return null;  
        }    
        
    }
    
    
    /**
     * Private helper method to get the taxon scope for a given term. 
     * Returns the set of taxons that have features annotated using this
     * term.
     */
    private Set<TaxonNode> getTermScope(Term term){

        List<String> taxonNames = jdbcTemplate.queryForList("select distinct o.common_name " +
                                                            "from feature_cvterm fcvt, organism o, feature f " +
                                                            "where fcvt.cvterm_id = " + term.getId() + " " +
                                                            "and fcvt.feature_id = f.feature_id " +
                                                            "and f.organism_id=o.organism_id ", String.class);
        Set<TaxonNode> scope = new HashSet<TaxonNode>();
        for(String name: taxonNames){
            scope.add(taxonNodeManager.getTaxonNodeForLabel(name));
        }
        return scope;
    }
    
 
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
        		          "where cv.name= '" + type + "' " +
        		          "and cvterm.cv_id=cv.cv_id " +
        		          "and cvterm.dbxref_id=dbxref.dbxref_id " +
        		          "and dbxref.db_id=db.db_id limit 1";
        
        logger.info(SQL_TO_GET_DB_ID);
        int db_id = jdbcTemplate.queryForInt(SQL_TO_GET_DB_ID);
        
        return db_id;
        
    }
    
    
    
    /**
     * Takes the cv name like 'genedb_products' and returns the corresponding cv id
     */
    private int getCvIdByCvType(String type){
        int cv_id = jdbcTemplate.queryForInt("select cv_id from cv where name=?", 
                                             new Object[]{ type }); //Get cv id
        return cv_id;        
    }
    
    
    
    /**
     * Returns the dbxref_id given the accession and the cvtype
     * If it does not exist, a dbxref record is added and
     * the new dbxref_id returned
     */
    private int getDbxrefId(String accession, String cvtype){
        int db_id = getDbIdByCvType(cvtype);
        int dbxref_id;
        String SQL_TO_GET_DBXREF = "select dbxref_id " +
                                   "from dbxref where accession=? and db_id=?";
                                   
        try{
            dbxref_id = jdbcTemplate.queryForInt(SQL_TO_GET_DBXREF, new Object[]{accession, db_id});
            
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){         
            jdbcTemplate.update("insert into dbxref (db_id, accession) values (?,?)", 
                                 new Object[]{db_id, accession});
            dbxref_id = jdbcTemplate.queryForInt(SQL_TO_GET_DBXREF, new Object[]{accession, db_id});
     
        }
        return dbxref_id;
    }
    
     /**
      * Given the scope of taxons and a term. returns all the relevant 
      * annotations (i.e., list of featureCvTerm objects)
      * @param selectedTaxons
      * @param term
      * @return
      */
    
    private List<FeatureCvTerm> getAnnotations(List<TaxonNode> selectedTaxons, Term term){
        
        RowMapper<FeatureCvTerm> mapper = new RowMapper<FeatureCvTerm>() {
            public FeatureCvTerm mapRow(ResultSet rs, int rowNum) throws SQLException {
                FeatureCvTerm fcvterm = new FeatureCvTerm(
                                            rs.getInt("feature_cvterm_id"),
                                            rs.getInt("feature_id"),
                                            rs.getInt("cvterm_id"), 
                                            rs.getInt("pub_id"), 
                                            rs.getInt("rank"),
                                            rs.getBoolean("is_not"));
                return fcvterm;
            }
        };
        
        List<FeatureCvTerm> annotations = new ArrayList<FeatureCvTerm>();
        String commaSeparatedNames = this.getTaxonNamesInSQLFormat(selectedTaxons);
        
        String SQL_TO_GET_ANNOTATIONS = " select feature_cvterm.* " +
        		                " from feature, feature_cvterm, organism" +
                                        " where feature_cvterm.cvterm_id=" + term.getId() +
                                        " and feature.feature_id=feature_cvterm.feature_id " +
                                        " and feature.organism_id=organism.organism_id" +
                                        " and organism.common_name IN (" + commaSeparatedNames +")";
        
        annotations = jdbcTemplate.query(SQL_TO_GET_ANNOTATIONS, mapper);    
        return annotations;
        
        
        
    }
    
    
    
    /**
     * Does this feature_cvterm already exist?
     * (Given a feature_id and a cvterm_id)
     * Would it be more useful to return the
     * feature_cvterm_id at some stage?
     */
    private boolean existsFeatureCvterm(int feature_id, int cvterm_id){
        
        String SQL_TO_GET_FEATURE_CVTERM = "select feature_cvterm_id " +
        		                   "from feature_cvterm " +
        		                   "where feature_id=? " +
        		                   "and cvterm_id=?";
        
        try{
            int fcid = jdbcTemplate.queryForInt(SQL_TO_GET_FEATURE_CVTERM, 
                                                new Object[]{feature_id, cvterm_id});
            return true;
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){
            return false;
        }
            
    }
    
    /**
     * Adds the given featurecvterm to the database
     * @param fcvt
     */ 
    private void addAnnotation(FeatureCvTerm fcvt){
        
        String SQL_TO_ADD_ANNOT = "insert into feature_cvterm" +
        		          "(feature_id, cvterm_id, pub_id, is_not, rank)" +
        		          "values " +
        		          "(?,?,?,?,?)";
        
        jdbcTemplate.update(SQL_TO_ADD_ANNOT, new Object[]{
                            fcvt.getFeatureId(),
                            fcvt.getCvtermId(),
                            fcvt.getPubId(),
                            fcvt.getIsNot(),
                            fcvt.getRank()});
    }
    
    
    /**
     * Delete a feature_cvterm 
     * @param selectedTaxons
     * @param term
     */
    private void deleteAnnotation(FeatureCvTerm fcvt){
        
        String SQL_TO_DELETE_ANNOT =" delete from feature_cvterm  " +
        		            " where feature_cvterm_id = " +
                                    fcvt.getFeatureCvtermId();

        jdbcTemplate.execute(SQL_TO_DELETE_ANNOT); 
        
    }
    

  
    /** INJECTED **/
    
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }
   
    
}
