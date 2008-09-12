package org.genedb.db.loading.alternative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class TestLoader {
    private ApplicationContext applicationContext;
    private EmblLoader loader;
    private SessionFactory sessionFactory;
    private Session session;

    static {
        URL url = TestLoader.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    public TestLoader(String organismCommonName, String filename) throws IOException, ParsingException {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});

        this.loader = (EmblLoader) applicationContext.getBean("emblLoader", EmblLoader.class);
        loader.setOrganismCommonName(organismCommonName);

        this.sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory", SessionFactory.class);
        this.session = SessionFactoryUtils.doGetSession(sessionFactory, true);

        EmblFile emblFile = new EmblFile(new File(filename));
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

    private void assertLoc(FeatureLoc loc, int strand, int fmin, int fmax) {
        assertEquals(strand, loc.getStrand().shortValue());
        assertEquals(fmin, loc.getFmin().intValue());
        assertEquals(fmax, loc.getFmax().intValue());
    }

    public void uniqueNames(Class<? extends Feature> featureClass, String... expectedUniqueNames) {
        @SuppressWarnings("unchecked")
        List<String> actualUniqueNames = session.createCriteria(featureClass)
            .setProjection(Projections.property("uniqueName")).list();
        assertSetEquals(actualUniqueNames, expectedUniqueNames);
    }

    public void names(Class<? extends Feature> featureClass, String... expectedNames) {
        @SuppressWarnings("unchecked")
        List<String> actualNames = session.createCriteria(featureClass)
            .setProjection(Projections.property("name")).list();
        assertSetEquals(actualNames, expectedNames);
    }

    public GeneTester geneTester(String uniqueName) {
        return new GeneTester(uniqueName);
    }

    class GeneTester {
        private AbstractGene gene;
        private GeneTester(String uniqueName) {
            Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);
            gene = (AbstractGene) session.createCriteria(AbstractGene.class)
                        .add(Restrictions.eq("uniqueName", uniqueName))
                        .uniqueResult();
            assertNotNull(gene);
            boundaries();
        }

        private GeneTester boundaries() {
            int strand = 0, fmin = Integer.MAX_VALUE, fmax = Integer.MIN_VALUE;
            for (Transcript transcript: gene.getTranscripts()) {
                assertEquals(gene.getPrimarySourceFeature(), transcript.getPrimarySourceFeature());

                int tStrand = transcript.getStrand();
                int tFmin = transcript.getFmin();
                int tFmax = transcript.getFmax();

                if (tFmin < fmin) {
                    fmin = tFmin;
                }
                if (tFmax > fmax) {
                    fmax = tFmax;
                }
                if (tStrand == 0) {
                    fail(String.format("Transcript '%s' of gene '%s' has no strand direction",
                        transcript.getUniqueName(), gene.getUniqueName()));
                }
                else if (strand == 0) {
                    strand = tStrand;
                }
                else if (strand != tStrand) {
                    fail(String.format("Gene '%s' has inconsistent strand directions on its transcripts"));
                }
            }
            assertEquals(gene.getFmin(), fmin);
            assertEquals(gene.getFmax(), fmax);
            assertEquals(gene.getStrand(), strand);
            return this;
        }

        public GeneTester loc(int strand, int fmin, int fmax) {
            assertLoc(gene.getRankZeroFeatureLoc(), strand, fmin, fmax);
            return this;
        }

        public GeneTester source(String sourceUniqueName) {
            assertEquals(sourceUniqueName, gene.getRankZeroFeatureLoc().getSourceFeature().getUniqueName());
            return this;
        }

        public GeneTester transcripts(String... uniqueNames) {
            assertFeatureUniqueNamesEqual(gene.getTranscripts(), uniqueNames);
            return this;
        }

        public TranscriptTester transcript(String uniqueName) {
            for (Transcript transcript: gene.getTranscripts()) {
                if (transcript.getUniqueName().equals(uniqueName)) {
                    return new TranscriptTester(transcript);
                }
            }
            fail(String.format("Transcript '%s' not found on gene '%s'", uniqueName, gene.getUniqueName()));
            return null; // silly compiler
        }
    }

    class TranscriptTester {
        private Transcript transcript;
        private TranscriptTester(Transcript transcript) {
            this.transcript = transcript;
        }

        public TranscriptTester synonyms(String synonymType, String... expectedUniqueNames) {
            assertSynonymNamesEqual(transcript.getSynonyms(synonymType), expectedUniqueNames);
            return this;
        }

        public TranscriptTester polypeptide(String uniqueName) {
            assertTrue (transcript instanceof ProductiveTranscript);
            ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;

            Polypeptide polypeptide = productiveTranscript.getProtein();
            assertEquals(uniqueName, polypeptide.getUniqueName());

            SortedSet<AbstractExon> exons = productiveTranscript.getComponents(AbstractExon.class);
            assertLoc(polypeptide.getRankZeroFeatureLoc(), transcript.getStrand(),
                exons.first().getFmin(), exons.last().getFmax());

            return this;
        }

        public TranscriptTester singleExon(int strand, int fmin, int fmax) {
            Collection<TranscriptRegion> components = transcript.getComponents();
            assertEquals(1, components.size());
            TranscriptRegion region = components.iterator().next();
            assertTrue(region instanceof AbstractExon);
            AbstractExon exon = (AbstractExon) region;
            assertLoc(exon.getRankZeroFeatureLoc(), strand, fmin, fmax);
            return this;
        }

        public TranscriptTester components(String... expectedUniqueNames) {
            assertFeatureUniqueNamesEqual(transcript.getComponents(), expectedUniqueNames);
            return this;
        }
    }
}
