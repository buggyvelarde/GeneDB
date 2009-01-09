package org.genedb.db.loading;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.loading.EmblLoader.OverwriteExisting;

import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * A utility class to help in writing tests for the EMBL loader.
 * An instance of this class corresponds to a single input EMBL file,
 * and utility methods make it easy to test specific properties of
 * the loaded data. See the test classes {@link EmblLoaderBergheiTest}
 * and {@link EmblLoaderMansoniTest} for examples of its use.
 * <p>
 * Note that this class explicitly manages the Hibernate session,
 * so your test class should <b>not</b> be annotated as <code>@Transactional</code>.
 *
 * @author rh11
 *
 */
public class EmblLoaderTestHelper {
    private static final Logger logger = TestLogger.getLogger(EmblLoaderTestHelper.class);

    private EmblLoader loader;
    private SessionFactory sessionFactory;
    private OrganismDao organismDao;

    private Session session;
    private String filename;

    private EmblLoaderTestHelper() {
        // empty
    }

    /**
     * Create a test helper for the given organism and filename. This loads the given
     * file into the organism, and returns a helper object that should be used to test
     * that the data have been loaded as expected.
     * <p>
     * This method should usually be called from a <code>@BeforeClass</code> method of
     * the test class.
     * <p>
     * The <code>cleanUp</code> method <strong>must</strong> be called when you have finished
     * with the object.
     *
     * @param organismCommonName the common name of the organism
     * @param filename the filename
     * @return the test helper, which is used to test that the data have been loaded as expected
     * @throws IOException if there is a problem reading the file
     * @throws ParsingException if there is a problem interpreting the file
     */
    public static EmblLoaderTestHelper create(String organismCommonName, String filename)
        throws IOException, ParsingException
    {
        return create(organismCommonName, null, null, null, filename);
    }

    /**
     * Create a test helper for the given organism and filename. This loads the given
     * file into the organism, and returns a helper object that should be used to test
     * that the data have been loaded as expected.
     * <p>
     * This method will create the organism, if there is not already an organism with the
     * given common name.
     * <p>
     * This method should usually be called from a <code>@BeforeClass</code> method of
     * the test class.
     * <p>
     * The <code>cleanUp</code> method <strong>must</strong> be called when you have finished
     * with the object.
     *
     * @param organismCommonName the common name of the organism. If there is already an organism
     *                          with the given common name, that organism is used.
     * @param organismGenus the genus assigned to the organism, if a new organism is created (because
     *                          there is no organism with the given common name in the skeleton database)
     * @param organismSpecies the species name assigned to the organism, if a new organism is created (because
     *                          there is no organism with the given common name in the skeleton database)
     * @param organismStrain the strain name assigned to the organism, if a new organism is created (because
     *                          there is no organism with the given common name in the skeleton database)
     * @param filename the filename
     * @return the test helper, which is used to test that the data have been loaded as expected
     * @throws IOException if there is a problem reading the file
     * @throws ParsingException if there is a problem interpreting the file
     */
    public static EmblLoaderTestHelper create(String organismCommonName, String organismGenus,
            String organismSpecies, String organismStrain, String filename)
                throws IOException, ParsingException {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});
        EmblLoaderTestHelper helper = applicationContext.getBean("emblLoaderTestHelper", EmblLoaderTestHelper.class);

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

        loader.setReportUnusedQualifiers(true);
        loader.setOrganismCommonName(organismCommonName);
        loadFile(filename);
        this.filename = filename;
    }

    /**
     * Reload the same file as before, with the <tt>overwriteExisting</tt> flag set.
     *
     * @throws ParsingException
     * @throws IOException
     */
    public void reload() throws IOException, ParsingException {
        if (this.filename == null) {
            throw new IllegalStateException("Filename not set");
        }
        logger.info("Reloading file: " + filename);
        loader.setOverwriteExisting(OverwriteExisting.YES);
        loadFile(filename);
    }

    /**
     * Close the session and shut down the database.
     * This method should be called once the tests are complete.
     * It should ordinarily be called from an <code>@AfterClass</code> method
     * of the test class.
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
        session.createSQLQuery("shutdown").executeUpdate();
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

    /**
     * Create a FeatureTester for the loaded data.
     *
     * @return
     */
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
