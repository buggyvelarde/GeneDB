package org.genedb.web.mvc.model.simple;

import org.apache.log4j.Logger;

public class SimpleGene extends SimpleFeature {

    Logger logger = Logger.getLogger(SimpleGene.class);

    private int fmin;

    private int sourceFeatureId;

    private String cvtName;

    private String topLevelFeatureUniqueName;

    public String getCvtName() {
        return cvtName;
    }

    public int getFmin() {
        return fmin;
    }

    public int getSourceFeatureId() {
        return sourceFeatureId;
    }

    public String getTopLevelFeatureUniqueName() {
        return topLevelFeatureUniqueName;
    }

    public void setCvtName(String cvtName) {
        this.cvtName = cvtName;
    }

    public void setFmin(int fmin) {
        this.fmin = fmin;
    }

    public void setSourceFeatureId(int sourceFeatureId) {
        this.sourceFeatureId = sourceFeatureId;
    }

    public void setTopLevelFeatureUniqueName(String topLevelFeatureUniqueName) {
        this.topLevelFeatureUniqueName = topLevelFeatureUniqueName;
    }

}
