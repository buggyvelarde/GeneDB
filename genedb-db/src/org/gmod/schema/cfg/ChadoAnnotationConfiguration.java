package org.gmod.schema.cfg;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SingleTableSubclass;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.sql.DataSource;

/**
 * Extends org.hibernate.cfg.AnnotationConfiguration in two ways:
 * <ul>
 * <li> Adds support for the {@link FeatureType} annotation.
 * <li> Extends the behaviour of {@link #addPackage} to add all annotated classes in the package.
 * </ul>
 * If you use this directly, you must set the data source. From Spring, use {@link ChadoSessionFactoryBean}
 * instead, which defaults to this configuration and automatically sets the data source.
 * <p>
 * The class {@link org.genedb.hibernate.search.IndexChado}, from the HibernateSearch
 * module, uses this class directly. It may serve as an example of how it can be used.
 * (Not that there's any magic to it: just use it in place of the ordinary Hibernate
 * <code>Configuration</code> class, and remember to set the data source.)
 *
 * @author rh11
 */
public class ChadoAnnotationConfiguration extends AnnotationConfiguration {
    private static final Logger logger = Logger.getLogger(ChadoAnnotationConfiguration.class);

    private DataSource dataSource;
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private ClassLoader classLoader = getClass().getClassLoader();
    /**
     * Set the class loader to use for locating the classes within a
     * package, when <code>addPackage</code> is called. The default,
     * if this method is not called, is to use the class loader that was
     * used to load this class.
     *
     * @param classLoader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Adds the specified package, reading any package metadata, and
     * <b>also</b> adds all annotated classes from this package.
     */
    @Override
    public AnnotationConfiguration addPackage(String packageName) throws MappingException {
        logger.info("Adding package: " + packageName);
        addClassesFromAnnotatedPackage(packageName);
        super.addPackage(packageName);
        return this;
    }

    private void addClassesFromAnnotatedPackage(String packageName) {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter( new AnnotationTypeFilter(Entity.class) );
        scanner.addIncludeFilter( new AnnotationTypeFilter(MappedSuperclass.class) );

        for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
          String className = bd.getBeanClassName();
          Class<?> type = ClassUtils.resolveClassName(className, this.classLoader);
          addAnnotatedClass(type);
          logger.info(String.format("Added class '%s'", className));
        }
    }

    @Override
    public SessionFactory buildSessionFactory() throws HibernateException {
        logger.debug("Building session factory");
        buildMappings();

        /* Not quite as redundant as it looks: the <code>classes</code>
         * field is declared as a plain old non-generic Map. */
        @SuppressWarnings("unchecked")
        Map<String,PersistentClass> classes = this.classes;

        for (Map.Entry<String,PersistentClass> e: classes.entrySet()) {
            String className = e.getKey();
            PersistentClass persistentClass = e.getValue();
            logger.trace(String.format("Inspecting class '%s'", className));
            if (!(persistentClass instanceof SingleTableSubclass)) {
                continue;
            }

            logger.trace(String.format("Processing class '%s'", className));
            Class<?> mappedClass = persistentClass.getMappedClass();
            for (Annotation annotation: mappedClass.getDeclaredAnnotations()) {
                if (annotation instanceof FeatureType) {
                    FeatureType featureType = (FeatureType) annotation;

                    validate(className, featureType);
                    Integer cvTermId = getCvTermIdForFeatureType(featureType);
                    if (cvTermId == null) {
                        throw new HibernateException(String.format("Failed to initialise class '%s': could not find %s",
                            className, description(featureType)));
                    }

                    logger.debug(String.format("Setting discriminator column of '%s' to %d (for %s)",
                        className, cvTermId, description(featureType)));
                    persistentClass.setDiscriminatorValue(String.valueOf(cvTermId));
                }
            }
        }

        return super.buildSessionFactory();
    }

    private void validate(String className, FeatureType featureType) throws HibernateException {
        String term = featureType.term();
        String accession = featureType.accession();

        if ("".equals(term) && "".equals(accession)) {
            throw new ChadoAnnotationException(String.format("@FeatureType annotation for class '%s' has neither 'term' nor 'accession'", className));
        }
            
        if (!"".equals(term) && !"".equals(accession)) {
            throw new ChadoAnnotationException(String.format("@FeatureType annotation for class '%s' has both 'term' and 'accession'", className));
        }
    }

    private String description(FeatureType featureType) {
        if ("".equals(featureType.accession())) {
            return String.format("term '%s' in CV '%s'", featureType.term(), featureType.cv());
        } else {
            return String.format("accession number '%s' in CV '%s'", featureType.accession(), featureType.cv());
        }
    }

    private Integer getCvTermIdForFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        if ("".equals(featureType.accession())) {
            return getCvTermIdForTermFeatureType(featureType);
        } else {
            return getCvTermIdForAccessionFeatureType(featureType);
        }
    }

    private Integer getCvTermIdForTermFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("select cvterm_id from cvterm join cv using (cv_id) where cv.name = ? and cvterm.name = ?");
            try {
                st.setString(1, featureType.cv());
                st.setString(2, featureType.term());
                ResultSet rs = st.executeQuery();
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt(1);
            }
            finally {
                try { st.close(); conn.close(); } catch (SQLException e) { logger.error(e); }
            }

        }
        catch (SQLException e) {
            throw new ChadoAnnotationException(e);
        }
    }

    private Integer getCvTermIdForAccessionFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("select cvterm_id from cvterm join cv using (cv_id) join dbxref using (dbxref_id) where cv.name = ? and dbxref.accession = ?");
            try {
                st.setString(1, featureType.cv());
                st.setString(2, featureType.accession());
                ResultSet rs = st.executeQuery();
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt(1);
            }
            finally {
                try { st.close(); conn.close(); } catch (SQLException e) { logger.error(e); }
            }

        }
        catch (SQLException e) {
            throw new ChadoAnnotationException(e);
        }
    }
}
