package org.genedb.web.mvc.model.simple;

import org.apache.log4j.Logger;

public class SimpleTranscript extends SimpleFeature {

    private String cvtName;

    private int fmin;

    Logger logger = Logger.getLogger(SimpleTranscript.class);

    public String getCvtName() {
        return cvtName;
    }

    public int getFmin() {
        return fmin;
    }

    public void setCvtName(String cvtName) {
        this.cvtName = cvtName;
    }

    public void setFmin(int fmin) {
        this.fmin = fmin;
    }

}
