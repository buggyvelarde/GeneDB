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
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.TranscriptRegion;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Synonym;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;


public class FeatureTester {
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

    public <T extends TopLevelFeature> TLFTester<T> tlfTester(Class<T> tlfClass, String uniqueName) {
        return new TLFTester<T>(tlfClass, uniqueName);
    }

    class GeneTester {
        private AbstractGene gene;
        private GeneTester(String uniqueName) {
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

        public GeneTester phaseIsNull() {
            for (FeatureLoc featureLoc: gene.getFeatureLocs()) {
                assertNull(featureLoc.getPhase());
            }
            return this;
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

        public ExonTester exon(String uniqueName) {
            for (AbstractExon exon: transcript.getExons()) {
                if (uniqueName.equals(exon.getUniqueName())) {
                    return new ExonTester(exon);
                }
            }
            fail(String.format("Exon '%s' not found on transcript '%s'", uniqueName, transcript.getUniqueName()));
            return null; // Not reached, but makes compiler happy
        }

        public TranscriptTester phaseIsNull() {
            for (FeatureLoc featureLoc: transcript.getFeatureLocs()) {
                assertNull(featureLoc.getPhase());
            }
            return this;
        }
}

    class ExonTester {
        private AbstractExon exon;
        private ExonTester(AbstractExon exon) {
            this.exon = exon;
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

    class TLFTester<T extends TopLevelFeature> {
        private T tlf;

        @SuppressWarnings("unchecked")
        private TLFTester(Class<T> tlfClass, String uniqueName) {
            tlf = (T) session.createCriteria(tlfClass)
                        .add(Restrictions.eq("uniqueName", uniqueName))
                        .uniqueResult();
            assertNotNull(tlf);
        }

        public TLFTester<T> seqLen(int len) {
            assertEquals(len, tlf.getSeqLen());
            return this;
        }

        public TLFTester<T> residues(String residues) {
            assertEquals(residues, tlf.getResidues());
            return this;
        }

        public TLFTester<T> loc(int strand, int fmin, int fmax) {
            assertLoc(tlf.getRankZeroFeatureLoc(), strand, fmin, fmax);
            return this;
        }
}
}
