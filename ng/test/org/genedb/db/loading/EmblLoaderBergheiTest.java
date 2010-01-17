package org.genedb.db.loading;

import org.gmod.schema.feature.Gene;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class EmblLoaderBergheiTest {

    private static EmblLoaderTestHelper helper;
    private static FeatureTester tester;

    @BeforeClass
    public static void setupAndLoad() throws IOException, ParsingException {
        helper = EmblLoaderTestHelper.create("Pberghei", "test/data/PB_PH0001.embl");
        tester = helper.tester();
    }

    @AfterClass
    public static void cleanUp() {
        helper.cleanUp();
    }

    @Test
    public void threeGenes() {
        tester.uniqueNames(Gene.class, "PB400001.00.0");
    }

    @Test
    public void testGene() {

        tester.geneTester("PB400001.00.0")
            .loc(1, 240,312)
            .source("PB_PH0001")
            .transcripts("PB400001.00.0:mRNA")
            .transcript("PB400001.00.0:mRNA")
            .hasPolypeptide("PB400001.00.0:pep")
            .singleExon(1, 240, 312);
    }
}
