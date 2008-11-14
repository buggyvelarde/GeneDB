package org.genedb.db.loading;

import org.genedb.db.loading.FeatureTester.TranscriptTester;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Pseudogene;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Test various corner cases using totally synthetic data.
 *
 * @author rh11
 *
 */
public class EmblLoaderSyntheticTest {
    private static EmblLoaderTestHelper helper;
    private static FeatureTester tester;

    private String repeat(char c, int n) {
        char[] array = new char[n];
        Arrays.fill(array, c);
        return new String(array);
    }

    @BeforeClass
    public static void setupAndLoad() throws IOException, ParsingException {
        helper = EmblLoaderTestHelper.create(
            "Synthetic", "Synthetic", "organism", null,
            "test/data/synthetic.embl");
        tester = helper.tester();
    }

    @AfterClass
    public static void cleanUp() {
        helper.cleanUp();
    }

    @Test
    public void featureNames() {
        tester.uniqueNames(AbstractGene.class, "s1", "s2", "s3")
              .uniqueNames(Pseudogene.class, "s1")
              .uniqueNames(Gene.class, "s2", "s3");
    }

    @Test
    public void contigSequence() {
        tester.tlfTester(Contig.class, "con1a")
            .residues(repeat('a', 100));

        tester.tlfTester(Contig.class, "con2c")
            .residues(repeat('c', 100));

        tester.tlfTester(Contig.class, "con3g")
            .residues(repeat('g', 100));

        tester.tlfTester(Contig.class, "con4t")
            .residues(repeat('t', 100));
}

    @Test
    public void s1GeneLocs() {
        tester.geneTester("s1")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
    }

    @Test
    public void s1TranscriptLocs() {
        tester.geneTester("s1").transcript("s1:pseudogenic_transcript")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
    }

    @Test
    public void s1PolypeptideLocs() {
        tester.geneTester("s1")
            .transcript("s1:pseudogenic_transcript")
            .hasPolypeptide("s1:pseudogenic_transcript:pep")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
    }

    @Test
    public void s2Names() {
        tester.geneTester("s1").name(null);
        tester.geneTester("s2").name("s2_name");
    }

    @Test
    public void s2Colours() {
        // Also check that the repetition of the qualifier /colour="9" does not cause
        // two properties to be added.
        TranscriptTester s2_2 = tester.geneTester("s2").transcript("s2_2:mRNA");
        s2_2.polypeptide("s2_2:pep").property("genedb_misc", "colour", "9");
        s2_2.exon("s2_2:exon:1").property("genedb_misc", "colour", "9");
        s2_2.exon("s2_2:exon:2").property("genedb_misc", "colour", "9");
    }

    /**
     * GENEDB-207: the gene <code>s2</code> has two transcripts. The first of them is
     * contained wholly within the contig <code>con1a</code>, whereas the
     * second has an additional exon on a different contig <code>con2c</code>.
     * <p>
     * What we expect, therefore, is that the gene should not have a contig location,
     * because it doesn't lie on a single contig. The first transcript <code>s1_1</code>,
     * on the other hand, does lie wholly on <code>con1a</code>, so it should have a
     * contig location.
     * <p>
     * The first exon (<code>s2_1:exon:1</code> and <code>s2_2:exon:1</code>) also lies
     * wholly on <code>con1a</code>, but to avoid confusion the exon should NOT have a
     * location on a contig unless its associated transcript does. So in this case,
     * <code>s2_1:exon:1</code> should have a contig location but <code>s2_2:exon:1</code>
     * should not.
     */
    @Test
    public void s2genelocs() {
        TranscriptTester s2_1 = tester.geneTester("s2").transcript("s2_1:mRNA");
        TranscriptTester s2_2 = tester.geneTester("s2").transcript("s2_2:mRNA");

        s2_1.loc("super1", 0, 0, +1, 89, 100)
            .loc("con1a", 1, 1, +1, 89, 100)
            .noLoc(0,1).noLoc(1,0);

        s2_1.exon("s2_1:exon:1")
            .loc("super1", 0, 0, +1, 89, 100)
            .loc("con1a", 1, 1, +1, 89, 100)
            .noLoc(0,1).noLoc(1,0);

        s2_2.loc(0, 0, +1, 89, 120)
            .noLoc(0,1).noLoc(1,0).noLoc(1,1);

        s2_2.exon("s2_2:exon:1")
            .loc("super1", 0, 0, +1, 89, 100)
            .noLoc(0,1).noLoc(1,0).noLoc(1,1);

        s2_2.exon("s2_2:exon:2")
            .loc("super1", 0, 0, +1, 109, 120)
            .noLoc(0,1).noLoc(1,0).noLoc(1, 1);
    }
}
