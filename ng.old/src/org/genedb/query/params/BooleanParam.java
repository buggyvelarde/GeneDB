package org.genedb.query.params;

import org.springframework.validation.Errors;

/**
 * Param which contains a Boolean
 *
 * @author art
 */
public class BooleanParam extends AbstractParam {

    private Boolean value;
    private boolean set = false;
    private String validation;

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public void setValue(Boolean value) {
        this.value = value;
        this.set = true;
    }


    @Override
    public Boolean getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return Boolean.class.isAssignableFrom(clazz);
    }

    public void validate(Object value, Errors errors) {
        // Deliberately empty
    }

    public boolean isSet() {
        return this.set;
    }
}
