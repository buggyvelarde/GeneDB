package org.genedb.querying.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.springframework.core.annotation.AnnotationUtils;

public class CachedParamDetails implements Comparable<CachedParamDetails> {

    private Field field;

    public Field getField() {
        return field;
    }

    private int order;

    public CachedParamDetails(Field field, Annotation annotation) {
        this.field = field;
        this.order = (Integer) AnnotationUtils.getValue(annotation, "order");
    }

    public int compareTo(CachedParamDetails cpd) {
        int difference = this.order - cpd.order;
        if (difference == 0) {
            throw new RuntimeException(String.format(
                "Two query parameters '%s' and '%s' have the same order '%i'", this.getName(),
                        cpd.getName(), this.order));
        }
        return difference;
    }

    public Type getType() {
        return field.getGenericType();
    }

    public String getName() {
        return this.field.getName();
    }

}
