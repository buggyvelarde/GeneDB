package org.genedb.db.audit;

import org.genedb.db.test.tools.BuildTestDatabase;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.test.HibernateTest;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.UserSuppliedConnectionProvider;
import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

public class HibernateChangeTrackerTest {
    private static SessionFactory sessionFactory;
    private static jdbcDataSource dataSource;
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
    public void test() {

    }

}
