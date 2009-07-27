package org.genedb.web.mvc.model.types;

public class TranscriptRegionType {
    private int featureId;   
    private int fmin;
    private int fmax;
    private String uniquename;
    private String cvtermName;
    
    public int getFeatureId() {
        return featureId;
    }
    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }
    public int getFmin() {
        return fmin;
    }
    public void setFmin(int fmin) {
        this.fmin = fmin;
    }
    public int getFmax() {
        return fmax;
    }
    public void setFmax(int fmax) {
        this.fmax = fmax;
    }
    public String getUniquename() {
        return uniquename;
    }
    public void setUniquename(String uniquename) {
        this.uniquename = uniquename;
    }
    public String getCvtermName() {
        return cvtermName;
    }
    public void setCvtermName(String cvtermName) {
        this.cvtermName = cvtermName;
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append(featureId);
        sb.append(",");
        sb.append(fmin);
        sb.append(",");
        sb.append(fmax);
        sb.append(",");
        
        sb.append("'");
        sb.append(uniquename);
        sb.append("',");
        
        sb.append("'");
        sb.append(cvtermName);
        sb.append("'");
        
        sb.append(")");
        return sb.toString();
    }

}
