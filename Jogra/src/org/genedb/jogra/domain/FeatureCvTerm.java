package org.genedb.jogra.domain;


/**
 * This class encapsulates the details of a featurecvterm. Note that this
 * is a simple class with data just enough for the rationaliser and has no
 * connection to the featurecvterm class in genedb.
 * 
 * @author nds
 *
 */

public class FeatureCvTerm  {

    private int feature_cvterm_id;
    private int feature_id;
    private int cvterm_id;
    private int pub_id;
    private int rank;
    private boolean is_not; 

    
    public FeatureCvTerm(int feature_cvterm_id, int feature_id, int cvterm_id, int pub_id, int rank, boolean is_not ) {
        this.feature_cvterm_id = feature_cvterm_id;
        this.feature_id = feature_id;
        this.cvterm_id = cvterm_id;    
        this.pub_id = pub_id;
        this.rank = rank;
        this.is_not = is_not;
    }
 
    public FeatureCvTerm(int feature_id, int cvterm_id, int pub_id, int rank, boolean is_not ) {
        this.feature_id = feature_id;
        this.cvterm_id = cvterm_id;    
        this.pub_id = pub_id;
        this.rank = rank;
        this.is_not = is_not;
    }
    
    public FeatureCvTerm() {   
    }

    /* --- Getters and setters --*/
    
    public int getFeatureCvtermId() {
        return feature_cvterm_id;
    }

    public void setFeatureCvtermId(int feature_cvterm_id) {
        this.feature_cvterm_id = feature_cvterm_id;
    }
    
    public int getFeatureId() {
        return feature_id;
    }

    public void setFeatureId(int feature_id) {
        this.feature_id = feature_id;
    }

    public int getCvtermId() {
        return cvterm_id;
    }

    public void setCvtermId(int cvterm_id) {
        this.cvterm_id = cvterm_id;
    }

    public int getPubId() {
        return pub_id;
    }

    public void setPubId(int pub_id) {
        this.pub_id = pub_id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean getIsNot() {
        return is_not;
    }

    public void setIsNot(boolean is_not) {
        this.is_not = is_not;
    }
 
  
    

}
