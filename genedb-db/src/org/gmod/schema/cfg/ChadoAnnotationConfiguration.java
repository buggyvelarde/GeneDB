package org.gmod.schema.cfg;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SingleTableSubclass;

/**
 * Extends org.hibernate.cfg.AnnotationConfiguration with support for the <code>@FeatureType</code> annotation.
 *
 * If you use this directly, you must set the data source. With Spring, use {@link ChadoSessionFactoryBean},
 * which defaults to this configuration and automatically sets the data source.
 *
 * @author rh11
 */
public class ChadoAnnotationConfiguration extends AnnotationConfiguration {
    private static final Logger logger = Logger.getLogger(ChadoAnnotationConfiguration.class);

    private DataSource dataSource;
    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public SessionFactory buildSessionFactory() throws HibernateException {
        @SuppressWarnings("unchecked")
        Map<String,PersistentClass> classes = this.classes;

        for (Map.Entry<String,PersistentClass> e: classes.entrySet()) {
            String className = e.getKey();
            PersistentClass persistentClass = e.getValue();
            if (!(persistentClass instanceof SingleTableSubclass))
                continue;

            logger.trace(String.format("Processing class '%s'", className));
            Class<?> mappedClass = persistentClass.getMappedClass();
            for (Annotation annotation: mappedClass.getDeclaredAnnotations()) {
                if (annotation instanceof FeatureType) {
                    FeatureType featureType = (FeatureType) annotation;

                    validate(className, featureType);
                    Integer cvTermId = getCvTermIdForFeatureType(featureType);
                    if (cvTermId == null)
                        throw new HibernateException(String.format("Failed to initialise class '%s': could not find %s",
                            className, description(featureType)));

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

        if ("".equals(term) && "".equals(accession))
            throw new ChadoAnnotationException(String.format("@FeatureType annotation for class '%s' has neither 'term' nor 'accession'", className));

        if (!"".equals(term) && !"".equals(accession))
            throw new ChadoAnnotationException(String.format("@FeatureType annotation for class '%s' has both 'term' and 'accession'", className));
    }

    private String description(FeatureType featureType) {
        if ("".equals(featureType.accession()))
            return String.format("term '%s' in CV '%s'", featureType.term(), featureType.cv());
        else
            return String.format("accession number '%s' in CV '%s'", featureType.accession(), featureType.cv());
    }

    private Integer getCvTermIdForFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        if ("".equals(featureType.accession()))
            return getCvTermIdForTermFeatureType(featureType);
        else
            return getCvTermIdForAccessionFeatureType(featureType);
    }

    private Integer getCvTermIdForTermFeatureType(FeatureType featureType) throws ChadoAnnotationException {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("select cvterm_id from cvterm join cv using (cv_id) where cv.name = ? and cvterm.name = ?");
            try {
                st.setString(1, featureType.cv());
                st.setString(2, featureType.term());
                ResultSet rs = st.executeQuery();
                if (!rs.next())
                    return null;
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
                if (!rs.next())
                    return null;
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
