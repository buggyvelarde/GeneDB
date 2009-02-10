package org.genedb.query.params;

import org.springframework.validation.Errors;


/**
 * Param which contains a String
 *
 * @author art
 */
public class StringParamFromList extends AbstractParam {

    private String value;
    private boolean set = false;
    private ListConstraint constraint;

    public void setListConstraint(ListConstraint constraint) {
        this.constraint = constraint;
    }

    public void setValue(String value) {
        this.value = value;
        this.set = true;
    }

    @Override
    public String getValue() {
        return value;
    }

    public boolean supports(Class clazz) {
        return Boolean.class.isAssignableFrom(clazz); // TODO Boolean, is that right? -rh11
    }

    public void validate(Object value, Errors errors) {
        String s = (String) value;
        if (!constraint.isValid(s)) {
        errors.reject("List doesn't contain '"+s+"'");
        }
    }

    public boolean isSet() {
        return this.set;
    }
}
