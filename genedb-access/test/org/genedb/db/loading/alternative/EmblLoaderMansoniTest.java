package org.genedb.db.loading.alternative;

import static org.junit.Assert.*;

import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.TranscriptRegion;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional(rollbackFor=Throwable.class)
public class EmblLoaderMansoniTest {
    private ApplicationContext applicationContext;
    private EmblLoader loader;
    private SessionFactory sessionFactory;

    //private static Logger logger;

    @BeforeClass
    public static void configureLogging() {
        URL url = EmblLoaderMansoniTest.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
        //logger = Logger.getLogger(EmblLoaderMansoniTest.class);
    }

    @Before
    public void setupAndLoad() throws IOException, ParsingException {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});

        this.loader = (EmblLoader) applicationContext.getBean("emblLoader", EmblLoader.class);
        loader.setOrganismCommonName("Smansoni");

        this.sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory", SessionFactory.class);

        EmblFile emblFile = new EmblFile(new File("test/data/Smp_scaff000604.embl"));
        loader.load(emblFile);
    }

    private <T> void assertSetEquals(Collection<T> set, T... elements) {
        assertEquals(String.format("Set %s is not of size %d", set, elements.length), elements.length, set.size());
        for (T element: elements) {
            assertTrue(String.format("Set %s does not contain element '%s'", set, element), set.contains(element));
        }
    }

    private void assertSynonymNamesEqual(Collection<Synonym> set, String... expectedNames) {
        Set<String> synonymNames = new HashSet<String>();
        for (Synonym synonym: set) {
            synonymNames.add(synonym.getName());
        }
        assertSetEquals(synonymNames, expectedNames);
    }

    private void assertFeatureUniqueNamesEqual(Collection<? extends Feature> set, String... expectedNames) {
        Set<String> featureUniqueNames = new HashSet<String>();
        for (Feature feature: set) {
            featureUniqueNames.add(feature.getUniqueName());
        }
        assertSetEquals(featureUniqueNames, expectedNames);
    }

    @Test
    public void threeGenes() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        @SuppressWarnings("unchecked")
        List<String> geneUniqueNames = session.createQuery("select uniqueName from Gene").list();
        assertSetEquals(geneUniqueNames, "Smp_097230", "Smp_097240", "Smp_175570");
    }

    @Test
    public void elevenRepeatRegions() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        @SuppressWarnings("unchecked")
        List<String> repeatRegionNames = session.createQuery("select name from RepeatRegion").list();
        assertSetEquals(repeatRegionNames, "29646.repeat000001", "29646.repeat000002",
            "29646.repeat000003", "29646.repeat000004", "29646.repeat000005", "29646.repeat000041",
            "29646.repeat000042", "29646.repeat000043", "29646.repeat000048", "29646.repeat000049",
            "29646.repeat000050");
    }

    private void assertLoc(FeatureLoc loc, int strand, int fmin, int fmax) {
        assertEquals(strand, loc.getStrand().shortValue());
        assertEquals(fmin, loc.getFmin().intValue());
        assertEquals(fmax, loc.getFmax().intValue());
    }


    @Test
    public void testSmp_097230() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        Gene gene = (Gene) session.createQuery("from Gene g where g.uniqueName = 'Smp_097230'").uniqueResult();
        assertEquals(1, gene.getTranscripts().size());
        FeatureLoc geneLoc = gene.getRankZeroFeatureLoc();
        assertLoc(geneLoc, 1, 18450, 18693);
        assertEquals("Smp_scaff000604", geneLoc.getSourceFeature().getUniqueName());

        Transcript transcript = gene.getTranscripts().iterator().next();
        assertEquals("Smp_097230:mRNA", transcript.getUniqueName());
        assertEquals(geneLoc, transcript.getRankZeroFeatureLoc());

        assertTrue(transcript instanceof ProductiveTranscript);
        ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;
        Polypeptide polypeptide = productiveTranscript.getProtein();
        assertEquals("Smp_097230:pep", polypeptide.getUniqueName());
        assertEquals(transcript.getRankZeroFeatureLoc(), polypeptide.getRankZeroFeatureLoc());

        assertSynonymNamesEqual(transcript.getSynonyms(), "29646.t000001", "29646.m000185");
        Collection<TranscriptRegion> transcriptRegions = transcript.getComponents();
        assertEquals(1, transcriptRegions.size());
        TranscriptRegion exon = transcriptRegions.iterator().next();
        assertTrue(exon instanceof AbstractExon);
        assertLoc(exon.getRankZeroFeatureLoc(), 1, 18450, 18693);
    }

    @Test
    public void test_Smp097240() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        Gene gene = (Gene) session.createQuery("from Gene g where g.uniqueName = 'Smp_097240'").uniqueResult();
        assertFeatureUniqueNamesEqual(gene.getTranscripts(), "Smp_097240.1:mRNA", "Smp_097240.2:mRNA", "Smp_097240.4:mRNA");
        FeatureLoc geneLoc = gene.getRankZeroFeatureLoc();
        assertLoc(geneLoc, -1, 18494, 22354);
        assertEquals("Smp_scaff000604", geneLoc.getSourceFeature().getUniqueName());
    }

    @Test
    public void test_Smp097240_1() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        Transcript transcript = (Transcript) session.createQuery("from Transcript t where t.uniqueName = 'Smp_097240.1:mRNA'").uniqueResult();
        assertNotNull(transcript);
        assertLoc(transcript.getRankZeroFeatureLoc(), -1, 18494, 22354);
        assertFeatureUniqueNamesEqual(transcript.getComponents(),
            "Smp_097240.1:exon:1", "Smp_097240.1:exon:2", "Smp_097240.1:exon:3", "Smp_097240.1:exon:4",
            "Smp_097240.1:exon:5", "Smp_097240.1:exon:6", "Smp_097240.1:exon:7", "Smp_097240.1:exon:8",
            "Smp_097240.1:3utr",   "Smp_097240.1:5utr:1", "Smp_097240.1:5utr:2");

        assertTrue(transcript instanceof ProductiveTranscript);
        ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;
        Polypeptide polypeptide = productiveTranscript.getProtein();
        assertEquals("Smp_097240.1:pep", polypeptide.getUniqueName());
    }
}
