package org.genedb.query.params;

import org.springframework.validation.Errors;

/**
 * Param which contains an Integer
 *
 * @author art
 */
public class IntParam extends AbstractParam {

    private Integer value;
    private boolean set = false;
    private String validation;

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public void setValue(Integer value) {
        this.value = value;
        this.set = true;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return Integer.class.isAssignableFrom(clazz);
    }

    public void validate(Object value, Errors errors) {
        // Deliberately empty
    }

    public boolean isSet() {
        return this.set;
    }

}
