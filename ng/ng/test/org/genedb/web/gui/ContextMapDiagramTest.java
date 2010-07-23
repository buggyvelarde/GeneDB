package org.genedb.web.gui;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.genedb.db.domain.objects.Chromosome;
import org.genedb.db.domain.objects.LocatedFeature;
import org.genedb.db.domain.objects.TranscriptComponent;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.db.domain.test.MockBasicGeneService;
import org.junit.Test;

/**
 * Test that the {@link ContextMapDiagram} class works as intended.
 *
 * @author rh11
 */
@SuppressWarnings("deprecation")
public class ContextMapDiagramTest {

    // Tests on the "isolated" genes, to make sure the basics are working
    private BasicGeneService simple () {
        MockBasicGeneService mockBasicGeneService = new MockBasicGeneService("cat", "chr1", 1);
        mockBasicGeneService.addSimpleGene("isolated", 90, 110, 2);

        mockBasicGeneService.setChromosome("chr2");
        mockBasicGeneService.setStrand(-1);
        mockBasicGeneService.addSimpleGene("isolated2", 90, 110, 2);

        mockBasicGeneService.setChromosome(new Chromosome("chr3", 103, 1000));
        mockBasicGeneService.addSimpleGene("lhs", 20, 30, 3);
        mockBasicGeneService.addSimpleGene("rhs", 950, 990, 3);

        mockBasicGeneService.setChromosome("chr4");
        mockBasicGeneService.addSimpleGene("large", 1000, 100000, 4);

        return mockBasicGeneService;
    }

    /**
     * Test that <code>ContextMapDiagram.forRegion</code> does something plausible-looking
     * in a simple case.
     */
    @Test public void simpleRegion() {
        ContextMapDiagram diagram = ContextMapDiagram.forRegion(simple(), "cat", "chr1", 101, 0, 1000);
        assertNotNull(diagram);
        assertEquals(0, diagram.getStart());
        assertEquals(1000, diagram.getEnd());
        List<LocatedFeature> track = diagram.getTrack(1);
        assertNotNull(track);
        assertEquals(1, track.size());
    }

    /**
     * Test that the diagram is centred about the gene of interest
     */
    @Test public void diagramPosition() {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "isolated", 100);
        assertEquals(diagram.getStart(), 50);
        assertEquals(diagram.getEnd(),   150);
    }

    /**
     * Test that a single gene is correctly placed in track 1,
     * and that (a reasonable sample of) the other tracks are unassigned.
     */
    @Test public void correctTracks() {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "isolated", 100);

        assertEquals(diagram.numberOfPositiveTracks(), 1);
        assertEquals(diagram.numberOfNegativeTracks(), 0);

        assertNotNull(diagram.getTrack(1));
        assertNull   (diagram.getTrack(2));
        assertNull   (diagram.getTrack(3));
        assertNull   (diagram.getTrack(-1));
        assertNull   (diagram.getTrack(-2));
        assertNull   (diagram.getTrack(-3));
    }

    /**
     * Test that a single gene on the reverse strand is correctly placed in track -1,
     * and that (a reasonable sample of) the other tracks are unassigned.
     */
    @Test public void correctTracksNegative() {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "isolated2", 100);

        assertEquals(diagram.numberOfPositiveTracks(), 0);
        assertEquals(diagram.numberOfNegativeTracks(), 1);

        assertNull   (diagram.getTrack(1));
        assertNull   (diagram.getTrack(2));
        assertNull   (diagram.getTrack(3));
        assertNotNull(diagram.getTrack(-1));
        assertNull   (diagram.getTrack(-2));
        assertNull   (diagram.getTrack(-3));
    }

    /**
     * Test that centring on a gene near the left-hand end of the chromosome
     * correctly begins the diagram at zero.
     */
    @Test
    public void nearLeftEnd () {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "lhs", 100);
        assertEquals(0, diagram.getStart());
        assertEquals(100, diagram.getEnd());
    }

    /**
     * Test that centring on a gene near the left-hand end of the chromosome
     * correctly begins the diagram at zero.
     */
    @Test
    public void nearRightEnd () {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "rhs", 100);
        assertEquals(900, diagram.getStart());
        assertEquals(1000, diagram.getEnd());
    }

    /**
     * Test that having a gene larger than the diagram works as expected.
     */
    @Test
    public void largeGene() {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "large", 100);
        int expectedCentre = (1000 + 100000) / 2;
        assertEquals(expectedCentre - 50, diagram.getStart());
        assertEquals(expectedCentre + 50, diagram.getEnd());
    }

    /**
     * Test that the track contains a single transcript with the correct
     * colour, and that the transcript contains a single exon at the correct
     * location.
     */
    @Test public void transcript() {
        ContextMapDiagram diagram = ContextMapDiagram.forGene(simple(), "isolated", 100);
        List<LocatedFeature> track = diagram.getTrack(1);
        assertNotNull(track);
        assertEquals(track.size(), 1);

        LocatedFeature transcriptFeature = track.get(0);
        assertNotNull(transcriptFeature);
        assertEquals(90,  transcriptFeature.getFmin());
        assertEquals(110, transcriptFeature.getFmax());

        assertTrue("The features of this diagram should be transcripts", transcriptFeature instanceof Transcript);
        Transcript transcript = (Transcript) transcriptFeature;

        assertNotNull(transcript.getColourId());
        assertEquals(2, transcript.getColourId().intValue());

        Set<TranscriptComponent> exons = transcript.getComponents();
        assertNotNull(exons);
        assertEquals(exons.size(), 1);

        for (TranscriptComponent exon: exons) {
            assertNotNull(exon);
            assertEquals(90,  exon.getStart());
            assertEquals(110, exon.getEnd());
        }
    }

    private void assertTrackAllocation (ContextMapDiagram diagram, String[][] expectedAssignments) {
        List<List<LocatedFeature>> tracks = new ArrayList<List<LocatedFeature>>();

        for (int i=1; i<=diagram.numberOfPositiveTracks(); i++)
            tracks.add(diagram.getTrack(i));

        for (int i=0; i<expectedAssignments.length; i++) {
            List<LocatedFeature> track = diagram.getTrack(i+1);
            String[] expected = expectedAssignments[i];

            if (expected == null) {
                assertNull(track);
                continue;
            }

            assertEquals(expected.length, track.size());
            for (int j=0; j < expected.length; j++) {
                String expectedGeneName = expected[j];
                LocatedFeature transcriptFeature = track.get(j);

                assertTrue("The features of this diagram should be transcripts", transcriptFeature instanceof Transcript);
                Transcript transcript = (Transcript) transcriptFeature;

                if (expectedGeneName.contains("/")) {
                    // gene/transcript
                    int slashLoc = expectedGeneName.indexOf('/');
                    String expectedTranscriptName = expectedGeneName.substring(slashLoc+1);
                    expectedGeneName = expectedGeneName.substring(0, slashLoc);

                    assertEquals(expectedTranscriptName, transcript.getUniqueName());
                }
                assertEquals(expectedGeneName, transcript.getGene().getDisplayName());
            }
        }
    }

    // Tests for simple genes with overlaps
    private BasicGeneService simpleOverlaps () {
        MockBasicGeneService mockBasicGeneService = new MockBasicGeneService("dog", "chr1", 1);
        mockBasicGeneService.addSimpleGene("one",   10, 30);
        mockBasicGeneService.addSimpleGene("two",   20, 40);
        mockBasicGeneService.addSimpleGene("three", 30, 50);
        mockBasicGeneService.addSimpleGene("four",  40, 60);
        mockBasicGeneService.addSimpleGene("five",  50, 70);

        return mockBasicGeneService;
    }

    /**
     * Test that track-allocation works correctly with some overlapping genes.
     */
    @Test public void overlappingTrackAllocation() {
        ContextMapDiagram diagram = ContextMapDiagram.forRegion(simpleOverlaps(), "dog", "chr1", 101, 0, 100);
        String[][] expectedTracks = new String[][] {
                new String[] {"one", "four"},
                new String[] {"two", "five"},
                new String[] {"three"},
                null
        };
        assertEquals(3, diagram.numberOfPositiveTracks());
        assertTrackAllocation(diagram, expectedTracks);
    }

    // Tests for alternative splicing

    private BasicGeneService altSimple (int strand) {
        MockBasicGeneService mockBasicGeneService = new MockBasicGeneService("dog", "chr1", strand);
        mockBasicGeneService.addAltGene("one", 10, 50)
            .transcript("t1", 10, 30)
            .transcript("t2", 30, 50);

        return mockBasicGeneService;
    }

    @Test public void altSimpleTrackAllocationPositive() {
        ContextMapDiagram diagram = ContextMapDiagram.forRegion(altSimple(1), "dog", "chr1", 101, 0, 100);
        assertEquals(0, diagram.numberOfNegativeTracks());
        assertEquals(2, diagram.numberOfPositiveTracks());
        assertEquals(2, diagram.numberOfTracks());
    }

    @Test public void altSimpleTrackAllocationNegative() {
        ContextMapDiagram diagram = ContextMapDiagram.forRegion(altSimple(-1), "dog", "chr1", 101, 0, 100);
        assertEquals(2, diagram.numberOfNegativeTracks());
        assertEquals(0, diagram.numberOfPositiveTracks());
        assertEquals(2, diagram.numberOfTracks());
    }

    private BasicGeneService altOverlaps () {
        MockBasicGeneService mockBasicGeneService = new MockBasicGeneService("dog", "chr1", 1);
        mockBasicGeneService.addAltGene("one", 10, 50)
            .transcript("t1", 10, 30)
            .transcript("t2", 30, 50);
        mockBasicGeneService.addSimpleGene("two",   20, 40);
        mockBasicGeneService.addSimpleGene("four",  40, 60);
        mockBasicGeneService.addSimpleGene("five",  50, 70);
        mockBasicGeneService.addSimpleGene("six",   60, 80);
        mockBasicGeneService.addSimpleGene("seven", 70, 90);

        return mockBasicGeneService;
    }

    /**
     * Test that track-allocation works correctly in the presence of alternative splicing.
     */
    @Test public void altOverlapTrackAllocation() {
        ContextMapDiagram diagram = ContextMapDiagram.forRegion(altOverlaps(), "dog", "chr1", 101, 0, 100);
        String[][] expectedTracks = new String[][] {
                new String[] {"one/t1", "six"},
                new String[] {"one/t2", "seven"},
                new String[] {"two", "five"},
                new String[] {"four"},
                null
        };
        assertEquals(4, diagram.numberOfPositiveTracks());
        assertTrackAllocation(diagram, expectedTracks);
    }
}
