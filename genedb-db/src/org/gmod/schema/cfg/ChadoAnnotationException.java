package org.gmod.schema.cfg;

import org.hibernate.HibernateException;

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
