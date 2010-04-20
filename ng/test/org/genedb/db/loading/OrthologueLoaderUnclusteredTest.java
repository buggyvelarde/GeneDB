package org.genedb.db.loading;

import org.gmod.schema.feature.Chromosome;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Test the loading of orthologue data in unclustered mode.
 * Implicit cluster mode is engaged when the input file does not
 * contain explicit clusters and no algorithm is specified (indicating
 * that the orthologues are manually curated). Identity percentage
 * values are not stored in the database in this mode.
 * <p>
 * This is not a unit test, in that it relies on the EMBL loader
 * to load the genes before we load their orthologue data.
 *
 * @author rh11
 *
 */
public class OrthologueLoaderUnclusteredTest {

    private static final Logger logger = TestLogger.getLogger(OrthologueLoaderUnclusteredTest.class);

    private static ApplicationContext applicationContext;
    private static OrthologueTester tester;

    @BeforeClass
    public static void setup() throws IOException, ParsingException {
        applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});

        loadEmblFile("test/data/MRSA252_subset.embl", "Saureus_MRSA252");
        loadEmblFile("test/data/MSSA476_subset.embl", "Saureus_MSSA476");
        loadEmblFile("test/data/EMRSA15_subset.embl", "Saureus_EMRSA15");

        loadOrthologues("test/data/Saureus_subset_genenames.ortho", true);
        loadOrthologues("test/data/Saureus_subset_transcriptnames.ortho", false);

        tester = applicationContext.getBean("orthologueTester", OrthologueTester.class);
    }

    private static void loadOrthologues(String filename, boolean geneNames)
        throws IOException, ParsingException {

        OrthologuesLoader loader = applicationContext.getBean("orthologuesLoader", OrthologuesLoader.class);

        File file = new File(filename);
        Reader reader = new FileReader(file);
        try {
            OrthologueFile orthologueFile = new OrthologueFile(file, reader);

            loader.setGeneNames(geneNames);
            loader.load(orthologueFile);
        } finally {
            reader.close();
        }
    }

    @AfterClass
    public static void cleanUp() {
        if (tester == null) {
            // This can happen if there's an error in setup:
            // JUnit still calls us even if setup threw an exception.
            logger.error("Tester is null in cleanUp");
        } else {
            tester.cleanUp();
        }
    }

    private static void loadEmblFile(String filename, String organismCommonName) throws IOException, ParsingException {
        logger.trace(String.format("Loading '%s' into organism '%s'", filename, organismCommonName));

        EmblLoader emblLoader = applicationContext.getBean("emblLoader", EmblLoader.class);
        emblLoader.setOrganismCommonName(organismCommonName);
        emblLoader.setSloppyControlledCuration(true);
        emblLoader.setTopLevelFeatureClass(Chromosome.class);

        File file = new File(filename);
        Reader reader = new FileReader(file);
        try {
            emblLoader.load(new EmblFile(file, reader));
        } finally {
            reader.close();
        }
    }

    @Transactional @Test
    public void testGeneNameOrthologueGroups() {
        tester.orthologue("SAEMRSA1513290.1:pep", "SAR1478.1:pep");
        tester.orthologue("SAEMRSA1516500.1:pep", "SAR1820.1:pep");
        tester.orthologue("SAEMRSA1519750.1:pep", "SAR2153.1:pep");
        tester.orthologue("SAEMRSA1519820.1:pep", "SAR2160.1:pep");
        tester.orthologue("SAEMRSA1521630.1:pep", "SAR2349.1:pep");
        tester.orthologue("SAEMRSA1502320.1:pep", "SAR0270.1:pep");
        tester.orthologue("SAEMRSA1523410.1:pep", "SAR2531.1:pep");
        tester.orthologue("SAEMRSA1525060.1:pep", "SAR2680.1:pep");
        tester.orthologue("SAEMRSA1503370.1:pep", "SAR0403.1:pep");
        tester.orthologue("SAEMRSA1504360.1:pep", "SAR0511.1:pep");
        tester.orthologue("SAEMRSA1507570.1:pep", "SAR0889.1:pep");
        tester.orthologue("SAEMRSA1511870.1:pep", "SAS1280.1:pep");
        tester.orthologue("SAEMRSA1517490.1:pep", "SAS1765.1:pep");
        tester.orthologue("SAEMRSA1518070.1:pep", "SAS1823.1:pep");
        tester.orthologue("SAEMRSA1520150.1:pep", "SAS2010.1:pep");
        tester.orthologue("SAEMRSA1523990.1:pep", "SAS2388.1:pep");
        tester.orthologue("SAEMRSA1524970.1:pep", "SAS2480.1:pep");
        tester.orthologue("SAEMRSA1503330.1:pep", "SAS0357.1:pep");
        tester.orthologue("SAEMRSA1504320.1:pep", "SAS0463.1:pep");
        tester.orthologue("SAEMRSA1504870.1:pep", "SAS0518.1:pep");
        tester.orthologue("SAEMRSA1505550.1:pep", "SAS0595.1:pep");
        tester.orthologue("SAEMRSA1500750.1:pep", "SAS0083.1:pep");
        tester.orthologue("SAEMRSA1508090.1:pep", "SAS0850.1:pep");
        tester.orthologue("SAR1647.1:pep", "SAS1508.1:pep");
        tester.orthologue("SAR1712.1:pep", "SAS1568.1:pep");
        tester.orthologue("SAR2389.1:pep", "SAS2196.1:pep");
        tester.orthologue("SAR2601.1:pep", "SAS2406.1:pep");
        tester.orthologue("SAR1812.1:pep", "SAS1660.1:pep");
        tester.orthologue("SAR0736.1:pep", "SAS0648.1:pep");
        tester.orthologue("SAR0156.1:pep", "SAS0129.1:pep");
        tester.orthologue("SAR1639.1:pep", "SAS1500.1:pep");
        tester.orthologue("SAR0015.1:pep", "SAS0015.1:pep");
        tester.orthologue("SAR1663.1:pep", "SAS1523.1:pep");
        tester.orthologue("SAR1939.1:pep", "SAS1769.1:pep");
    }

    @Transactional @Test
    public void testPepNameOrthologueGroups() {
        tester.orthologue("SAEMRSA1511480.1:pep", "SAR1311.1:pep");
        tester.orthologue("SAEMRSA1512940.1:pep", "SAR1444.1:pep");
        tester.orthologue("SAEMRSA1513440.1:pep", "SAR1493.1:pep");
        tester.orthologue("SAEMRSA1514830.1:pep", "SAR1640.1:pep");
        tester.orthologue("SAEMRSA1514980.1:pep", "SAR1655.1:pep");
        tester.orthologue("SAEMRSA1515150.1:pep", "SAR1673.1:pep");
        tester.orthologue("SAEMRSA1516810.1:pep", "SAR0692.1:pep");
        tester.orthologue("SAEMRSA1516860.1:pep", "SAR1859.1:pep");
        tester.orthologue("SAEMRSA1517760.1:pep", "SAR1959.1:pep");
        tester.orthologue("SAEMRSA1518420.1:pep", "SAR2018.1:pep");
        tester.orthologue("SAEMRSA1520260.1:pep", "SAR2206.1:pep");
        tester.orthologue("SAEMRSA1521860.1:pep", "SAR2373.1:pep");
        tester.orthologue("SAEMRSA1525490.1:pep", "SAR2723.1:pep");
        tester.orthologue("SAEMRSA1508900.1:pep", "SAR1032.1:pep");
        tester.orthologue("SAEMRSA1509280.1:pep", "SAR1072.1:pep");
        tester.orthologue("SAEMRSA1510800.1:pep", "SAS1181.1:pep");
        tester.orthologue("SAEMRSA1514330.1:pep", "SAS1451.1:pep");
        tester.orthologue("SAEMRSA1501520.1:pep", "SAS0162.1:pep");
        tester.orthologue("SAEMRSA1519310.1:pep", "SAS1928.1:pep");
        tester.orthologue("SAEMRSA1520710.1:pep", "SAS2067.1:pep");
        tester.orthologue("SAEMRSA1522400.1:pep", "SAS2234a.1:pep");
        tester.orthologue("SAEMRSA1522570.1:pep", "SAS2250.1:pep");
        tester.orthologue("SAEMRSA1523030.1:pep", "SAS2295.1:pep");
        tester.orthologue("SAEMRSA1523490.1:pep", "SAS2341.1:pep");
        tester.orthologue("SAEMRSA1525780.1:pep", "SAS2557.1:pep");
        tester.orthologue("SAEMRSA1504600.1:pep", "SAS0491.1:pep");
        tester.orthologue("SAEMRSA1504710.1:pep", "SAS0502.1:pep");
        tester.orthologue("SAEMRSA1505740.1:pep", "SAS0613.1:pep");
        tester.orthologue("SAR1035.1:pep", "SAS0997.1:pep");
        tester.orthologue("SAR1141.1:pep", "SAS1101.1:pep");
        tester.orthologue("SAR1187.1:pep", "SAS1145.1:pep");
        tester.orthologue("SAR0014.1:pep", "SAS0014.1:pep");
        tester.orthologue("SAR1512.1:pep", "SAS0939.1:pep");
        tester.orthologue("SAR1729.1:pep", "SAS1585.1:pep");
        tester.orthologue("SAR0628.1:pep", "SAS0587.1:pep");
        tester.orthologue("SAR0772.1:pep", "SAS0684.1:pep");
        tester.orthologue("SAR0883.1:pep", "SAS0791.1:pep");
        tester.orthologue("SAR2691.1:pep", "SAS2498.1:pep");
        tester.orthologue("SAR0864.1:pep", "SAS0773.1:pep");
        tester.orthologue("SAR0234.1:pep", "SAS0217.1:pep");
        tester.orthologue("SAR2454.1:pep", "SAS2256.1:pep");
        tester.orthologue("SAR0594.1:pep", "SAS0547.1:pep");
    }
}
