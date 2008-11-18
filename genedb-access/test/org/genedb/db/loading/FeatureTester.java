package org.genedb.db.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.ProteinMatch;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.TranscriptRegion;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;


public class FeatureTester {
    private static final Logger logger = Logger.getLogger(FeatureTester.class);

    private Session session;
    public FeatureTester(Session session) {
        this.session = session;
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
        if (loc == null) {
            fail("FeatureLoc is null");
        }
        assertEquals(strand, loc.getStrand().shortValue());
        assertEquals(fmin, loc.getFmin().intValue());
        assertEquals(fmax, loc.getFmax().intValue());
    }

    public FeatureTester uniqueNames(Class<? extends Feature> featureClass, String... expectedUniqueNames) {
        @SuppressWarnings("unchecked")
        List<String> actualUniqueNames = session.createCriteria(featureClass)
            .setProjection(Projections.property("uniqueName")).list();
        assertSetEquals(actualUniqueNames, expectedUniqueNames);
        return this;
    }

    public FeatureTester names(Class<? extends Feature> featureClass, String... expectedNames) {
        @SuppressWarnings("unchecked")
        List<String> actualNames = session.createCriteria(featureClass)
            .setProjection(Projections.property("name")).list();
        assertSetEquals(actualNames, expectedNames);
        return this;
    }

    public GeneTester geneTester(String uniqueName) {
        return new GeneTester(uniqueName);
    }

    public TLFTester tlfTester(Class<? extends TopLevelFeature> tlfClass, String uniqueName) {
        return new TLFTester(tlfClass, uniqueName);
    }

    /*
     * The purpose of this rather complicated type trickery is to ensure
     * that the return type of these methods is the type of the subclass,
     * for method chaining.
     *
     * Although the definition may be a little mind-bending, it's easy to
     * use. Just define <code>class FooTester extends AbstractTester&lt;FooTester&gt;</code>,
     * and pass <code>FooTester.class</code> to the super constructor.
     */
    abstract class AbstractTester<T extends AbstractTester<T>> {
        private Class<T> ourClass;
        protected Feature feature;

        /*
         * Because generics are implemented using type erasure, we have
         * to use a Class object to explicitly pass the type information
         * from compile-time to run-time. We're using a completely constrained
         * Class object, so the compiler will only accept precisely the
         * correct Class object here.
         */
        protected AbstractTester(Class<T> ourClass, Feature feature) {
            assert ourClass.isInstance(this);
            this.ourClass = ourClass;
            this.feature = feature;
        }
        public T loc(int strand, int fmin, int fmax) {
            assertLoc(feature.getRankZeroFeatureLoc(), strand, fmin, fmax);
            return ourClass.cast(this);
        }
        public T loc(int locgroup, int rank, int strand, int fmin, int fmax) {
            assertLoc(feature.getFeatureLoc(locgroup, rank), strand, fmin, fmax);
            return ourClass.cast(this);
        }
        public T loc(String sourceFeatureUniqueName, int locgroup, int rank, int strand, int fmin, int fmax) {
            FeatureLoc featureLoc = feature.getFeatureLoc(locgroup, rank);
            assertLoc(featureLoc, strand, fmin, fmax);
            assertNotNull(featureLoc.getSourceFeature());
            assertEquals(sourceFeatureUniqueName, featureLoc.getSourceFeature().getUniqueName());
            return ourClass.cast(this);
        }
        public T noLoc(int locgroup, int rank) {
            assertNull(feature.getFeatureLoc(locgroup, rank));
            return ourClass.cast(this);
        }
        public T source(String sourceUniqueName) {
            assertEquals(sourceUniqueName, feature.getRankZeroFeatureLoc().getSourceFeature().getUniqueName());
            return ourClass.cast(this);
        }
        public T phaseIsNull() {
            for (FeatureLoc featureLoc: feature.getFeatureLocs()) {
                assertNull(featureLoc.getPhase());
            }
            return ourClass.cast(this);
        }
        public T name(String name) {
            assertEquals(name, feature.getName());
            return ourClass.cast(this);
        }
        public T property(String cv, String term, String value) {
            boolean found = false;
            for (FeatureProp featureProp: feature.getFeatureProps()) {
                CvTerm propType = featureProp.getType();
                String propValue = featureProp.getValue();

                logger.trace(String.format("Found property (%s=%s) on '%s'", propType, propValue, feature.getUniqueName()));
                if (propType.getCv().getName().equals(cv) && propType.getName().equals(term)) {
                    if (found) {
                        fail(String.format("Property '%s' found more than once on feature '%s'", propType, feature.getUniqueName()));
                    }
                    assertEquals(value, featureProp.getValue());
                    found = true;
                }
            }
            assertTrue (String.format("Property '%s:%s' not found on feature '%s'", cv, term, feature.getUniqueName()), found);
            return null; // Not reached
        }
    }

    class GeneTester extends AbstractTester<GeneTester> {
        private AbstractGene gene;
        private GeneTester(String uniqueName) {
            super(GeneTester.class, (Feature) session.createCriteria(AbstractGene.class)
                .add(Restrictions.eq("uniqueName", uniqueName))
                .uniqueResult());

            this.gene = (AbstractGene) feature;
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

        public GeneTester transcripts(String... uniqueNames) {
            assertFeatureUniqueNamesEqual(gene.getTranscripts(), uniqueNames);
            return this;
        }

        public TranscriptTester transcript(String uniqueName) {
            StringBuilder transcriptNames = new StringBuilder("{");
            boolean first = true;
            for (Transcript transcript: gene.getTranscripts()) {
                if (transcript.getUniqueName().equals(uniqueName)) {
                    return new TranscriptTester(transcript);
                }
                if (!first) {
                    transcriptNames.append(", ");
                }
                transcriptNames.append(transcript.getUniqueName());
                first = false;
            }
            transcriptNames.append("}");
            fail(String.format("Transcript '%s' not found on gene '%s'; transcripts are %s",
                uniqueName, gene.getUniqueName(), transcriptNames));
            return null; // Not reached; silly compiler
        }
    }

    class TranscriptTester extends AbstractTester<TranscriptTester> {
        private Transcript transcript;
        private TranscriptTester(Transcript transcript) {
            super(TranscriptTester.class, transcript);
            this.transcript = (Transcript) feature;
        }

        public TranscriptTester synonyms(String synonymType, String... expectedUniqueNames) {
            assertSynonymNamesEqual(transcript.getSynonyms(synonymType), expectedUniqueNames);
            return this;
        }

        public TranscriptTester hasPolypeptide(String uniqueName) {
            assertTrue (transcript instanceof ProductiveTranscript);
            ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;

            Polypeptide polypeptide = productiveTranscript.getProtein();
            assertEquals(uniqueName, polypeptide.getUniqueName());

            SortedSet<AbstractExon> exons = productiveTranscript.getComponents(AbstractExon.class);
            assertLoc(polypeptide.getRankZeroFeatureLoc(), transcript.getStrand(),
                exons.first().getFmin(), exons.last().getFmax());

            return this;
        }

        public PolypeptideTester polypeptide(String uniqueName) {
            assertTrue (transcript instanceof ProductiveTranscript);
            ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;

            Polypeptide polypeptide = productiveTranscript.getProtein();
            assertEquals(uniqueName, polypeptide.getUniqueName());

            return new PolypeptideTester(polypeptide);
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

        public ExonTester exon(String uniqueName) {
            for (AbstractExon exon: transcript.getExons()) {
                if (uniqueName.equals(exon.getUniqueName())) {
                    return new ExonTester(exon);
                }
            }
            fail(String.format("Exon '%s' not found on transcript '%s'", uniqueName, transcript.getUniqueName()));
            return null; // Not reached, but makes compiler happy
        }
}

    class PolypeptideTester extends AbstractTester<PolypeptideTester> {
        private Polypeptide polypeptide;
        private PolypeptideTester(Polypeptide polypeptide) {
            super(PolypeptideTester.class, polypeptide);
            this.polypeptide = (Polypeptide) feature;
        }

        public SimilarityTester similarity(String db, String accession) {
            for (ProteinMatch proteinMatch: polypeptide.getProteinMatches()) {
                DbXRef dbXRef = proteinMatch.getQuery().getDbXRef();
                if (dbXRef.getDb().getName().equals(db) && dbXRef.getAccession().equals(accession)) {
                    return new SimilarityTester(proteinMatch);
                }
            }
            fail(String.format("Could not find similarity '%s:%s' on polypeptide '%s'",
                db, accession, polypeptide.getUniqueName()));
            return null; // Not reached
        }
    }

    class SimilarityTester extends AbstractTester<SimilarityTester> {
        private ProteinMatch proteinMatch;
        private SimilarityTester(ProteinMatch proteinMatch) {
            super(SimilarityTester.class, proteinMatch);
            this.proteinMatch = proteinMatch;
        }
    }

    class ExonTester extends AbstractTester<ExonTester> {
        private AbstractExon exon;
        private ExonTester(AbstractExon exon) {
            super(ExonTester.class, exon);
            this.exon = (AbstractExon) feature;
        }

        public ExonTester fmin(int fmin) {
            assertEquals(fmin, exon.getFmin());
            return this;
        }
        public ExonTester fmax(int fmax) {
            assertEquals(fmax, exon.getFmax());
            return this;
        }
        public ExonTester strand(int strand) {
            assertEquals(strand, exon.getStrand());
            return this;
        }
        public ExonTester phase(Integer phase) {
            for (FeatureLoc featureLoc: exon.getFeatureLocs()) {
                assertEquals(phase, featureLoc.getPhase());
            }
            return this;
        }
    }

    class TLFTester extends AbstractTester<TLFTester> {
        private TopLevelFeature tlf;

        @SuppressWarnings("unchecked")
        private TLFTester(Class<? extends TopLevelFeature> tlfClass, String uniqueName) {
            super(TLFTester.class, (Feature) session.createCriteria(tlfClass)
                        .add(Restrictions.eq("uniqueName", uniqueName))
                        .uniqueResult());
            tlf = tlfClass.cast(feature);
            assertNotNull(tlf);
        }

        public TLFTester seqLen(int len) {
            assertEquals(len, tlf.getSeqLen());
            return this;
        }

        public TLFTester residues(String residues) {
            assertEquals(residues, tlf.getResidues());
            return this;
        }
}
}
