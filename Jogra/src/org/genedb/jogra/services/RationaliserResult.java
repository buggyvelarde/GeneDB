package org.genedb.jogra.services;

import org.genedb.jogra.domain.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * RationaliserResult contains the results of the rationalising process 
 */

public class RationaliserResult extends MethodResult {

    private List<Term> termsAdded = new ArrayList<Term>();
    private List<Term> termsDeleted = new ArrayList<Term>(); 
    private String message;
  

    public RationaliserResult(String message) {
        this.message = message;
    }
    
    public RationaliserResult(String message, List<Term> added, List<Term> deleted){
        this.message = message;
        this.termsAdded = added;
        this.termsDeleted = deleted;
    }
    
    public String getMessage(){
        return message;
    }
    
    public void setMessage(String message){
        this.message = message;
    }

    public List<Term> getTermsAdded() {
        return termsAdded;
    }

    public void setTermsAdded(List<Term> termsAdded) {
        this.termsAdded = termsAdded;
    }

    public List<Term> getTermsDeleted() {
        return termsDeleted;
    }

    public void setTermsDeleted(List<Term> termsDeleted) {
        this.termsDeleted = termsDeleted;
    }
    
   
   
}
