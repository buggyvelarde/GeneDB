package org.genedb.web.mvc.validators;

import org.genedb.web.mvc.controller.download.DownloadBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DownloadFeaturesValidator implements Validator {

    public boolean supports(Class arg0) {
        return true;
    }

    public void validate(Object object, Errors errors) {
        DownloadBean db = (DownloadBean) object;

        if(db.getHistoryItem() == 0) {
            System.err.println("History item in validator in 0");
            errors.reject("no.download.history");
            return;
        }

        if(db.getOutputFormat() == null) {
            errors.reject("no.download.outputformat");
            return;
        }
    }

}
