package org.gmod.schema.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.util.ClassUtils;

/**
 * Extends {@link org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean} with
 * some useful extra features:
 * <ul>
 * <li> Includes support for the {@link FeatureType} annotation.
 * <li> Changes the behaviour of the annotatedPackages attribute, so that all annotated
 * classes in such a package are automatically included.
 * <li> Default filters can be specified, which will be applied to all Hibernate sessions
 * when they're created. This uses {@link FilteringSessionFactory}.
 * </ul>
 * <p>
 * The default Configuration class is {@link ChadoAnnotationConfiguration}, and the data source
 * is set appropriately.
 *
 * @author rh11
 */
public class ChadoSessionFactoryBean extends AnnotationSessionFactoryBean {
    private static final Logger logger = Logger.getLogger(AnnotationSessionFactoryBean.class);

    public ChadoSessionFactoryBean() {
        setConfigurationClass(ChadoAnnotationConfiguration.class);
    }

    private String[] annotatedPackages;
    private Class<?>[] annotatedClasses;
    private ClassLoader beanClassLoader;

    private String[] defaultFilters = new String[0];

    @Override
    public void setAnnotatedPackages(String[] annotatedPackages) {
        this.annotatedPackages = annotatedPackages;
        super.setAnnotatedPackages(annotatedPackages);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setAnnotatedClasses(Class[] annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

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
        if (cfg instanceof ChadoAnnotationConfiguration)
            ((ChadoAnnotationConfiguration) cfg).setDataSource(getDataSource());
        return cfg;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (annotatedPackages != null)
            addClassesFromAnnotatedPackages();
        super.afterPropertiesSet();
    }


    private void addClassesFromAnnotatedPackages() {
        for (String packageName: annotatedPackages)
            addClassesFromAnnotatedPackage(packageName);
    }

    private void addClassesFromAnnotatedPackage(String packageName) {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter( new AnnotationTypeFilter(Entity.class) );
        scanner.addIncludeFilter( new AnnotationTypeFilter(MappedSuperclass.class) );

        List<Class<?>> foundClasses = new ArrayList<Class<?>>();
        for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
          String className = bd.getBeanClassName();
          Class<?> type = ClassUtils.resolveClassName(className, this.beanClassLoader);
          foundClasses.add(type);
          logger.info(String.format("Added class '%s'", className));
        }

        if (annotatedClasses != null)
            Collections.addAll(foundClasses, annotatedClasses);

        super.setAnnotatedClasses(foundClasses.toArray(new Class<?>[0]));
    }

    @Override
    protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
        FilteringSessionFactory filteringSessionFactory = new FilteringSessionFactory(super.newSessionFactory(config));
        for (String filterName: defaultFilters)
            filteringSessionFactory.addFilter(filterName);
        return filteringSessionFactory;
    }

}
