package org.genedb.db.loading.auxiliary;

import org.hibernate.HibernateException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;

public class InterProLoaderTest {
    private static InterProLoader loader;
    private static Load load;

    @BeforeClass
    public static void setup() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"Load.xml", "AuxTest.xml"});

        loader = ctx.getBean("iploader", InterProLoader.class);
        load = new Load(loader);
        loader.processOptionIfValid("key-type", "polypeptide");
    }

    @Test
    public void load() throws IOException, HibernateException, SQLException {
        loader.clear("Pfalciparum");
        load.load("test/data/Pfalciparum_domains_subset.interpro");
    }
}
