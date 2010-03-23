package org.genedb.jogra.services;

import org.genedb.jogra.domain.Term;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * RationaliserResult contains all the results of the rationalising
 * process that need to be communicated to the user. It holds:
 * 
 * 1)The terms that were deleted and added.
 * 2)A message containing explanations of what went on.
 * 
 */

public class RationaliserResult extends MethodResult {

    /* We keep track of the terms that were added and deleted
     * during the rationalising process here because the 2 lists
     * displayed to the user isn't always refreshed from the
     * database. The terms added will always be the same for
     * both lists in the interface. However, the deleted terms
     * will differ. Most often the terms can be removed from the
     * organism-specific list but continue to remain in the
     * more general term list. It's only when a term is actually
     * deleted that it can be removed from the general list.
     */
    
    private Set<Term> termsAdded = new HashSet<Term>();
    private Set<Term> termsDeletedSpecific = new HashSet<Term>();
    private Set<Term> termsDeletedGeneral = new HashSet<Term>();
    
    private String message = ""; //A non-null string
  

    public RationaliserResult() {

    }
  
    //Constructor used by the SQLTermService
    public RationaliserResult(String message, Set<Term> added, 
                              Set<Term> deletedSpecific, Set<Term> deletedGeneral){
        
        this.message = message;
        
        this.termsAdded = added;
        this.termsDeletedSpecific = deletedSpecific;
        this.termsDeletedGeneral = deletedGeneral;
        
    }
    
    /* Add a term to one of the sets */
    public void added(Term t) { this.termsAdded.add(t); }
    public void deletedSpecific(Term t) { this.termsDeletedSpecific.add(t); }
    public void deletedGeneral(Term t) { this.termsDeletedGeneral.add(t); }
    
    @Override
    public String toString(){
        
        return String.format("Terms added: %s \n" +
        		     "Terms deleted from specific list: %s \n" +
        		     "Terms deleted from general list: %s \n" +
        		     "Message: %s \n",
        		     StringUtils.collectionToCommaDelimitedString(termsAdded),
        		     StringUtils.collectionToCommaDelimitedString(termsDeletedSpecific),
        		     StringUtils.collectionToCommaDelimitedString(termsDeletedGeneral),
        		     message);
        
    }
   
    /* Getters and setters */
    
    public String getMessage(){
        return message;
    }
    
    public void setMessage(String message){
        this.message = this.message.concat(message);
    }
  

    public Set<Term> getTermsDeletedSpecific() {
        return termsDeletedSpecific;
    }

    public void setTermsDeletedSpecific(Set<Term> termsDeletedSpecific) {
        this.termsDeletedSpecific = termsDeletedSpecific;
    }

    public Set<Term> getTermsAdded() {
        return termsAdded;
    }

    public void setTermsAddedGeneral(Set<Term> termsAddedGeneral) {
        this.termsAdded = termsAddedGeneral;
    }

    public Set<Term> getTermsDeletedGeneral() {
        return termsDeletedGeneral;
    }

    public void setTermsDeletedGeneral(Set<Term> termsDeletedGeneral) {
        this.termsDeletedGeneral = termsDeletedGeneral;
    }

   
   
   
}
