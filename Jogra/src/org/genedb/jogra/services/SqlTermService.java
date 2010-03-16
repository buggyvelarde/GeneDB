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
 * This class implements the Data Access Layer for the Term Rationaliser. It contains all the SQL needed for 
 * querying, updating and deleting terms. We use the Spring JDBCTemplate here to have more control over the 
 * sql we execute rather than using the existing DAOs which use Hibernate. It also does make db access faster.
 * 
 * @author nds
 */

public class SqlTermService implements TermService {
    
    private static final Logger logger = Logger.getLogger(SqlTermService.class);

    /* Configured during runtime */
    private JdbcTemplate jdbcTemplate;
    private TaxonNodeManager taxonNodeManager; 

     
    /** 
     * Takes a list of TaxonNodes (corresponding to the user's selection of organisms) and a cv 
     * type (e.g. genedb_products) and returns all the corresponding terms. 
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
     * Gets ALL the terms in a given cv
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
                                    "where cv.name= ? ;"; 
        
        logger.info(SQL_TO_GET_TERMS);
        
        terms = jdbcTemplate.query(SQL_TO_GET_TERMS, new Object[]{new String(cvName)}, mapper);
        
        return terms;
    }
    
  

    /**
     * This method rationalises terms. If there is no cvterm corresponding to the newtext, one is created. 
     * For each of the old terms, if the changeAll boolean is set to true, then all references to it are
     * changed in the feature_cvterm to refer to the correct new term. If not, then only references within
     * the user's selected organisms are changed. Then, if the changeAll is true or if the scope of the old
     * term is within the selected organisms, the old term is also deleted along with its dbxref. 
     * This method returns a RationaliserResult which contains a list of terms that were added, a list of 
     * terms that were deleted and a string holding a message to the user about the success/failure of the
     * process.                          
     */
    @Override


    public RationaliserResult rationaliseTerm(List<Term> oldTerms, String newText,
                              boolean changeAllOrganisms, List<TaxonNode> selectedTaxons) throws SQLException{
        
        /* SHOULDNT THIS HAVE SOME SORT OF TRANSACTIONAL ANNOTATION OR ROLLBACK MECHANISM ??? */
        
        /* */
        Set<Term> termsAddedSpecific = new HashSet<Term>();
        Set<Term> termsDeletedSpecific = new HashSet<Term>();
        
        Set<Term> termsAddedGeneral = new HashSet<Term>();
        Set<Term> termsDeletedGeneral = new HashSet<Term>();
        
        String message = new String(); 
        
        String type = oldTerms.get(0).getType();  
        /* */
        
        Term new_term = getTerm(newText, type); //Does this term exist?
       
        if(new_term == null){ //This term does not yet exist, so create one
          
            new_term = addTerm(newText, type);
            termsAddedSpecific.add(new_term);
            termsAddedGeneral.add(new_term);
            message = message.concat(String.format("Created term '%s' in cv %s. \n", newText, type));
            logger.info(String.format("Created term %s in cv %s", newText, type));
 
        }

        if(new_term!=null){ //sanity check as something could have gone wrong with the sql insert
            for(Term old: oldTerms){
    
                if(!old.getName().equals(newText)){ //Sanity check 
    
                    if(changeAllOrganisms){ //Just a difference in case, so update the cvterm name and dbxref accession in-situ

                        jdbcTemplate.update("update cvterm " + 
                                            "set name=? where cvterm_id=?;", 
                                             new Object[]{new_term.getName(), old.getId()});
                        
                        jdbcTemplate.update("update dbxref " + 
                                            "set accession= ? where dbxref_id=?;", 
                                            new Object[]{new_term.getName(), 
                                            this.getDbxrefId(new_term.getName(), type)});
                        
                        logger.info(String.format("Changed the case of the cvterm name and dbxref accession to %s", newText));
                        termsDeletedSpecific.add(old);
                        termsDeletedGeneral.add(old);
                        message = message.concat(String.format("Changed all annotations from '%s' to '%s'. \n", old.getName(), newText));
    
                    }else{ //Change only annotations associated with the selected organisms
    
                        //One way is to check each one (delete wrong ones, add correct ones)
                        // Or can we not just delete everything after making a list of th efeatures and then add them again
                        for(String s: getTaxonNamesList(selectedTaxons)){
                            jdbcTemplate.update("update feature_cvterm " + 
                                    "set cvterm_id= ? " + 
                                    "from feature, organism " +
                                    "where feature_cvterm.cvterm_id=? " +
                                    "and feature_cvterm.feature_id = feature.feature_id " +
                                    "and feature.organism_id=organism.organism_id " +
                                    "and organism.common_name = ?;", 
                                    new Object[]{new Integer(new_term.getId()), new Integer(old.getId()), s});
                        }
    
                        logger.info(String.format("Changed relevant annotations from '%s' to '%s'",old.getName(), newText));
                        message = message.concat(String.format("Changed relevant annotations within selected organisms from '%s' to '%s'.\n",
                                                 old.getName(), newText));
                        
                        termsDeletedSpecific.add(old);
                        
                        
                        //If scope of old term within user's chosen organisms, delete cvterm and corresponding dbxref
                        if(selectedTaxons.containsAll(getTermScope(old))){
                            jdbcTemplate.execute("delete from cvterm where cvterm_id=" + old.getId()); 
                            jdbcTemplate.update("delete from dbxref where accession=?", new Object[]{old.getName()} );
                            termsDeletedGeneral.add(old);
                            logger.info(String.format("Deleted old cvterm '%s' and dbxref.", old.getName()));
                            message = message.concat(String.format("Deleted cvterm '%s'. \n", old.getName()));                    
                        }
    
                    }
    
                   
                    
                }else{
                    message = message.concat(String.format("Error: Both the old term and new term are the same (%s). Operation aborted! \n", newText));
                }
            }
        }else{
            message = message.concat(String.format("Something went wrong with inserting a new cvterm called %s, process aborted", new_term));
        }

        RationaliserResult rationaliserResult  = new RationaliserResult(message, termsAddedSpecific, termsDeletedSpecific, 
                                                                        termsAddedGeneral, termsDeletedGeneral);

        return rationaliserResult;
    }

    /**
     * This method takes a term and a list of taxons that the user has selected,
     * and returns the list of systematic IDs for this term (within the scope 
     * of the selected taxons)
     */
   public List<String> getSystematicIDs(Term term, List<TaxonNode> selectedTaxons){

       String namesInSQLFormat = getTaxonNamesInSQLFormat(selectedTaxons);
       
       String SQL_TO_GET_SYS_IDS = "select feature.uniquename from feature, feature_cvterm, organism" +
                                  " where feature_cvterm.cvterm_id=" + term.getId() +
                                  " and feature.feature_id=feature_cvterm.feature_id " +
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
                                                      "from cvterm where lower(name)=? and cv_id=? ", 
                                                       new Object[]{name.toLowerCase(), cv_id},
                                                       mapper);
           
           logger.info(String.format("Term %s already exists in cv %s.", name, type));
           return term;
  
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){
            //Grr! Wish queryforobject just returned null when no data was found!
            return null;
        }
    }
   
    /** PRIVATE HELPER METHODS **/
    
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
                    "from cvterm where lower(name)=? and cv_id=? ", 
                     new Object[]{name.toLowerCase(), cv_id},
                     mapper);
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){
        
            return null;
        
        }
        
        return term;
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
    
    /** Returns same list as above but without quotes */
    
    private List<String> getTaxonNamesList(List<TaxonNode> taxons){
        
        List<String> temp = new ArrayList<String>();
        for(TaxonNode t: taxons){
            temp.addAll(t.getAllChildrenNames());     //getAllChildrenNamesInSQLFormat());
        }
        return temp;
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
        int cv_id = jdbcTemplate.queryForInt("select cv_id from cv where name=?", new Object[]{ type }); //Get cv id
        return cv_id;        
    }
    
    /**
     * Returns the dbxref_id given the accession and the cvtype
     * If it does not exist, a dbxref record is added
     */
    private int getDbxrefId(String accession, String cvtype){
        int db_id = getDbIdByCvType(cvtype);
        int dbxref_id;
        try{
            dbxref_id = jdbcTemplate.queryForInt("select dbxref_id " +
                                                 "from dbxref where lower(accession)=? and db_id=?", 
                                                 new Object[]{accession.toLowerCase(), db_id}); 
        }catch(org.springframework.dao.EmptyResultDataAccessException dae){
            
            jdbcTemplate.update("insert into dbxref (db_id, accession) values (?,?)", 
                                 new Object[]{db_id, accession}); 
            dbxref_id = jdbcTemplate.queryForInt("select dbxref_id " +
                                                 "from dbxref where lower(accession)=? and db_id=?", 
                                                 new Object[]{accession.toLowerCase(), db_id});
        
        }
        return dbxref_id;
    }
    
    /**
     * Given a cvterm and a set of taxons, get the IDs of the annoated features
     */
    

  
    /** INJECTED **/
    
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }
   
    
}
