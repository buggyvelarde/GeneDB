package org.genedb.db.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.genedb.db.loading.FeatureTable.Feature;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class EmblFileTest {
    private static final Logger logger = Logger.getLogger(EmblFileTest.class);
    private EmblFile emblFile;

    @Before
    public void loadEmblFile() throws IOException, ParsingException {
        logger.debug("Current working directory is " + new File(".").getCanonicalPath());
        File file = new File("test/data/smp_scaff000604.embl");
        emblFile = new EmblFile(file, new FileReader(file));
    }

    @Test
    public void basics() {
        assertEquals("Smp_scaff000604", emblFile.getAccession());
        assertEquals(1, emblFile.getSequenceVersion());
        assertEquals("linear", emblFile.getTopology());
        assertEquals("genomic DNA", emblFile.getMoleculeType());
        assertEquals("ANN", emblFile.getDataClass());
        assertEquals("PRO", emblFile.getTaxonomicDivision());
        assertEquals(206621, emblFile.getSequenceLength());
        assertEquals(206621, emblFile.getSequence().length());
    }

    @Test
    public void pseudo() throws ParsingException {
        FeatureTable featureTable = emblFile.getFeatureTable();
        Iterable<FeatureTable.Feature> features = featureTable.getFeatures();

        int foundFeatures = 0;
        for (Feature feature: features) {
            if (!feature.type.equals("CDS")) {
                continue;
            }

            String temporarySystematicId = feature.getQualifierValue("temporary_systematic_id");
            if ("Smp_097250".equals(temporarySystematicId)) {
                foundFeatures++;
                System.out.println(feature);
                System.out.println(feature.getQualifierValues("pseudo"));
                System.out.println(feature.getQualifierValues("pseudo").isEmpty());
                assertTrue(feature.hasQualifier("pseudo"));
            }
            else if ("Smp_175570".equals(temporarySystematicId)) {
                foundFeatures++;
                assertEquals("CDS at complement(join(31435..31550,31594..31709,31743..31811," +
                        "31849..31930,31973..32027)): " +
                        "/method=\"new or changed gene model\"; " +
                        "/note=\"predicted by EVM2\"; " +
                        "/product=\"conserved hypothetical protein\"; " +
                        "/psu_db_xref=\"SMA1:Sm00.scaff00658.0040;\"; " +
                        "/psu_db_xref=\"GeneDB_Smansoni:Sm00874; Blastn score=852 evalue=0.0, Blastx score=299 evalue=1e-82\"; " +
                        "/similarity=\"blastp; GB:AAH73413.1; ; ; ; ; id=55.3%; ; E()=3.8e-39; ; ; ;\"; " +
                        "/similarity=\"blastp; RF:NP_001004925.1; ; ; ; ; id=56.1%; ; E()=4.8e-39; ; ; ;\"; " +
                        "/similarity=\"blastp; RF:NP_001025088.1; ; ; ; ; id=56.0%; ; E()=6.2e-39; ; ; ;\"; " +
                        "/similarity=\"blastp; SP:Q91VH6; ; ; ; ; id=56.0%; ; E()=6.2e-39; ; ; ;\"; " +
                        "/similarity=\"blastp; SP:Q9Y316; ; ; ; ; id=56.0%; ; E()=6.2e-39; ; ; ;\"; " +
                        "/synonym=\"29646.t000004\"; " +
                        "/synonym=\"29646.m000188\"; " +
                        "/temporary_systematic_id=\"Smp_175570\"", feature.toString());
            }
        }
        assertEquals(2, foundFeatures);
    }
}
