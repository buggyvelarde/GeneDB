package org.gmod.schema.cfg;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * Extends {@link org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean} with
 * support for the <code>@FeatureType</code> annotation. See {@link FeatureType}.
 *
 * All it does is to change the default Configuration class to {@link ChadoAnnotationConfiguration}
 * and set the data source appropriately.
 *
 * @author rh11
 */
public class ChadoSessionFactoryBean extends AnnotationSessionFactoryBean {

    public ChadoSessionFactoryBean() {
        setConfigurationClass(ChadoAnnotationConfiguration.class);
    }

    @Override
    protected Configuration newConfiguration() throws HibernateException {
        Configuration cfg = super.newConfiguration();
        if (cfg instanceof ChadoAnnotationConfiguration)
            ((ChadoAnnotationConfiguration) cfg).setDataSource(getDataSource());
        return cfg;
    }
}
