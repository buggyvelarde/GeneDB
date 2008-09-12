package org.genedb.db.loading.alternative;

import org.gmod.schema.feature.Gene;

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Transactional(rollbackFor=Throwable.class)
public class EmblLoaderBergheiTest {

    private TestLoader testLoader;

    @Before
    public void setupAndLoad() throws IOException, ParsingException {
        testLoader = new TestLoader("Pberghei", "test/data/PB_PH0001.embl");
    }

    @Test
    public void threeGenes() {
        testLoader.uniqueNames(Gene.class, "PB400001.00.0");
    }

    @Test
    public void testGene() {

        testLoader.geneTester("PB400001.00.0")
            .loc(1, 240,312)
            .source("PB_PH0001")
            .transcripts("PB400001.00.0:mRNA")
            .transcript("PB400001.00.0:mRNA")
            .synonyms("temporary_systematic_id", "PB400001.00.0")
            .polypeptide("PB400001.00.0:pep")
            .singleExon(1, 240, 312);
    }
}
