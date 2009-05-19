package org.genedb.jogra.services;

import java.io.Serializable;

public class MethodResult implements Serializable {

    public static final MethodResult SUCCESS = new MethodResult();

    private boolean successful;
    private String errorMsg;
    private String successMsg; //Added by NDS to also get information about methods that have been successful

   /* Getter and setter message for success message */
    public String getSuccessMsg() {
        return successMsg;
    }

    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

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
