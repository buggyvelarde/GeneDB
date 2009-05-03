package org.gmod.schema.cfg;

import org.gmod.schema.mapped.Feature;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.sql.DataSource;

/**
 * Extends org.hibernate.cfg.AnnotationConfiguration in two ways:
 * <ul>
 * <li> Adds support for the {@link FeatureType} annotation.
 * <li> Extends the behaviour of {@link #addPackage} to add all annotated classes in the package.
 * </ul>
 * If you use this directly, you must set the data source. (In that case you may
 * also want to use the <code>InjectedDataSourceConnectionProvider</code>; see
 * {@link ChadoAnnotationSettingsFactory} for more information.)
 * From Spring, use {@link ChadoSessionFactoryBean}
 * instead, which defaults to this configuration and automatically sets the data source.
 *
 * @author rh11
 */
public class ChadoAnnotationConfiguration extends AnnotationConfiguration {
    private static final Logger logger = Logger.getLogger(ChadoAnnotationConfiguration.class);

    private DataSource dataSource;

    public ChadoAnnotationConfiguration() {
        super(new ChadoAnnotationSettingsFactory());

        /*
         * Why use ChadoAnnotationSettingsFactory here?
         *
         * The ChadoAnnotationConfiguration needs access to the database, so it can
         * look up the CV terms that are referenced in the @FeatureType annotation,
         * and set the discriminator value to the appropriate term ID.
         *
         * If you use Hibernate via Spring, then Spring will use its
         * LocalDatasourceConnectionProvider to provide database connections to Hibernate
         * from the DataSource supplied to the SessionFactoryBean; the ChadoSessionFactoryBean
         * additionally passes this DataSource object to the ChadoAnnotationConfiguration.
         *
         * The problem comes if you use ChadoAnnotationConfiguration not from Spring.
         * You need to supply a DataSource to the ChadoAnnotationConfiguration, but Hibernate
         * cannot use this DataSource to obtain its connections. (There is
         * a DataSourceConnectionProvider, but that requires the DataSource to be
         * registered with JNDI.)
         *
         * The natural solution is to define a new ConnectionProvider class which uses
         * a DataSource to provide connections (but doesn't rely on getting that object
         * from JNDI) and then to arrange for the ChadoAnnotationConfiguration to make
         * its DataSource available to this ConnectionProvider. The only way I can see
         * to do the second part is to define yet another class, this time extending
         * SettingsFactory, which the ChadoAnnotationConfiguration uses. This
         * SettingsFactory can then override the createConnectionProvider method and pass
         * the DataSource to the ConnectionProvider that it creates.
         *
         * Fortuitously it turns out that there is already a suitable ConnectionProvider
         * defined in the Hibernate EJB3 code,
         * org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider, so I've used
         * that rather than reimplement it. The new SettingsFactory is, obviously,
         * ChadoAnnotationSettingsFactory.
         *
         * In fact we don't even attempt to influence the choice of ConnectionProvider,
         * because Spring (for example) still needs to be free to make its own choices.
         * The ChadoAnnotationSettingsFactory simply checks which connection provider is
         * being used, and injects the DataSource only if it finds an
         * InjectedDataSourceConnectionProvider.
         */

    }

    public ChadoAnnotationConfiguration setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        ((ChadoAnnotationSettingsFactory) settingsFactory).setDataSource(dataSource);
        return this;
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

    private Map<Class<? extends Feature>, Set<Integer>> typeIdsByClass
        = new HashMap<Class<? extends Feature>, Set<Integer>>();

    private void recordTypeId(int typeId, Class<?> mappedClass) {
        if (!Feature.class.isAssignableFrom(mappedClass)) {
            throw new RuntimeException(String.format(
                "Class '%s' has a @FeatureType annotation, but is not a subclass of Feature",
                mappedClass));
        }

        Class<? extends Feature> featureClass = mappedClass.asSubclass(Feature.class);

        boolean finished = false;
        do {
            logger.trace(String.format("Class '%s' can be represented by CV term %d", featureClass, typeId));
            if (!typeIdsByClass.containsKey(featureClass)) {
                typeIdsByClass.put(featureClass, new HashSet<Integer>());
            }
            typeIdsByClass.get(featureClass).add(typeId);

            Class<?> superclass = featureClass.getSuperclass();
            if (superclass != null && Feature.class.isAssignableFrom(superclass)) {
                featureClass = superclass.asSubclass(Feature.class);
            } else {
                finished = true;
            }
        } while (!finished);
    }

    /**
     * Get the type IDs that represent this class or a subclass.
     *
     * @param featureClass the class of Feature
     * @return a collection of CvTerm IDs
     */
    public Collection<Integer> getTypeIdsByClass(Class<? extends Feature> featureClass) {
        if (!typeIdsByClass.containsKey(featureClass)) {
            throw new RuntimeException(String.format(
                "Neither the Feature class '%s', nor any of its subclasses, has a @FeatureType annotation",
                featureClass));
        }
        return typeIdsByClass.get(featureClass);
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
            FeatureType featureType = FeatureTypeUtils.getFeatureTypeForClass(mappedClass);
            if (featureType != null) {
                Integer cvTermId = getCvTermIdForFeatureType(featureType);
                if (cvTermId == null) {
                    throw new HibernateException(String.format("Failed to initialise class '%s': could not find %s",
                        className, description(featureType)));
                }

                logger.debug(String.format("Setting discriminator column of '%s' to %d (for %s)",
                    className, cvTermId, description(featureType)));
                persistentClass.setDiscriminatorValue(String.valueOf(cvTermId));
                recordTypeId(cvTermId, mappedClass);
            }
        }

        return super.buildSessionFactory();
    }

    private String description(FeatureType featureType) {
        if ("".equals(featureType.accession())) {
            return String.format("term '%s' in CV '%s'", featureType.term(), featureType.cv());
        }
        return String.format("accession number '%s' in CV '%s'", featureType.accession(), featureType.cv());
    }

    private Integer getCvTermIdForFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        if ("".equals(featureType.accession())) {
            return getCvTermIdForTermFeatureType(featureType);
        }
        return getCvTermIdForAccessionFeatureType(featureType);
    }

    private Integer getCvTermIdForTermFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement(
                "select cvterm_id" +
                " from cvterm" +
                " join cv on cvterm.cv_id = cv.cv_id" +
                " where cv.name = ?" +
                " and cvterm.name = ?");
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
            PreparedStatement st = conn.prepareStatement(
                "select cvterm_id" +
                " from cvterm" +
                " join cv on cvterm.cv_id = cv.cv_id" +
                " join dbxref on cvterm.dbxref_id = dbxref.dbxref_id" +
                " where cv.name = ?" +
                " and dbxref.accession = ?");
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
