package org.genedb.db.loading;

import org.gmod.schema.feature.Gene;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Transactional(rollbackFor=Throwable.class)
public class EmblLoaderBergheiTest {

    private static FeatureTester tester;

    @BeforeClass
    public static void setupAndLoad() throws IOException, ParsingException {
        tester = new EmblLoaderTestHelper("Pberghei", "test/data/PB_PH0001.embl").tester();
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
            .synonyms("temporary_systematic_id", "PB400001.00.0")
            .polypeptide("PB400001.00.0:pep")
            .singleExon(1, 240, 312);
    }
}
