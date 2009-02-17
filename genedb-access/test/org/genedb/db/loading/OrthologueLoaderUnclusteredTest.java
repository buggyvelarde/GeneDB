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
        tester.orthologue("SAEMRSA1513290:pep", "SAR1478:pep");
        tester.orthologue("SAEMRSA1516500:pep", "SAR1820:pep");
        tester.orthologue("SAEMRSA1519750:pep", "SAR2153:pep");
        tester.orthologue("SAEMRSA1519820:pep", "SAR2160:pep");
        tester.orthologue("SAEMRSA1521630:pep", "SAR2349:pep");
        tester.orthologue("SAEMRSA1502320:pep", "SAR0270:pep");
        tester.orthologue("SAEMRSA1523410:pep", "SAR2531:pep");
        tester.orthologue("SAEMRSA1525060:pep", "SAR2680:pep");
        tester.orthologue("SAEMRSA1503370:pep", "SAR0403:pep");
        tester.orthologue("SAEMRSA1504360:pep", "SAR0511:pep");
        tester.orthologue("SAEMRSA1507570:pep", "SAR0889:pep");
        tester.orthologue("SAEMRSA1511870:pep", "SAS1280:pep");
        tester.orthologue("SAEMRSA1517490:pep", "SAS1765:pep");
        tester.orthologue("SAEMRSA1518070:pep", "SAS1823:pep");
        tester.orthologue("SAEMRSA1520150:pep", "SAS2010:pep");
        tester.orthologue("SAEMRSA1523990:pep", "SAS2388:pep");
        tester.orthologue("SAEMRSA1524970:pep", "SAS2480:pep");
        tester.orthologue("SAEMRSA1503330:pep", "SAS0357:pep");
        tester.orthologue("SAEMRSA1504320:pep", "SAS0463:pep");
        tester.orthologue("SAEMRSA1504870:pep", "SAS0518:pep");
        tester.orthologue("SAEMRSA1505550:pep", "SAS0595:pep");
        tester.orthologue("SAEMRSA1500750:pep", "SAS0083:pep");
        tester.orthologue("SAEMRSA1508090:pep", "SAS0850:pep");
        tester.orthologue("SAR1647:pep", "SAS1508:pep");
        tester.orthologue("SAR1712:pseudogenic_transcript:pep", "SAS1568:pep");
        tester.orthologue("SAR2389:pep", "SAS2196:pep");
        tester.orthologue("SAR2601:pep", "SAS2406:pep");
        tester.orthologue("SAR1812:pep", "SAS1660:pep");
        tester.orthologue("SAR0736:pep", "SAS0648:pep");
        tester.orthologue("SAR0156:pep", "SAS0129:pep");
        tester.orthologue("SAR1639:pep", "SAS1500:pep");
        tester.orthologue("SAR0015:pep", "SAS0015:pep");
        tester.orthologue("SAR1663:pep", "SAS1523:pep");
        tester.orthologue("SAR1939:pep", "SAS1769:pep");
    }

    @Transactional @Test
    public void testPepNameOrthologueGroups() {
        tester.orthologue("SAEMRSA1511480:pep", "SAR1311:pep");
        tester.orthologue("SAEMRSA1512940:pep", "SAR1444:pep");
        tester.orthologue("SAEMRSA1513440:pep", "SAR1493:pep");
        tester.orthologue("SAEMRSA1514830:pep", "SAR1640:pep");
        tester.orthologue("SAEMRSA1514980:pep", "SAR1655:pep");
        tester.orthologue("SAEMRSA1515150:pep", "SAR1673:pep");
        tester.orthologue("SAEMRSA1516810:pep", "SAR0692:pep");
        tester.orthologue("SAEMRSA1516860:pep", "SAR1859:pep");
        tester.orthologue("SAEMRSA1517760:pep", "SAR1959:pep");
        tester.orthologue("SAEMRSA1518420:pep", "SAR2018:pep");
        tester.orthologue("SAEMRSA1520260:pep", "SAR2206:pep");
        tester.orthologue("SAEMRSA1521860:pep", "SAR2373:pep");
        tester.orthologue("SAEMRSA1525490:pep", "SAR2723:pep");
        tester.orthologue("SAEMRSA1508900:pep", "SAR1032:pep");
        tester.orthologue("SAEMRSA1509280:pep", "SAR1072:pep");
        tester.orthologue("SAEMRSA1510800:pep", "SAS1181:pep");
        tester.orthologue("SAEMRSA1514330:pep", "SAS1451:pep");
        tester.orthologue("SAEMRSA1501520:pep", "SAS0162:pep");
        tester.orthologue("SAEMRSA1519310:pep", "SAS1928:pep");
        tester.orthologue("SAEMRSA1520710:pep", "SAS2067:pep");
        tester.orthologue("SAEMRSA1522400:pep", "SAS2234a:pep");
        tester.orthologue("SAEMRSA1522570:pep", "SAS2250:pep");
        tester.orthologue("SAEMRSA1523030:pep", "SAS2295:pep");
        tester.orthologue("SAEMRSA1523490:pep", "SAS2341:pep");
        tester.orthologue("SAEMRSA1525780:pep", "SAS2557:pep");
        tester.orthologue("SAEMRSA1504600:pep", "SAS0491:pep");
        tester.orthologue("SAEMRSA1504710:pep", "SAS0502:pep");
        tester.orthologue("SAEMRSA1505740:pep", "SAS0613:pep");
        tester.orthologue("SAR1035:pep", "SAS0997:pep");
        tester.orthologue("SAR1141:pep", "SAS1101:pep");
        tester.orthologue("SAR1187:pep", "SAS1145:pep");
        tester.orthologue("SAR0014:pep", "SAS0014:pep");
        tester.orthologue("SAR1512:pep", "SAS0939:pep");
        tester.orthologue("SAR1729:pep", "SAS1585:pep");
        tester.orthologue("SAR0628:pep", "SAS0587:pep");
        tester.orthologue("SAR0772:pep", "SAS0684:pep");
        tester.orthologue("SAR0883:pep", "SAS0791:pep");
        tester.orthologue("SAR2691:pep", "SAS2498:pep");
        tester.orthologue("SAR0864:pep", "SAS0773:pep");
        tester.orthologue("SAR0234:pep", "SAS0217:pep");
        tester.orthologue("SAR2454:pep", "SAS2256:pep");
        tester.orthologue("SAR0594:pep", "SAS0547:pep");
    }
}
