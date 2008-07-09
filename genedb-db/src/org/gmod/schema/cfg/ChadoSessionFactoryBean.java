package org.gmod.schema.cfg;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * Extends {@link org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean} with
 * some useful extra features:
 * <ul>
 * <li> Includes support for the {@link FeatureType} annotation.
 * <li> Changes the behaviour of the annotatedPackages attribute, so that all annotated
 * classes in such a package are automatically included.
 * <li> Default filters can be specified, which will be applied to all Hibernate sessions
 * when they're created. (Internally this uses {@link FilteringSessionFactory}.)
 * </ul>
 * <p>
 * In fact the first two features are provided by {@link ChadoAnnotationConfiguration}, which
 * can also be used from outside Spring. <code>FilteringSessionFactory</code> could be
 * used from outside Spring too, if default filters are required.
 *
 * @see org.gmod.schema.cfg.FeatureType
 *
 * @author rh11
 */
public class ChadoSessionFactoryBean extends AnnotationSessionFactoryBean {
    //private static final Logger logger = Logger.getLogger(AnnotationSessionFactoryBean.class);

    public ChadoSessionFactoryBean() {
        setConfigurationClass(ChadoAnnotationConfiguration.class);
    }

    private ClassLoader beanClassLoader;

    private String[] defaultFilters = new String[0];

    @Override
    public void setBeanClassLoader( ClassLoader beanClassLoader ) {
      this.beanClassLoader = beanClassLoader;
      super.setBeanClassLoader(beanClassLoader);
    }

    public void setDefaultFilters(String[] defaultFilters) {
        this.defaultFilters = defaultFilters;
    }

    @Override
    protected Configuration newConfiguration() throws HibernateException {
        Configuration cfg = super.newConfiguration();
        if (cfg instanceof ChadoAnnotationConfiguration) {
            ChadoAnnotationConfiguration chadoCfg = (ChadoAnnotationConfiguration) cfg;
            chadoCfg.setDataSource(getDataSource());
            chadoCfg.setClassLoader(beanClassLoader);
        }
        return cfg;
    }

    @Override
    protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
        FilteringSessionFactory filteringSessionFactory = new FilteringSessionFactory(super.newSessionFactory(config));
        for (String filterName: defaultFilters) {
            filteringSessionFactory.addFilter(filterName);
        }
        return filteringSessionFactory;
    }

}
