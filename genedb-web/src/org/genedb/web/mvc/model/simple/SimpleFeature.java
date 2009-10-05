package org.genedb.web.mvc.model.simple;

import org.apache.log4j.Logger;

public class SimpleFeature {

    Logger logger = Logger.getLogger(SimpleFeature.class);

    private int featureId;

    private String uniqueName;

    public int getFeatureId() {
        return featureId;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

}
