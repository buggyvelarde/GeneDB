package org.genedb.db.domain.misc;

import java.io.Serializable;

public class MethodResult implements Serializable {

    public static final MethodResult SUCCESS = new MethodResult();

    private boolean successful;
    private String errorMsg;

    public MethodResult() {
        successful = true;
    }

    public MethodResult(String errorMsg) {
        successful = false;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

}
