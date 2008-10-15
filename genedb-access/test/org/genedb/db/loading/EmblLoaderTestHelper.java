package org.genedb.db.loading;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
    private ApplicationContext applicationContext;
    private EmblLoader loader;
    private SessionFactory sessionFactory;
    private Session session;

    static {
        URL url = EmblLoaderTestHelper.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    public EmblLoaderTestHelper(String organismCommonName, String filename) throws IOException, ParsingException {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});

        this.loader = (EmblLoader) applicationContext.getBean("emblLoader", EmblLoader.class);
        loader.setOrganismCommonName(organismCommonName);

        this.sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory", SessionFactory.class);
        this.session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        EmblFile emblFile = new EmblFile(new File(filename));
        loader.load(emblFile);
    }

    public FeatureTester tester() {
        return new FeatureTester(session);
    }

}
