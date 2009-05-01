package org.genedb.db.loading.auxiliary;

import static org.junit.Assert.*;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.SortedSet;

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
    public void testInterProDomain() {
        SessionFactory sessionFactory = loader.getSessionFactory();
        Session session = SessionFactoryUtils.getSession(sessionFactory, true);
        try {
            Polypeptide polypeptide = (Polypeptide) session.createQuery(
                "from Polypeptide where uniquename = 'PFC0495w:pep'"
            ).uniqueResult();

            assertNotNull(polypeptide);
            assertEquals("PFC0495w:pep", polypeptide.getUniqueName());

            SortedSet<PolypeptideDomain> domains = polypeptide.getDomains();
            assertNotNull(domains);
            assertEquals(1, domains.size());

            PolypeptideDomain domain = domains.first();
            assertNotNull(domain);
            assertEquals("PFC0495w:pep:InterPro:IPR001461", domain.getUniqueName());

            DbXRef dbxref = domain.getDbXRef();
            assertNotNull(dbxref);
            assertEquals("PRINTS", dbxref.getDb().getName());
            assertEquals("PR00792", dbxref.getAccession());

            Collection<FeatureDbXRef> featureDbXRefs = domain.getFeatureDbXRefs();
            assertNotNull(featureDbXRefs);
            assertEquals(1, featureDbXRefs.size());
            FeatureDbXRef featureDbXRef = featureDbXRefs.iterator().next();
            DbXRef interproDbXRef = featureDbXRef.getDbXRef();
            assertNotNull(interproDbXRef);
            assertEquals("InterPro", interproDbXRef.getDb().getName());
            assertEquals("IPR001461", interproDbXRef.getAccession());

        } finally {
            SessionFactoryUtils.releaseSession(session, sessionFactory);
        }
    }

    @Test
    public void testNonInterProDomain() {
        SessionFactory sessionFactory = loader.getSessionFactory();
        Session session = SessionFactoryUtils.getSession(sessionFactory, true);
        try {
            Polypeptide polypeptide = (Polypeptide) session.createQuery(
                "from Polypeptide where uniquename = 'MAL13P1.310:pep'"
            ).uniqueResult();

            assertNotNull(polypeptide);
            assertEquals("MAL13P1.310:pep", polypeptide.getUniqueName());

            SortedSet<PolypeptideDomain> domains = polypeptide.getDomains();
            assertNotNull(domains);
            assertEquals(1, domains.size());

            PolypeptideDomain domain = domains.first();
            assertNotNull(domain);
            assertEquals("MAL13P1.310:pep:InterPro:PTHR10183:SF22", domain.getUniqueName());

            DbXRef dbxref = domain.getDbXRef();
            assertNotNull(dbxref);
            assertEquals("PANTHER", dbxref.getDb().getName());
            assertEquals("PTHR10183:SF22", dbxref.getAccession());

            // No InterPro dbxrefs
            Collection<FeatureDbXRef> featureDbXRefs = domain.getFeatureDbXRefs();
            assertNotNull(featureDbXRefs);
            assertEquals(0, featureDbXRefs.size());

        } finally {
            SessionFactoryUtils.releaseSession(session, sessionFactory);
        }
    }

    @AfterClass @SuppressWarnings("deprecation")
    public static void shutdownDatabase() throws HibernateException, SQLException {
        Session session = SessionFactoryUtils.getSession(loader.getSessionFactory(), true);

        // When session.connection() is deprecated, change to use doWork(Work)
        // and remove the @SuppressWarnings("deprecation").
        session.connection().createStatement().execute("shutdown");
    }
}
