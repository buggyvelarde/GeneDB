package org.genedb.web.mvc.model.types;

public class FeaturePropType {

    private int featurePropId;
    private String value;
    private String cvtName;
    private String cvName;
    public int getFeaturePropId() {
        return featurePropId;
    }
    public void setFeaturePropId(int featurePropId) {
        this.featurePropId = featurePropId;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getCvtName() {
        return cvtName;
    }
    public void setCvtName(String cvtName) {
        this.cvtName = cvtName;
    }
    public String getCvName() {
        return cvName;
    }
    public void setCvName(String cvName) {
        this.cvName = cvName;
    }
    
    
}
