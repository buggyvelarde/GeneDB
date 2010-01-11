package org.genedb.query.params;

import org.springframework.validation.Errors;

/**
 * Param which contains an Integer
 *
 * @author art
 */
public class FloatParam extends AbstractParam {

    private Float value;
    private boolean set = false;
    private String validation;

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public void setValue(Float value) {
        this.value = value;
        this.set = true;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return Float.class.isAssignableFrom(clazz);
    }

    public void validate(Object value, Errors errors) {
        // Deliberately empty
    }

    public boolean isSet() {
        return this.set;
    }
}
