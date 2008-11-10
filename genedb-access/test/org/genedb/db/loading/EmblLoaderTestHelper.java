package org.genedb.db.loading;

import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A utility class to help in writing tests for the EMBL loader.
 * An instance of this class corresponds to a single input EMBL file,
 * and utility methods make it easy to test specific properties of
 * the loaded data. See the test classes {@link EmblLoaderBergheiTest}
 * and {@link EmblLoaderMansoniTest} for examples of its use.
 *
 * @author rh11
 *
 */
public class EmblLoaderTestHelper {
    private static final Logger logger = Logger.getLogger(EmblLoaderTestHelper.class);

    private EmblLoader loader;
    private SessionFactory sessionFactory;
    private OrganismDao organismDao;

    private Session session;

    static {
        URL url = EmblLoaderTestHelper.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    EmblLoaderTestHelper() {
        // empty
    }

    public static EmblLoaderTestHelper create(String organismCommonName, String filename)
        throws IOException, ParsingException
    {
        return create(organismCommonName, null, null, null, filename);
    }

    public static EmblLoaderTestHelper create(String organismCommonName, String organismGenus,
            String organismSpecies, String organismStrain, String filename)
                throws IOException, ParsingException {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});
        EmblLoaderTestHelper helper = (EmblLoaderTestHelper) applicationContext.getBean("emblLoaderTestHelper", EmblLoaderTestHelper.class);

        helper.doLoad(organismCommonName, organismGenus,
                organismSpecies, organismStrain, filename);
        return helper;
    }

    private void doLoad(String organismCommonName, String organismGenus,
            String organismSpecies, String organismStrain, String filename)
    throws IOException, ParsingException {

        if (organismGenus != null) {
            createOrganismIfNecessary(organismCommonName, organismGenus,
                organismSpecies, organismStrain);
        }

        loader.setOrganismCommonName(organismCommonName);
        loadFile(filename);
    }

    /**
     * Shut down the database and close the session.
     */
    public void cleanUp() {
        /*
         * If the database is not shutdown, the changes will not be
         * persisted to the data file. It's useful to be able to inspect
         * the loaded data directly sometimes, so it's important to do
         * this. (The HSLQDB documentation suggests that comitted changes
         * will never be lost even if the database is not shut down. That
         * does not appear to be true.)
         */
        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                logger.debug("Shutting down database");
                connection.createStatement().execute("shutdown");
            }
        });
        SessionFactoryUtils.releaseSession(session, sessionFactory);
        session = null;
    }

    @Transactional
    private void createOrganismIfNecessary(String organismCommonName,
            String organismGenus,String organismSpecies, String organismStrain)
    {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        Organism organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism != null) {
            logger.debug(String.format("Organism '%s' already exists", organismCommonName));
            return;
        }
        if (organismStrain != null) {
            organismSpecies += " " + organismStrain;
        }
        logger.debug(String.format("Creating organism '%s' (%s %s)",
            organismCommonName, organismGenus, organismSpecies));
        organism = new Organism(organismGenus, organismSpecies, organismCommonName,
            organismCommonName, null);
        session.persist(organism);
        session.flush();
    }

    @Transactional
    private void loadFile(String filename) throws IOException, ParsingException,
            FileNotFoundException, DataError {
        File file = new File(filename);
        EmblFile emblFile = new EmblFile(file, new FileReader(file));
        loader.load(emblFile);
        logger.debug("Finished loading");
    }

    public FeatureTester tester() {
        return new FeatureTester(session);
    }

    /* Setters for Spring injection */
    public void setLoader(EmblLoader loader) {
        this.loader = loader;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.session = SessionFactoryUtils.getSession(sessionFactory, true);
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

}
