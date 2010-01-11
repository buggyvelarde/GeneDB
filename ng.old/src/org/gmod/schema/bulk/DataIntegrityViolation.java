package org.gmod.schema.bulk;

public class DataIntegrityViolation extends Exception {

    public DataIntegrityViolation() {
        super();
    }

    public DataIntegrityViolation(String format, Object... params) {
        super(String.format(format, params));
    }
}
