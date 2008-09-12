package org.gmod.schema.test;

import static org.junit.Assert.assertNotNull;

import org.genedb.db.test.tools.BuildTestDatabase;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.feature.AbstractGene;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.UserSuppliedConnectionProvider;
import org.hibernate.metadata.ClassMetadata;
import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class HibernateTest {

    private static SessionFactory sessionFactory;
    private static jdbcDataSource dataSource;
    private Session session;
    private static Logger logger;

    public static void configureLogging() {
        URL url = BuildTestDatabase.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);

        logger = Logger.getLogger(HibernateTest.class);
    }

    @BeforeClass
    public static void createSessionFactory() {
        configureLogging();

        dataSource = new org.hsqldb.jdbc.jdbcDataSource();
        dataSource.setDatabase("jdbc:hsqldb:file:test-data/hsqldb/pfalciparum");
        dataSource.setUser("sa");

        Configuration cfg = new ChadoAnnotationConfiguration()
        .setDataSource(dataSource)
        .addPackage("org.gmod.schema.mapped")
        .addPackage("org.gmod.schema.feature")
        .setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
        .setProperty("hibernate.search.default.indexBase", "/tmp")
        .setProperty(Environment.CONNECTION_PROVIDER, UserSuppliedConnectionProvider.class.getName());

        sessionFactory = cfg.buildSessionFactory();
    }

    @Test
    public void testConnection() throws SQLException {
        assertNotNull(dataSource.getConnection());
    }

    @Before
    public void getSession() throws SQLException {
        this.session = sessionFactory.openSession(dataSource.getConnection());
    }

    @After
    public void closeSession() {
        session.close();
    }

    @Test
    public void loadMappedEntities() {
        @SuppressWarnings("unchecked")
        Map<String,ClassMetadata> classMetadataByName = sessionFactory.getAllClassMetadata();
        for (String name: classMetadataByName.keySet()) {
            logger.info(name);
            session.createQuery("from " + name).setMaxResults(100).list();
        }
    }

    @Test
    public void fetchGeneStarts() {
        @SuppressWarnings("unchecked")
        List<AbstractGene> genes = session.createCriteria(AbstractGene.class).setMaxResults(100).list();
        for (AbstractGene gene: genes) {
            logger.info(String.format("Gene '%s' starts at %d", gene.getUniqueName(), gene.getStart()));
        }
    }
}
