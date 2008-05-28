package org.genedb.web.mvc.validators;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.controller.ContextMapController;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A simple validator for ContextMap requests.
 * 
 * Does not check that the named chromosome actually exists in the
 * database.
 * 
 * @author rh11
 * 
 */
public class ContextMapFormValidator implements Validator {
    private static final Logger logger = Logger.getLogger(ContextMapFormValidator.class);
    
    public boolean supports(@SuppressWarnings({"unused", "unchecked"}) Class clazz) {
        return true;
    }

    public void validate(Object rawTarget, Errors errors) {
        ContextMapController.Command target = (ContextMapController.Command) rawTarget;

        if (!target.hasRequiredData()) {
            logger.error("Context map underspecified");
            errors.reject("Context map underspecified: data missing");
            return;
        }
    }
}
