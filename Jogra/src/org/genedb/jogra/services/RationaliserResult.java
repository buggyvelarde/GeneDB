package org.genedb.jogra.services;

import org.genedb.jogra.domain.Term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RationaliserResult contains the results of the rationalising process
 * 16March2010: We modify this to contain information specific to the
 * two lists. 
 */

public class RationaliserResult extends MethodResult {

    //Information for list on left hand side
    private Set<Term> termsAddedSpecific = new HashSet<Term>();
    private Set<Term> termsDeletedSpecific = new HashSet<Term>(); 
    
    //Information for list on right hand side
    //Could have done with one added list, but we leave both in
    //for symmetry
    private Set<Term> termsAddedGeneral = new HashSet<Term>();
    private Set<Term> termsDeletedGeneral = new HashSet<Term>(); 
    
    private String message;
  

    public RationaliserResult(String message) {
        this.message = message;
    }
    
    public RationaliserResult(String message, Set<Term> added, Set<Term> deleted){
        this.message = message;
        this.termsAddedSpecific = added;
        this.termsDeletedSpecific = deleted;
    }
    
    //Constructor used by the Rationaliser
    public RationaliserResult(String message, Set<Term> addedSpecific, Set<Term> deletedSpecific, 
                                              Set<Term> addedGeneral, Set<Term> deletedGeneral){
        this.message = message;
        
        this.termsAddedSpecific = addedSpecific;
        this.termsDeletedSpecific = deletedSpecific;
        this.termsAddedGeneral = addedGeneral;
        this.termsDeletedGeneral = deletedGeneral;
        
    }
    
    public String getMessage(){
        return message;
    }
    
    public void setMessage(String message){
        this.message = message;
    }

    public Set<Term> getTermsAddedSpecific() {
        return termsAddedSpecific;
    }

    public void setTermsAddedSpecific(Set<Term> termsAddedSpecific) {
        this.termsAddedSpecific = termsAddedSpecific;
    }

    public Set<Term> getTermsDeletedSpecific() {
        return termsDeletedSpecific;
    }

    public void setTermsDeletedSpecific(Set<Term> termsDeletedSpecific) {
        this.termsDeletedSpecific = termsDeletedSpecific;
    }

    public Set<Term> getTermsAddedGeneral() {
        return termsAddedGeneral;
    }

    public void setTermsAddedGeneral(Set<Term> termsAddedGeneral) {
        this.termsAddedGeneral = termsAddedGeneral;
    }

    public Set<Term> getTermsDeletedGeneral() {
        return termsDeletedGeneral;
    }

    public void setTermsDeletedGeneral(Set<Term> termsDeletedGeneral) {
        this.termsDeletedGeneral = termsDeletedGeneral;
    }

   
   
   
}
