package org.gmod.schema.cfg;

import org.hibernate.HibernateException;

/**
 * A <code>ChadoAnnotationException</code> is thrown if there's a problem
 * processing Chado-specific annotations. (At the time of writing, the only
 * Chado-specific annotation is {@link FeatureType}.)
 *
 * @author rh11
 */
public class ChadoAnnotationException extends HibernateException {

    public ChadoAnnotationException(String string, Throwable root) {
        super(string, root);
    }

    public ChadoAnnotationException(String s) {
        super(s);
    }

    public ChadoAnnotationException(Throwable root) {
        super(root);
    }
}
