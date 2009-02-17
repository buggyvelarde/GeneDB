package org.genedb.db.loading.auxiliary;

import static org.junit.Assert.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.IOException;
import java.sql.SQLException;

public class InterProLoaderTest {
    private static InterProLoader loader;

    @BeforeClass
    public static void setup() throws IOException, HibernateException, SQLException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"Load.xml", "AuxTest.xml"});

        loader = ctx.getBean("iploader", InterProLoader.class);
        assertTrue(loader.processOptionIfValid("key-type", "polypeptide"));
        
        loader.clear("Pfalciparum");
        new Load(loader).load("test/data/Pfalciparum-20090107-subset.interpro");
    }
    
    @Test
    public void test() {
        
    }

    @AfterClass @SuppressWarnings("deprecation")
    public static void shutdownDatabase() throws HibernateException, SQLException {
        Session session = SessionFactoryUtils.getSession(loader.getSessionFactory(), true);
        
        // When session.connection() is deprecated, change to use doWork(Work)
        // and remove the @SuppressWarnings("deprecation").
        session.connection().createStatement().execute("shutdown");
    }
}
