package org.genedb.db.loading;

import static org.junit.Assert.*;

import org.genedb.db.loading.FeatureTester.GeneTester;
import org.genedb.db.loading.FeatureTester.GenericTester;
import org.genedb.db.loading.FeatureTester.PolypeptideTester;
import org.genedb.db.loading.FeatureTester.TranscriptTester;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.RepeatRegion;
import org.gmod.schema.feature.RepeatUnit;
import org.gmod.schema.mapped.CvTerm;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Test various corner cases using totally synthetic data.
 * (Some of the individual qualifiers are lifted from real
 * data sets, but the genome as a whole is fictitious.)
 *
 * @author rh11
 *
 */
public class EmblLoaderSyntheticTest {
    //private static final Logger logger = Logger.getLogger(EmblLoaderSyntheticTest.class);

    protected static EmblLoaderTestHelper helper;
    protected static FeatureTester tester;

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
    public static void cleanUp() {
        helper.cleanUp();
    }

    @Test
    public void featureNames() {
        tester.uniqueNames(AbstractGene.class, "s1", "s2", "s3", "s4", "s5", "Smp_124050",
                  "super1_tRNA1", "Tc00.1047053511907.50")
              .uniqueNames(Pseudogene.class, "s1")
              .uniqueNames(Gene.class, "s2", "s3", "s4", "s5", "Smp_124050", "super1_tRNA1",
                  "Tc00.1047053511907.50")
              .uniqueNames(Contig.class, "con1a", "con2c", "con3g", "con4t", "con5a",
                  "con6c_reversed", "con7g", "con8t");
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
    public void rileyClass() {
        tester.geneTester("Tc00.1047053511907.50")
        .transcript("Tc00.1047053511907.50.1")
        .polypeptide("Tc00.1047053511907.50.1:pep")
        .cvtermsCheckingDb("RILEY", "RILEY", "4.1.6");
    }

    @Test
    public void s1GeneLocs() {
        tester.geneTester("s1")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
    }

    @Test
    public void s1TranscriptLocs() {
        tester.geneTester("s1").transcript("s1.1")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
    }

    @Test
    public void s1PolypeptideLocs() {
        tester.geneTester("s1")
            .transcript("s1.1")
            .hasPolypeptide("s1.1:pep")
            .loc(0, 0, -1, 3, 87)
            .loc(1, 1, -1, 3, 87);
    }

    @Test
    public void s1EC_number() {
        tester.geneTester("s1")
            .transcript("s1.1")
            .polypeptide("s1.1:pep")
            .property("genedb_misc", "EC_number", "1.3.99.1");
    }

    @Test
    public void s2Names() {
        tester.geneTester("s1").name(null);
        tester.geneTester("s2").name("s2_name");
    }

    @Test
    public void s2Colours() {
        // Also check that the repetition of the qualifier /colour="9" does not cause
        // two properties to be added.
        TranscriptTester s2_2 = tester.geneTester("s2").transcript("s2_2");
        s2_2.polypeptide("s2_2:pep").property("genedb_misc", "colour", "9");
        s2_2.exon("s2_2:exon:1").property("genedb_misc", "colour", "9");
        s2_2.exon("s2_2:exon:2").property("genedb_misc", "colour", "9");
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
     * wholly on <code>con1a</code>, but to avoid confusion the exon should NOT have a
     * location on a contig unless its associated transcript does. So in this case,
     * <code>s2_1:exon:1</code> should have a contig location but <code>s2_2:exon:1</code>
     * should not.
     */
    @Test
    public void s2genelocs() {
        TranscriptTester s2_1 = tester.geneTester("s2").transcript("s2_1");
        TranscriptTester s2_2 = tester.geneTester("s2").transcript("s2_2");

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
            .noLoc(0,1).noLoc(1,0).noLoc(1,1);

        s2_2.exon("s2_2:exon:2")
            .loc("super1", 0, 0, +1, 109, 120)
            .noLoc(0,1).noLoc(1,0).noLoc(1, 1);
    }

    @Test
    public void s2products() {
        PolypeptideTester s2_2 = tester.geneTester("s2").transcript("s2_2").polypeptide("s2_2:pep");

        s2_2.cvtermsCheckingDb("genedb_products", "PRODUCT", "product 1", "product 2", "product 3");
    }

    @Test
    public void s3similarities() {
        PolypeptideTester s3 = tester.geneTester("s3").transcript("s3.1").polypeptide("s3.1:pep");

        // /similarity="blastp; SWALL:Q26723 (EMBL:M20871);
        // Trypanosoma brucei brucei; variant-specific antigen;
        // ESAG3; ; id=70%; ; E()=2e-42; score=438; ; ;"

        s3.similarity("UniProt", "Q26723")
            .analysisProgram("blastp")
            .organism("Trypanosoma brucei brucei")
            .product("variant-specific antigen")
            .gene("ESAG3")
            .id(70.0)
            .eValue(2E-42)
            .score(438.0);

        // /similarity="blastp; GB:BAD74067.1; ; ; ; ; id=54.4%; ;
        // E()=e-17; ; ; ;"

        s3.similarity("GB", "BAD74067.1")
            .analysisProgram("blastp")
            .organism(null)
            .product(null)
            .gene(null)
            .id(54.4)
            .eValue(1E-17)
            .score(null);
    }

    @Test
    public void s4Similarities() {
        PolypeptideTester s4 = tester.geneTester("s4").transcript("s4.1").polypeptide("s4.1:pep");

        // /similarity="fasta; SWALL:O21243 (EMBL:AF007261,
        // SWALL:COXZ_RECAM); Reclinomonas americana; ; ; length 182
        // aa; id=44.805%; ungapped id=46.939%; E()=2.5e-25; ; 151 aa
        // overlap; query 1-152 aa; subject 32-180 aa"

        s4.similarity("UniProt", "O21243")
            .analysisProgram("fasta")
            .organism("Reclinomonas americana")
            .product(null)
            .gene(null)
            .id(44.805)
            .eValue(2.5E-25)
            .score(null)
            .overlap(151)
            .loc(0, 0, 0, 0, 152)
            .loc(0, 1, 0, 31, 180)
            .secondaryDbXRefs("EMBL:AF007261", "UniProt:COXZ_RECAM");
    }

    @Test
    public void s4_GO() {
        PolypeptideTester s4 = tester.geneTester("s4").transcript("s4.1").polypeptide("s4.1:pep");

        // TODO test GO term loading
        Collection<CvTerm> process  = s4.getTerms("biological_process", "GO");
        Collection<CvTerm> function = s4.getTerms("molecular_function", "GO");
        Collection<CvTerm> component = s4.getTerms("molecular_function", "GO");
    }

    @Test
    public void s5_dbxrefs() {
        PolypeptideTester s5 = tester.geneTester("s5").transcript("s5.1").polypeptide("s5.1:pep");

        s5.dbXRefs("UniProt:Q95Z09");
    }

    @Test
    public void Smp_124050_similarities() {
        PolypeptideTester Smp_124050_4 = tester.geneTester("Smp_124050")
            .transcript("Smp_124050.4").polypeptide("Smp_124050.4:pep");

        Smp_124050_4.dbXRefs("EMBL:AY953433")
            .similarity("UniProt", "A6WB28.1")
                .analysisProgram("blastall", "v2.2.6")
                .analysisAlgorithm("ComparativeBlastX_uni");
    }

    @Test
    public void Smp_124050_controlled_curation() {
        PolypeptideTester Smp_124050_4 = tester.geneTester("Smp_124050")
        .transcript("Smp_124050.4").polypeptide("Smp_124050.4:pep");

        Smp_124050_4.cvterms("CC_genedb_controlledcuration",
            "expression in 7 week adult");
    }

    @Test
    public void Smp_124050_literature() {
        PolypeptideTester Smp_124050_4 = tester.geneTester("Smp_124050")
            .transcript("Smp_124050.4").polypeptide("Smp_124050.4:pep");

        Smp_124050_4.pubs("PMID:23456", "PMID:34567");
    }

    @Test
    public void Smp_124050_private() {
        PolypeptideTester Smp_124050_4 = tester.geneTester("Smp_124050")
            .transcript("Smp_124050.4").polypeptide("Smp_124050.4:pep");

        Smp_124050_4.property("genedb_misc", "private", "a private note");
    }

    @Test
    public void repeats() {
        tester.uniqueNames(RepeatRegion.class,
            "super1:repeat:0-93", "super1:repeat_unit:3-9", "super1:repeat_unit:9-15");
        tester.uniqueNames(RepeatUnit.class, "super1:repeat_unit:3-9", "super1:repeat_unit:9-15");

        tester.featureTester("super1:repeat:0-93")
            .name(null)
            .loc(0, 0, 93)
            .phaseIsNull()
            .property("feature_property", "comment", "/rpt_family=telomere")
            .property("genedb_misc", "EMBL_qualifier", "/rpt_unit=TTAGGG");

        tester.featureTester("super1:repeat_unit:3-9")
            .name(null)
            .loc(0, 3, 9)
            .phaseIsNull()
            .property("genedb_misc", "colour", "5")
            .properties("feature_property", "comment", "/label=Trpt", "telomeric repeat hexamer TTAGGG");

        tester.featureTester("super1:repeat_unit:9-15")
            .name(null)
            .loc(0, 9, 15)
            .phaseIsNull()
            .property("genedb_misc", "colour", "5")
            .properties("feature_property", "comment", "/label=Trpt", "telomeric repeat hexamer TTAGGG");
    }

    @Test
    public void tRNA() {
        GeneTester tRNA1 = tester.geneTester("super1_tRNA1");
        tRNA1.loc(1, 369, 400)
            .phaseIsNull()
            .name(null)
            .properties("genedb_misc", "colour")
            .properties("feature_property", "comment");

        tRNA1.transcript("super1_tRNA1.1")
            .properties("feature_property", "comment", "/label=tRNA label")
            .properties("genedb_misc", "colour", "12")
            .property("genedb_misc", "EMBL_qualifier", "/invented_qualifier=\"value\"")
            .dbXRefs("RFAM:RF00230");
    }

    @Test
    public void productCaseSensitivity() {
        Collection<CvTerm> s3_products = tester.geneTester("s3")
            .transcript("s3.1").polypeptide("s3.1:pep")
            .getTerms("genedb_products");
        Collection<CvTerm> s4_products = tester.geneTester("s4")
            .transcript("s4.1").polypeptide("s4.1:pep")
            .getTerms("genedb_products");
        assertEquals(s3_products, s4_products);
    }

    @Test
    public void archivedFeature() {
        FeatureTester.GenericTester t = tester.featureTester("super1:archived:nonstandard:1");
        t.loc(+1, 599, 630);
        t.assertObsolete();
        t.propertyMatches("feature_property", "comment",
            "Archived from nonstandard feature with location join\\(600\\.\\.610,620\\.\\.630\\); " +
            "file 'test/data/synthetic.embl', line \\d+");
        t.property("genedb_misc", "EMBL_qualifier", "/madeupqualifier=\"value\"");
    }

    private void gap(int fmin, int fmax, String... notes) {
        GenericTester g = tester.featureTester(String.format("super1:gap:%d-%d", fmin, fmax)).loc(0, fmin, fmax);
        g.properties("feature_property", "comment", notes);
    }

    @Test
    public void gapsBetweenContigs() {
        gap(100, 110); gap(210, 220); gap(320, 330); gap(430, 440);
        gap(640, 650); gap(850, 860); gap(1060, 1110);
    }

    @Test
    public void gapFeature() {
        gap(1180, 1190, "This is a gap that has a note", "And this is a cow with an itchy throat");
    }
}
