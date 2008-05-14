package org.genedb.web.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.services.BasicGeneService;

/**
 * This class represents a context map diagram in an abstract way, i.e.
 * independent of the specific technology used to display the diagram.
 * <p>
 * A context map diagram represents a contiguous segment of a chromosome or contig.
 * It consists of a number of positive (forward) and negative (reverse) tracks.
 * Each track contains a number of disjoint genes, each of which consists of a
 * number of disjoint exons implicitly separated by introns.
 * <p>
 * In the commonest case, there is at most one positive and one negative track.
 * However, a single-track diagram may prove impossible for one of the following
 * reasons:
 * <ul>
 * <li> If a gene is alternatively spliced, we represent each splicing on a
 * separate track.
 * 
 * <li> Sometimes the database contains unconfirmed gene predictions made
 * automatically by software. In this case, it is possible to have inconsistent
 * overlapping predictions, at most one of which will be correct.
 * 
 * <li> Some organisms have nested genes, where a gene is contained wholly
 * within an exon belonging to another gene. (The reading frame of the "inner"
 * gene is often different.) At the time of writing (2008-04-28) the pathogen
 * database does not contain any nested genes, but this may change in future.
 * </ul>
 * 
 * A particular gene should always appear on the same track, no matter how far
 * the actual map region extends. It is therefore possible for a particular gene
 * to be pushed to a higher track by an overlapping gene that is not visible on
 * the map, because it lies outside the boundaries. This makes it possible to
 * stitch together adjacent maps seamlessly. Of course it <i>may</i> happen
 * that some views have more tracks than others; a context map display tool will
 * need to allow for this.
 * 
 * @author rh11
 * 
 */
public class ContextMapDiagram {
    /*
     * Implementation note: The following is not part of the specification of
     * the class, and should be regarded as an implementation detail.
     *
     * All the transcripts of a particular gene are placed together, on
     * adjacent tracks.
     * 
     * Where two genes overlap, the one with the 5'-most start will always be on
     * a lower track. If the overlapping genes have the same start position, the
     * longer of the two will be placed below it (so that the configuration
     * looks stable; it's subconsciously disturbing to look at a diagram that
     * appears liable to overbalance).
     * 
     * If two genes abut precisely (is that even biologically possible?), they
     * are treated as overlapping and placed on different tracks.
     * 
     * We might need to inspect genes outside the bounds of the map to achieve
     * consistency: because of the 5'most-below rule, we only need to look in
     * the 5' direction. If there is a gene that extends off the 5' end of the
     * map, we need to find all the genes that overlap with it to establish
     * which track it should be in. This procedure may need to be iterated -- in
     * the theoretical worst-case, all the way to the 5' end of the chromosome
     * or contig. (In practice it is usual that the procedure ever needs to be
     * iterated at all, because overlaps are relatively rare.)
     */
    
    private static Logger logger = Logger.getLogger(ContextMapDiagram.class);

    private class Half {
        public int numTracks = 0;
        private Map<Integer,List<Transcript>> tracks = new HashMap<Integer,List<Transcript>>();
        public void addGene(BasicGene gene, int track) {
            for (Transcript transcript: gene.getTranscripts()) {
                if (!tracks.containsKey(track))
                    tracks.put(track, new ArrayList<Transcript>());
                tracks.get(track).add(transcript);
                track++;
            }
        }
        public List<Transcript> getTrack(int track) {
            return tracks.get(track);
        }
    }

    private int start, end;
    private Half positiveHalf, negativeHalf;
    
    public int numberOfPositiveTracks() {
        return positiveHalf.numTracks;
    }
    public int numberOfNegativeTracks() {
        return negativeHalf.numTracks;
    }
    /**
     * The track number should be between -1 and <code>-numberOfNegativeTracks()</code>
     * inclusive, to retrieve a negative track; or between 1 and <code>numberOfPositiveTracks()</code>
     * inclusive, to retrieve a negative track.
     * 
     * @param track
     * @return a list of Transcripts, or null if there are none
     */
    public List<Transcript> getTrack(int track) {
        if (track == 0)
            throw new IllegalArgumentException("The track specifier must be positive or negative, not zero");
        else if (track < 0)
            return negativeHalf.getTrack(-track-1);
        else
            return positiveHalf.getTrack(track-1);
    }

    /**
     * Users should not call this constructor directly, but use one of the
     * static methods defined below.
     */
    private ContextMapDiagram(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Create a context map diagram of length size, centred about the specified
     * gene.
     * 
     * @param basicGeneService A BasicGeneService, used to fetch the genes from
     *                a data store
     * @param geneName The unique name of the gene
     * @param size The size (in bases) of the diagram
     * @return
     */
    public static ContextMapDiagram forGene(BasicGeneService basicGeneService, String geneName,
            int size) {
        BasicGene gene = basicGeneService.findGeneByUniqueName(geneName);
        int chromosomeLength = gene.getChromosome().getLength();
        
        int start, end;
        if (size > chromosomeLength) {
            logger.info(String.format("Trying to create diagram of size %d for a chromosome of length %d", size, chromosomeLength));
            start = 0;
            end = chromosomeLength;
        }
        else {
            int geneCentre = (gene.getFmin() + gene.getFmax()) / 2;
            start = geneCentre - (size / 2);
            end = start + size;
            
            if (start < 0) {
                start = 0;
                end = size;
            }
            else if (end > chromosomeLength) {
                end = chromosomeLength;
                start = end - size;
            }
        }
                
        String organismName = gene.getOrganism();
        String chromosomeName = gene.getChromosomeName();

        return forRegion(basicGeneService, organismName, chromosomeName, start, end);
    }

    /**
     * Create a context map diagram for the specified region of the specified
     * chromosome.
     * 
     * @param basicGeneService A BasicGeneService, used to fetch the genes from
     *                a data store
     * @param organismName The common name of the organism
     * @param chromosomeName The name of the chromosome
     * @param start The start position (measured in bases from the 5' end)
     * @param end The end position
     * @return
     */
    public static ContextMapDiagram forRegion(BasicGeneService basicGeneService,
            String organismName, String chromosomeName, int start, int end) {
        ContextMapDiagram diagram = new ContextMapDiagram(start, end);
                
        diagram.positiveHalf = diagram.createHalf(basicGeneService, organismName, chromosomeName, +1, start, end);
        diagram.negativeHalf = diagram.createHalf(basicGeneService, organismName, chromosomeName, -1, start, end);

        return diagram;
    }
    
    // The next segment of code implements track allocation
    
    /**
     * Represents the boundary (either the START of the END) of a gene.
     */
    private static class GeneBoundary implements Comparable<GeneBoundary> {
        /**
         * Compares GeneBoundaries, ordering them in the appropriate way for
         * track-allocation. If two boundaries have different locations, the
         * leftmost one goes first; if their locations coincide then we put
         * START boundaries before ENDs, and we put the START of a longer gene
         * before the START of a shorter. 
         */
        public int compareTo(GeneBoundary other) {
            int thisLoc  = this.getLocation();
            int otherLoc = other.getLocation();
            
            if (thisLoc != otherLoc)
                return (thisLoc - otherLoc);
            else if (this.type == GeneBoundary.Type.START && other.type == GeneBoundary.Type.END)
                return -1;
            else if (this.type == GeneBoundary.Type.END && other.type == GeneBoundary.Type.START)
                return +1;
            else if (this.type == GeneBoundary.Type.START && other.type == GeneBoundary.Type.START) {
                int ret = other.gene.getFmax() - this.gene.getFmax();
                if (ret != 0)
                    return ret;
            }
            return this.gene.getSystematicId().compareTo(other.gene.getSystematicId());
        }
        
        /**
         * Two GeneBoundary objects are equal if they have the same type
         * and they refer to the same gene.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GeneBoundary) {
                GeneBoundary other = (GeneBoundary) obj;
                return (this.type == other.type && this.gene.equals(other.gene));
            }
            return false;
        }

        public enum Type {START, END}
        public Type type;
        public BasicGene gene;
        
        GeneBoundary(Type type, BasicGene gene) {
            this.type = type;
            this.gene = gene;
        }
        /**
         * Returns the location of the boundary, which is to say the
         * <code>fmin</code> of the gene for a START boundary, or the <code>fmax</code> for an END.
         * 
         * @return the location
         */
        public int getLocation () {
            switch (type) {
            case START: return gene.getFmin();
            case END:   return gene.getFmax();
            default:    throw new IllegalStateException("GeneBoundary type is invalid. This should never happen!");
            }
        }

        @Override
        public String toString() {
            return String.format("%s of %s (%d..%d)", type, gene.displayName(), gene.getFmin(), gene.getFmax());
        }
    }
    
    private class GeneBoundarySet extends TreeSet<GeneBoundary> {
        public GeneBoundarySet (Collection<BasicGene> genes) {
            super();
            addGenes(genes);
        }
        public void addGenes(Collection<BasicGene> genes) {
            for (BasicGene gene: genes) {
                this.add(new GeneBoundary(GeneBoundary.Type.START, gene));
                this.add(new GeneBoundary(GeneBoundary.Type.END,   gene));
            }
        }
        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            boolean first = true;
            for (GeneBoundary boundary: this) {
                if (first)
                    first = false;
                else
                    s.append(", ");
                s.append(boundary.toString());
            }
            return s.toString();
        }
    }
    
    /**
     * Return a <code>GeneBoundarySet</code> of matched boundaries, such that the 5'-most gene
     * overlaps only with genes included in the set. We may need to include genes beyond
     * the 5' end of the stated range in order to achieve this; we include only as many
     * as necessary.
     * 
     * @param genes
     * @return
     */
    private GeneBoundarySet boundaries(BasicGeneService basicGeneService, String organismName, String chromosomeName, int strand, int start, int end) {
        Collection<BasicGene> genes = basicGeneService.findGenesOverlappingRange(organismName, chromosomeName, strand, start, end);

        GeneBoundarySet boundaries = new GeneBoundarySet(genes);
        if (boundaries.isEmpty())
            return boundaries;
        
        int newStart;
        while ((newStart = boundaries.first().getLocation()) < start) {
            Collection<BasicGene> moreGenes = basicGeneService.findGenesOverlappingRange(organismName, chromosomeName, strand, newStart, start);
            boundaries.addGenes(moreGenes);
            start = newStart;
        }
        
        return boundaries;
    }
    
    /**
     * Allocates each transcript to a track in the appropriate way,
     * grouping together the different transcripts of each gene.
     * 
     * @param  genes
     * @return the diagram Half
     */
    private Half createHalf(BasicGeneService basicGeneService, String organismName, String chromosomeName, int strand, int start, int end) {
        Half half = new Half();
        SortedSet<GeneBoundary> boundaries = boundaries(basicGeneService, organismName, chromosomeName, strand, start, end);
        
        int numTracks = 0;
        Set<Integer>           activeTracks = new HashSet<Integer> ();
        Map<BasicGene,Integer> activeGenes  = new HashMap<BasicGene,Integer> ();
        for(GeneBoundary boundary: boundaries) {
            BasicGene gene = boundary.gene;
            int numTranscripts = gene.getTranscripts().size();
            int track, lastUsedTrack;
            switch (boundary.type) {
            case START:
                track = findGap(activeTracks, numTranscripts);
                lastUsedTrack = track + numTranscripts - 1;
                if (lastUsedTrack >= numTracks)
                    numTracks = lastUsedTrack + 1;
                for(int i = track; i <= lastUsedTrack; i++)
                    activeTracks.add(i);
                activeGenes.put(gene, track);
                break;
            case END:
                track = activeGenes.get(gene);
                lastUsedTrack = track + numTranscripts - 1;
                half.addGene(gene, track);
                activeGenes.remove(gene);

                for(int i = track; i <= lastUsedTrack; i++)
                    activeTracks.remove(i);
                break;
            default: throw new IllegalStateException("GeneBoundary type is invalid. This should never happen!");
            }
        }
        half.numTracks = numTracks;
        return half;
    }
    
    /**
     * Find the first gap large enough to contain <code>gapSize</code> entries.
     * 
     * @param filled    the set of unavailable indices
     * @param gapSize   the size of the required gap, ³ 1
     * @return          the index of the start of the first sufficiently-large gap
     */
    private static int findGap(Set<Integer> filled, int gapSize) {
        /* Irrelevant note:
         *
         * This algorithm is linear-time and constant-space.
         * I reckon it should be possible to do better with a
         * special-purpose data structure; presumably there's
         * a huge literature on this in the context of memory
         * management / allocation.
         */
        if (gapSize < 1)
            throw new IllegalArgumentException(String
                .format("gapSize is %d, must be ³1", gapSize));

        int currentGapSize = 0;
        for (int i=0; ; i++) {
            if (filled.contains(i))
                currentGapSize = 0;
            else {
                if (++currentGapSize == gapSize)
                    return i - gapSize + 1;
            }
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
