package org.genedb.db.loading;

import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.RepeatRegion;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class EmblLoaderMansoniTest {

    private static EmblLoaderTestHelper helper;
    private static FeatureTester tester;

    @BeforeClass
    public static void setupAndLoad() throws IOException, ParsingException {
        helper = EmblLoaderTestHelper.create("Smansoni", "test/data/Smp_scaff000604.embl");
        tester = helper.tester();
    }

    @AfterClass
    public static void cleanUp() {
        helper.cleanUp();
    }

    @Test
    public void threeGenes() {
        tester.uniqueNames(Gene.class, "Smp_097230", "Smp_097240", "Smp_175570");
    }

    @Test
    public void elevenRepeatRegions() {
        tester.names(RepeatRegion.class, "29646.repeat000001", "29646.repeat000002",
            "29646.repeat000003", "29646.repeat000004", "29646.repeat000005", "29646.repeat000041",
            "29646.repeat000042", "29646.repeat000043", "29646.repeat000048", "29646.repeat000049",
            "29646.repeat000050");
    }

    @Test
    public void testSmp_097230() {

        FeatureTester.GeneTester geneTester = tester.geneTester("Smp_097230")
            .loc(1, 18450, 18693)
            .source("Smp_scaff000604")
            .transcripts("Smp_097230:mRNA");

        geneTester.transcript("Smp_097230:mRNA")
            .synonyms("synonym", "29646.t000001", "29646.m000185")

            // Check that temporary_systematic_id and systematic_id synonyms
            // are NOT being added
            .synonyms("temporary_systematic_id").synonyms("systematic_id")
            .hasPolypeptide("Smp_097230:pep")
            .singleExon(1, 18450, 18693);
    }

    @Test
    public void test_Smp097240() {
        tester.geneTester("Smp_097240")
        .loc(-1, 18494, 22354)
        .source("Smp_scaff000604")
        .transcripts("Smp_097240.1", "Smp_097240.2", "Smp_097240.4");
    }

    @Test
    public void test_Smp097240_1() {
        tester.geneTester("Smp_097240").transcript("Smp_097240.1")
            .components("Smp_097240.1:exon:1", "Smp_097240.1:exon:2", "Smp_097240.1:exon:3", "Smp_097240.1:exon:4",
                        "Smp_097240.1:exon:5", "Smp_097240.1:exon:6", "Smp_097240.1:exon:7", "Smp_097240.1:exon:8",
                        "Smp_097240.1:3utr",   "Smp_097240.1:5utr:1", "Smp_097240.1:5utr:2")
            .hasPolypeptide("Smp_097240.1:pep");
    }

    /**
     * Check that the phase is only set for exons, not for other features.
     */
    @Test
    public void phase() {
        // CDS Smp_097250 has /codon_start=2
        // (It doesn't really: that has been added for testing so we can
        // test that it works with an explicit /codon_start qualifier/)
        FeatureTester.TranscriptTester tt = tester.geneTester("Smp_097250").phaseIsNull()
            .transcript("Smp_097250:pseudogenic_transcript").phaseIsNull();
        tt.exon("Smp_097250:exon:1").phase(1);
        tt.exon("Smp_097250:exon:2").phase(1);

        tt = tester.geneTester("Smp_175570").phaseIsNull()
            .transcript("Smp_175570:mRNA").phaseIsNull();
        tt.exon("Smp_175570:exon:1").phase(null);
        tt.exon("Smp_175570:exon:2").phase(null);
        tt.exon("Smp_175570:exon:3").phase(null);
        tt.exon("Smp_175570:exon:4").phase(null);
        tt.exon("Smp_175570:exon:5").phase(null);
    }
}
