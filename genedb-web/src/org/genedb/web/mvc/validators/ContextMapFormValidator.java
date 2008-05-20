package org.genedb.web.mvc.validators;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.controller.ContextMapController;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A simple validator for ContextMap requests.
 * 
 * Does not check that the named gene or chromosome actually exists in the
 * database.
 * 
 * @author rh11
 * 
 */
public class ContextMapFormValidator implements Validator {
    private static final Logger logger = Logger.getLogger(ContextMapFormValidator.class);
    
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        logger.debug(String.format("Do we support the class %s?\n", clazz));
        return true;
    }

    public void validate(Object rawTarget, Errors errors) {
        ContextMapController.Command target = (ContextMapController.Command) rawTarget;
        boolean isGeneCommand = target.isGeneCommand();
        boolean isRegionCommand = target.isRegionCommand();

        if (isGeneCommand && isRegionCommand) {
            errors.reject("Context map overspecified: need gene OR region spec, not both!");
            return;
        }
        if (!isGeneCommand && !isRegionCommand) {
            errors.reject("Context map underspecified: need gene or region spec");
            return;
        }

        if (isRegionCommand) {
            long start = target.getStart();
            long end = target.getEnd();
            if (0 < start)
                errors.reject("The start of the region must be at least 0");
            if (end < start)
                errors.reject("The region end must be after the start");
        }
    }
}
