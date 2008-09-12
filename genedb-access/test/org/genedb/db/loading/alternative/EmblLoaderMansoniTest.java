package org.genedb.db.loading.alternative;

import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.RepeatRegion;

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Transactional(rollbackFor=Throwable.class)
public class EmblLoaderMansoniTest {

    private TestLoader testLoader;

    @Before
    public void setupAndLoad() throws IOException, ParsingException {
        testLoader = new TestLoader("test/data/Smp_scaff000604.embl");
    }

    @Test
    public void threeGenes() {
        testLoader.uniqueNames(Gene.class, "Smp_097230", "Smp_097240", "Smp_175570");
    }

    @Test
    public void elevenRepeatRegions() {
        testLoader.names(RepeatRegion.class, "29646.repeat000001", "29646.repeat000002",
            "29646.repeat000003", "29646.repeat000004", "29646.repeat000005", "29646.repeat000041",
            "29646.repeat000042", "29646.repeat000043", "29646.repeat000048", "29646.repeat000049",
            "29646.repeat000050");
    }

    @Test
    public void testSmp_097230() {

        TestLoader.GeneTester geneTester = testLoader.geneTester("Smp_097230")
            .loc(1, 18450, 18693)
            .source("Smp_scaff000604")
            .transcripts("Smp_097230:mRNA");

        geneTester.transcript("Smp_097230:mRNA")
            .synonyms("synonym", "29646.t000001", "29646.m000185")
            .synonyms("temporary_systematic_id", "Smp_097230")
            .polypeptide("Smp_097230:pep")
            .singleExon(1, 18450, 18693);
    }

    @Test
    public void test_Smp097240() {
        testLoader.geneTester("Smp_097240")
        .loc(-1, 18494, 22354)
        .source("Smp_scaff000604")
        .transcripts("Smp_097240.1:mRNA", "Smp_097240.2:mRNA", "Smp_097240.4:mRNA");
    }

    @Test
    public void test_Smp097240_1() {
        testLoader.geneTester("Smp_097240").transcript("Smp_097240.1:mRNA")
            .components("Smp_097240.1:exon:1", "Smp_097240.1:exon:2", "Smp_097240.1:exon:3", "Smp_097240.1:exon:4",
                        "Smp_097240.1:exon:5", "Smp_097240.1:exon:6", "Smp_097240.1:exon:7", "Smp_097240.1:exon:8",
                        "Smp_097240.1:3utr",   "Smp_097240.1:5utr:1", "Smp_097240.1:5utr:2")
            .polypeptide("Smp_097240.1:pep");
    }
}
