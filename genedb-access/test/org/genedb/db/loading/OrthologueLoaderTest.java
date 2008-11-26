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


public class OrthologueLoaderTest {

    private static final Logger logger = Logger.getLogger(OrthologueLoaderTest.class);

    private static ApplicationContext applicationContext;
    private static OrthologuesLoader loader;
    private static OrthologueTester tester;

    private static final String program = "fasta";
    private static final String programVersion = "3.4t26";
    private static final String algorithm = "Reciprocal best match";

    @BeforeClass
    public static void setup() throws IOException, ParsingException {
        applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "Test.xml"});
        loader = (OrthologuesLoader) applicationContext.getBean("orthologuesLoader", OrthologuesLoader.class);

        loadEmblFile("test/data/MRSA252_subset.embl", "Saureus_MRSA252");
        loadEmblFile("test/data/MSSA476_subset.embl", "Saureus_MSSA476");
        loadEmblFile("test/data/EMRSA15_subset.embl", "Saureus_EMRSA15");

        File file = new File("test/data/orth1.ortho");
        Reader reader = new FileReader(file);
        try {
            OrthologueFile orthologueFile = new OrthologueFile(file, reader);

            loader.setAnalysisProperties(program, programVersion, algorithm);
            loader.setGeneNames(true);
            loader.load(orthologueFile);
        } finally {
            reader.close();
        }

        tester = (OrthologueTester) applicationContext.getBean("orthologueTester", OrthologueTester.class);
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

        EmblLoader emblLoader = (EmblLoader) applicationContext.getBean("emblLoader", EmblLoader.class);
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

    private void testOrthologueGroup(double identity, String... polypeptideUniqueNames) {
        tester.orthologueGroup(program, programVersion, algorithm, identity, polypeptideUniqueNames);
    }

    @Transactional @Test
    public void testOrthologueGroups() {
        testOrthologueGroup(100.0, "SAEMRSA1513290:pep", "SAR1478:pep");
        testOrthologueGroup(92.9, "SAEMRSA1516500:pep", "SAR1820:pep");
        testOrthologueGroup(100.0, "SAEMRSA1519750:pep", "SAR2153:pep");
        testOrthologueGroup(95.7, "SAEMRSA1519820:pep", "SAR2160:pep");
        testOrthologueGroup(99.3, "SAEMRSA1521630:pep", "SAR2349:pep");
        testOrthologueGroup(99.2, "SAEMRSA1502320:pep", "SAR0270:pep");
        testOrthologueGroup(96.6, "SAEMRSA1523410:pep", "SAR2531:pep");
        testOrthologueGroup(100.0, "SAEMRSA1525060:pep", "SAR2680:pep");
        testOrthologueGroup(96.5, "SAEMRSA1503370:pep", "SAR0403:pep");
        testOrthologueGroup(100.0, "SAEMRSA1504360:pep", "SAR0511:pep");
        testOrthologueGroup(100.0, "SAEMRSA1507570:pep", "SAR0889:pep");
        testOrthologueGroup(100.0, "SAEMRSA1511870:pep", "SAS1280:pep");
        testOrthologueGroup(99.5, "SAEMRSA1517490:pep", "SAS1765:pep");
        testOrthologueGroup(99.6, "SAEMRSA1518070:pep", "SAS1823:pep");
        testOrthologueGroup(100.0, "SAEMRSA1520150:pep", "SAS2010:pep");
        testOrthologueGroup(98.5, "SAEMRSA1523990:pep", "SAS2388:pep");
        testOrthologueGroup(98.6, "SAEMRSA1524970:pep", "SAS2480:pep");
        testOrthologueGroup(99.2, "SAEMRSA1503330:pep", "SAS0357:pep");
        testOrthologueGroup(98.9, "SAEMRSA1504320:pep", "SAS0463:pep");
        testOrthologueGroup(100.0, "SAEMRSA1504870:pep", "SAS0518:pep");
        testOrthologueGroup(98.6, "SAEMRSA1505550:pep", "SAS0595:pep");
        testOrthologueGroup(98.7, "SAEMRSA1500750:pep", "SAS0083:pep");
        testOrthologueGroup(93.0, "SAEMRSA1508090:pep", "SAS0850:pep");
        testOrthologueGroup(99.4, "SAR1647:pep", "SAS1508:pep");
        testOrthologueGroup(100.0, "SAR1712:pseudogenic_transcript:pep", "SAS1568:pep");
        testOrthologueGroup(99.4, "SAR2389:pep", "SAS2196:pep");
        testOrthologueGroup(100.0, "SAR2601:pep", "SAS2406:pep");
        testOrthologueGroup(97.1, "SAR1812:pep", "SAS1660:pep");
        testOrthologueGroup(99.0, "SAR0736:pep", "SAS0648:pep");
        testOrthologueGroup(99.2, "SAR0156:pep", "SAS0129:pep");
        testOrthologueGroup(97.9, "SAR1639:pep", "SAS1500:pep");
        testOrthologueGroup(100.0, "SAR0015:pep", "SAS0015:pep");
        testOrthologueGroup(100.0, "SAR1663:pep", "SAS1523:pep");
        testOrthologueGroup(100.0, "SAR1939:pep", "SAS1769:pep");
    }
}
