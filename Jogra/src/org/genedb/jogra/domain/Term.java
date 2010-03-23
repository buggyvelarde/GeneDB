package org.genedb.jogra.domain;

import java.util.List;
import java.util.Set;
import org.genedb.db.taxon.TaxonNode;

/**
 * This class encapsulates the details of controlled vocabulary terms. These can either
 * be products or other types of cvterms like curation-controlled terms. These terms will 
 * be used by the term rationaliser.
 * 
 * @author nds
 *
 */

public class Term implements Comparable<Term> {

    private int id;
    private String name;
    private List<String> systematicIds; //The uniquenames of features that are annoted using me
    private List<String> evidenceCodes; //My evidence codes
    private Set<TaxonNode> scope; //The organisms that have features annoted using me
    private String type; //The cv that I come from
    private boolean seen; //Has someone looked at me since I was loaded?

       
    /* More useful constructor */
    public Term(int id, String name, String type) {
        this.name = name;
        this.id = id;    
        this.type = type;
    }

    public Term(int id, String name) {
        this.name = name;
        this.id = id;     
    }
 
  
    @Override
    /* Method called by Jlist etc */
    public String toString() {
        return name;
    }


    /* equals method used by contains method in list */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final Term other = (Term) obj;
        if(id>0 && other.getId()>0){ //test terms will be made to have a negative id 
            if (id != other.getId()) {
                return false;
            }
        }
       
        if (name == null) {
            if (other.getName() != null) {
                return false;
            }
        } else {
            if (!name.equalsIgnoreCase(other.getName())) {
                return false;
            }
        }
        return true;
    }
    
    /* The compareTo class enables terms to be sorted using generic sorting methods (e.g. Collections sort) 
     * May need to look into case sensitivity here */
    @Override
    public int compareTo(Term other){
        return this.name.compareTo(other.name);
        
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSystematicIds() {
        return systematicIds;
    }

    public void setSystematicIds(List<String> systematicIds) {
        this.systematicIds = systematicIds;
    }

    public List<String> getEvidenceCodes() {
        return evidenceCodes;
    }

    public void setEvidenceCodes(List<String> evidenceCodes) {
        this.evidenceCodes = evidenceCodes;
    }

    public Set<TaxonNode> getScope() {
        return scope;
    }

    public void setScope(Set<TaxonNode> scope) {
        this.scope = scope;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSeen(boolean seen){
        this.seen = seen;
    }
    
    public boolean seen(){
        return this.seen;
    }
  

}
