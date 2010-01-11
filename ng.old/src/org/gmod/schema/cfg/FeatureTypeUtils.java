package org.gmod.schema.cfg;

import org.hibernate.HibernateException;

import java.lang.annotation.Annotation;

/**
 * Utility functions for dealing with {@link FeatureType} annotations.
 *
 * @author rh11
 */
public class FeatureTypeUtils {
    /**
     * Get the <code>@FeatureType</code> annotation on the specified class.
     *
     * @param theClass the class
     * @return the FeatureType annotation, or <code>null</code> if there is none
     */
    public static FeatureType getFeatureTypeForClass(Class<?> theClass) {
        for (Annotation annotation: theClass.getDeclaredAnnotations()) {
            if (annotation instanceof FeatureType) {
                return validate(theClass.getName(), (FeatureType) annotation);
            }
        }
        return null;
    }

    private static FeatureType validate(String className, FeatureType featureType) throws HibernateException {
        String term = featureType.term();
        String accession = featureType.accession();

        if ("".equals(term) && "".equals(accession)) {
            throw new ChadoAnnotationException(String.format("@FeatureType annotation for class '%s' has neither 'term' nor 'accession'", className));
        }

        if (!"".equals(term) && !"".equals(accession)) {
            throw new ChadoAnnotationException(String.format("@FeatureType annotation for class '%s' has both 'term' and 'accession'", className));
        }

        return featureType;
    }
}
