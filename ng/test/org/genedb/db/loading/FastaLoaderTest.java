package org.genedb.db.loading;

import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Supercontig;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Transactional(rollbackFor=Throwable.class)
public class FastaLoaderTest {

    static { TestLogger.configure(); }

    private ApplicationContext applicationContext;
    private FastaLoader loader;
    private SessionFactory sessionFactory;
    private Session session;
    private FeatureTester tester;

    private static final String organismCommonName = "dummy";
    private static final String filename = "test/data/test1.fasta";

    public FastaLoaderTest() throws IOException {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});

        this.loader = applicationContext.getBean("fastaLoader", FastaLoader.class);
        loader.setOrganismCommonName(organismCommonName);

        this.sessionFactory = applicationContext.getBean("sessionFactory", SessionFactory.class);
        this.session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        FastaFile fastaFile = new FastaFile(new FileReader(new File(filename)));
        loader.load("test1", fastaFile);

        tester = new FeatureTester(session);
    }

    @Test
    public void test() {
        tester.uniqueNames(Supercontig.class, "test1");
        tester.uniqueNames(Contig.class, "contig1", "contig2", "contig3", "contig4");

        tester.tlfTester(Supercontig.class, "test1")
            .residues("gattacagaaacatgtaatttaactgatggtattggagtagcaccgcccaattgtagctgctacactgttgcacttcttata" +
                      "tgacccagtatagctgtgatattcactcctataggttccatcataattgaccagagccagattaca");

        tester.tlfTester(Contig.class, "contig1")
            .seqLen(7)
            .residues("gattaca")
            .loc(0, 0, 7);

        tester.tlfTester(Contig.class, "contig3")
        .seqLen(0)
        .residues("")
        .loc(0, 141, 141);
}
}
