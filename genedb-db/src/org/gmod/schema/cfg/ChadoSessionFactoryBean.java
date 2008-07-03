package org.gmod.schema.cfg;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * Extends {@link org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean} with
 * support for the <code>@FeatureType</code> annotation. See {@link FeatureType}.
 *
 * @author rh11
 */
public class ChadoSessionFactoryBean extends AnnotationSessionFactoryBean {

    public ChadoSessionFactoryBean() {
        setConfigurationClass(ChadoAnnotationConfiguration.class);
    }

    @Override @SuppressWarnings("unchecked")
    public void setConfigurationClass(Class configurationClass) {
        if (configurationClass != null && ChadoAnnotationConfiguration.class.isAssignableFrom(configurationClass))
            super.setConfigurationClass(configurationClass);
    }

    @Override
    protected Configuration newConfiguration() throws HibernateException {
        Configuration cfg = super.newConfiguration();
        ((ChadoAnnotationConfiguration) cfg).setDataSource(getDataSource());
        return cfg;
    }
}
