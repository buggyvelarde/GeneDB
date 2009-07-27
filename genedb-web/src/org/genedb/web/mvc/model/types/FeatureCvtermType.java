package org.genedb.web.mvc.model.types;

public class FeatureCvtermType {
    private int featureCvtId;
    private int featureId;
    private int typeId;
    private int pubId;
    private String cvname;
    private String cvtname;
    private String pubUniqueName;
    
    public int getFeatureCvtId() {
        return featureCvtId;
    }
    public void setFeatureCvtId(int featureCvtId) {
        this.featureCvtId = featureCvtId;
    }
    public int getFeatureId() {
        return featureId;
    }
    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }
    public int getTypeId() {
        return typeId;
    }
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    public int getPubId() {
        return pubId;
    }
    public void setPubId(int pubId) {
        this.pubId = pubId;
    }
    public String getCvname() {
        return cvname;
    }
    public void setCvname(String cvname) {
        this.cvname = cvname;
    }
    public String getCvtname() {
        return cvtname;
    }
    public void setCvtname(String cvtname) {
        this.cvtname = cvtname;
    }
    public String getPubUniqueName() {
        return pubUniqueName;
    }
    public void setPubUniqueName(String pubUniqueName) {
        this.pubUniqueName = pubUniqueName;
    }
    
}
