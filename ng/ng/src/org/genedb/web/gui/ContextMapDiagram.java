package org.genedb.web.gui;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Gap;
import org.genedb.db.domain.services.BasicGeneService;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
 * gene is often different.) At the time of writing (2008-04-28) GeneDB
 * does not contain any nested genes, but this may change in future.
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
public class ContextMapDiagram extends TrackedDiagram {
    /*
     * Implementation note: The following is not part of the specification of
     * the class, and should be regarded as an implementation detail.
     *
     * All the transcripts of a particular gene are placed together, on
     * adjacent tracks.
     *
     * Where two genes overlap, the one with the leftmost start will always be on
     * a lower track. If the overlapping genes have the same start position, the
     * longer of the two will be placed below it (so that the configuration
     * looks stable; it's subconsciously disturbing to look at a diagram that
     * appears liable to overbalance).
     *
     * If two genes abut precisely (is that even biologically possible?), they
     * are treated as overlapping and placed on different tracks.
     *
     * We might need to inspect genes outside the bounds of the map to achieve
     * consistency: because of the leftmost-below rule, we only need to look to
     * the left. If there is a gene that extends off the left-hand end of the
     * map, we need to find all the genes that overlap with it to establish
     * which track it should be in. This procedure may need to be iterated -- in
     * the theoretical worst-case, all the way to the leftmost end of the chromosome
     * or contig. (In practice it is unusual for the procedure to be
     * iterated at all, because overlaps are relatively rare.)
     */

    private static final Logger logger = Logger.getLogger(ContextMapDiagram.class);

    private String organism, chromosome;

    private int chromosomeFeatureId;
    /**
     * Users should not call this constructor directly, but use one of the
     * static methods defined below.
     */
    private ContextMapDiagram(String organism, String chromosome, int chromosomeFeatureId, int start, int end) {
        super(start, end);
        this.organism = organism;
        this.chromosome = chromosome;
        this.chromosomeFeatureId = chromosomeFeatureId;
    }

    /**
     * Create a context map diagram of length <code>size</code>, centred about the specified
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
        int geneCentre = (gene.getFmin() + gene.getFmax()) / 2;
        if (size > chromosomeLength) {
            logger.info(String.format("Trying to create diagram of size %d for a chromosome of length %d", size, chromosomeLength));
            start = 0;
            end = chromosomeLength;
        }
        else {
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
        int chromosomeFeatureId = gene.getChromosome().getFeatureId();

        return forRegion(basicGeneService, organismName, chromosomeName, chromosomeFeatureId, start, end);
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
            String organismName, String chromosomeName, int chromosomeFeatureId, int start, int end) {
        ContextMapDiagram diagram = new ContextMapDiagram(organismName, chromosomeName, chromosomeFeatureId, start, end);

        diagram.allocateTracks(diagram.genes(basicGeneService, organismName, chromosomeName, +1, start, end), false);
        diagram.allocateTracks(diagram.genes(basicGeneService, organismName, chromosomeName, -1, start, end), true);

        for (Gap gap: basicGeneService.findGapsOverlappingRange(organismName, chromosomeName, start, end))
            diagram.addGlobalFeature(gap);

        return diagram;
    }

    public static ContextMapDiagram forChromosome(BasicGeneService basicGeneService,
            String organismName, String chromosomeName, int chromosomeFeatureId, int chromosomeLength) {
        ContextMapDiagram diagram = new ContextMapDiagram(organismName, chromosomeName, chromosomeFeatureId, 0, chromosomeLength);

        diagram.allocateTracks(diagram.genes(basicGeneService, organismName, chromosomeName, +1), false);
        diagram.allocateTracks(diagram.genes(basicGeneService, organismName, chromosomeName, -1), true);

        for (Gap gap: basicGeneService.findGapsOnChromosome(organismName, chromosomeName)) {
            diagram.addGlobalFeature(gap);
        }

        return diagram;
    }


    /**
     * Get the common name of the organism of whose genome this map represents part.
     * @return the common name of the organism
     */
    public String getOrganism() {
        return organism;
    }

    /**
     * Get the name of the chromosome of which this map represents part.
     * @return the name of the chromosome
     */
    public String getChromosome() {
        return chromosome;
    }

    public int getChromosomeFeatureId() {
        return chromosomeFeatureId;
    }

    private Set<BasicGene> genes(BasicGeneService basicGeneService, String organismName,
        String chromosomeName, int strand) {
        return genes(basicGeneService, organismName, chromosomeName, strand, getStart(), getEnd());
    }

    /**
     * Return a set of BasicGene features, such that
     * the leftmost feature overlaps only with features included in the set. We may
     * need to include features beyond the left-hand end of the stated range in order to
     * achieve this; we include only as many as necessary.
     */
    private Set<BasicGene> genes(BasicGeneService basicGeneService, String organismName,
            String chromosomeName, int strand, int start, int end) {

        SortedSet<BasicGene> genes = new TreeSet<BasicGene>(
            basicGeneService.findGenesOverlappingRange(organismName,
                chromosomeName, strand, start, end));

        if (genes.isEmpty())
            return genes;

        int newStart;
        while ((newStart = genes.first().getFmin()) < start) {
            Collection<BasicGene> moreGenes = basicGeneService.findGenesOverlappingRange(
                organismName, chromosomeName, strand, newStart, start);
            genes.addAll(moreGenes);
            start = newStart;
        }

        return genes;
    }

}
