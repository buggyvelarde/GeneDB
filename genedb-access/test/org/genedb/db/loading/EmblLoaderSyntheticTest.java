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
import java.sql.SQLException;
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
    public static void cleanUp() throws SQLException {
        helper.cleanUp();
    }

    @Test
    public void featureNames() {
        tester.uniqueNames(AbstractGene.class, "s1", "s2")
              .uniqueNames(Pseudogene.class, "s1")
              .uniqueNames(Gene.class, "s2");
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
            .polypeptide("s1:pseudogenic_transcript:pep")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
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
     * wholly on <code>con1a</code>, and so it should have a location on that contig.
     * The second exon <code>s2_2:exon:2</code> lies on contig <code>con2c</code>, and
     * so should have a location there instead.
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
            //.loc("con1a",  1, 1, +1, 89, 100)
            .noLoc(0,1).noLoc(1,0);

        s2_2.exon("s2_2:exon:2")
            .loc("super1", 0, 0, +1, 109, 120)
            .loc("con2c",  1, 1, +1, 0, 10)
            .noLoc(0,1).noLoc(1,0);
    }
}
