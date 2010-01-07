package org.genedb.db.domain.test;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Chromosome;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Gap;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.objects.TranscriptComponent;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.util.TwoKeyMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Used to generate BasicGene objects for testing.
 * Ordinarily used via the proxy methods of {@link MockBasicGeneService}
 * rather than directly.
 *
 * @author rh11
 *
 */
class BasicGeneFactory {
    private int nextId;

    private String organism;
    private Chromosome chromosome;
    private int strand;

    /**
     * The default length of a test chromosome, if not explicitly specified.
     */
    private static int DEFAULT_CHR_LENGTH = 1000000;

    private static int DEFAULT_CHR_ID = 101;

    /**
     * Sets the default organism, chromosome and strand for genes produced by this factory.
     *
     * @param organism
     * @param chromosome
     * @param strand
     */
    public BasicGeneFactory(String organism, String chromosome, int strand) {
        assert strand == 1 || strand == -1;

        this.nextId = 1;
        this.organism = organism;
        this.chromosome = new Chromosome(chromosome, DEFAULT_CHR_ID, DEFAULT_CHR_LENGTH);
        this.strand = strand;
    }

    /**
     * Create a BasicGene with the specified uniqueName and location,
     * on the strand and chromosome specified by this factory. A gene created
     * by this method will have no transcripts. Transcripts must be added
     * manually, perhaps using {@link BasicGeneHelper}.
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     * @return
     */
    public BasicGene nakedGene (String uniqueName, int fmin, int fmax) {
        BasicGene gene = new BasicGene();
        gene.setFeatureId(nextId++);
        gene.setOrganism(organism);
        gene.setChromosome(chromosome);
        gene.setStrand(strand);

        gene.setUniqueName(uniqueName);
        gene.setFmin(fmin);
        gene.setFmax(fmax);

        return gene;
    }

    /**
     * Create a new BasicGene with the specified uniqueName and location,
     * on the strand and chromosome specified by this factory. A gene created
     * by this method will have one transcript, consisting of one exon.
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     * @return
     */
    public BasicGene simpleGene (String uniqueName, int fmin, int fmax, Integer colourId) {
        BasicGene gene = nakedGene(uniqueName, fmin, fmax);
        // Produce a single transcript for this gene
        Transcript transcript = new Transcript();
        transcript.setFmin(fmin);
        transcript.setFmax(fmax);
        transcript.setColourId(colourId);

        // with a single exon
        Set<TranscriptComponent> exons = new HashSet<TranscriptComponent> ();
        exons.add(new Exon(fmin, fmax));
        transcript.setComponents(exons);

        gene.addTranscript(transcript);

        return gene;
    }

    /**
     * Create a new BasicGene with the specified uniqueName, name and location,
     * on the strand and chromosome specified by this factory.
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     * @return
     */
    public BasicGene simpleGene (String uniqueName, String name, int fmin, int fmax, Integer colourId) {
        BasicGene gene = simpleGene(uniqueName, fmin, fmax, colourId);
        gene.setName(name);

        return gene;
    }

    /**
     * Change the default organism for genes produced by this factory.
     *
     *
     * @param organism The name of the new default organism
     */
    public void setOrganism(String organism) {
        this.organism = organism;
    }
    /**
     * Change the default chromosome for genes produced by this factory.
     *
     * @param chromosome The new default chromosome
     */
    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }
    /**
     * Change the default chromosome for genes produced by this factory.
     * This convenience method creates a new {@link org.genedb.db.domain.objects.Chromosome}
     * object with a default length.
     *
     * @param chromosome The name of the new default chromosome
     */
    public void setChromosome(String chromosomeName) {
        this.chromosome = new Chromosome(chromosomeName, DEFAULT_CHR_ID, DEFAULT_CHR_LENGTH);
    }
    /**
     * Change the default strand for genes produced by this factory.
     *
     * @param strand The new default strand (<code>1</code> or <code>-1</code>)
     */
    public void setStrand(int strand) {
        assert strand == 1 || strand == -1;

        this.strand = strand;
    }

    public String getOrganism() {
        return organism;
    }

    public String getChromosomeName() {
        return chromosome.getName();
    }
}

/**
 * A stub class that implements the {@link org.genedb.db.domain.services.BasicGeneService} interface
 * using sample data. A new instance initially contains no genes; genes can be added using the methods
 * {@link #addSimpleGene(String,int,int,Integer)}, {@link #addSimpleGene(String,String,int,int,Integer)}
 * etc.
 *
 * @author rh11
 *
 */
public class MockBasicGeneService implements BasicGeneService {
    private BasicGeneFactory factory;
    private Collection<BasicGene> genes;
    private TwoKeyMap<String,String,Collection<Gap>> gapsByOrganismAndChromosome;

    /**
     * Creates a new instance that uses the supplied organism, chromosome and strand
     * as the default for genes that are added using the <code>add*Gene</code> methods.
     *
     * These defaults can be changed with the <code>set*</code> methods, which change
     * the values used for genes added subsequently.
     *
     * @param organism
     * @param chromosome
     * @param strand
     */
    public MockBasicGeneService (String organism, String chromosome, int strand) {
        factory = new BasicGeneFactory(organism, chromosome, strand);
        genes = new ArrayList<BasicGene> ();
        gapsByOrganismAndChromosome = new TwoKeyMap<String,String,Collection<Gap>>();
    }

    /**
     * Add a simple, minimal gene.
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     */
    public void addSimpleGene(String uniqueName, int fmin, int fmax) {
        genes.add(factory.simpleGene(uniqueName, fmin, fmax, null));
    }

    /**
     * Add a simple Gene.
     * This method is a proxy for {@see BasicGeneFactory.addSimpleGene(String,int,int,Integer)}.
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     * @param colourId
     */
    public void addSimpleGene(String uniqueName, int fmin, int fmax, Integer colourId) {
        genes.add(factory.simpleGene(uniqueName, fmin, fmax, colourId));
    }

    /**
     * Add a simple Gene with a name and a colour.
     * This method is a proxy for {@see BasicGeneFactory.addSimpleGene(String,int,int,Integer)}.
     *
     * @param uniqueName
     * @param name
     * @param fmin
     * @param fmax
     * @param colourId
     */
    public void addSimpleGene(String uniqueName, String name, int fmin, int fmax, Integer colourId) {
        genes.add(factory.simpleGene(uniqueName, name, fmin, fmax, colourId));
    }

    /**
     * Add a gene with no transcripts. This is used to generate alternatively-spliced
     * test genes in conjunction with {@link BasicGeneHelper#transcript(String,int,int)}.
     *
     * For example,
     * <code>
     *     mockBasicGeneService.addAltGene("gene1", 100, 200)
     *          .transcript("rna1", 100, 150)
     *          .transcript("rna2", 120, 200);
     * </code>
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     * @return
     */
    public BasicGeneHelper addAltGene(String uniqueName, int fmin, int fmax) {
        BasicGeneHelper gene = new BasicGeneHelper(factory.nakedGene(uniqueName, fmin, fmax));
        genes.add(gene);
        return gene;
    }

    /**
     * Add a gap.
     *
     * @param uniqueName
     * @param fmin
     * @param fmax
     */
    public void addGap(String uniqueName, int fmin, int fmax) {
        String organism   = factory.getOrganism();
        String chromosome = factory.getChromosomeName();

        if (!gapsByOrganismAndChromosome.containsKey(organism,chromosome)) {
            gapsByOrganismAndChromosome.put(organism, chromosome, new TreeSet<Gap>());
        }

        gapsByOrganismAndChromosome.get(organism,chromosome).add(new Gap(uniqueName, fmin, fmax));
    }

    /**
     * Get all the gaps for the specified chromosome of the specified organism.
     *
     * @param organism the common name of the organism
     * @param chromosome the unique name of the chromosome
     * @return
     */
    public Collection<Gap> findGapsOnChromosome(String organism, String chromosome) {
        if (!gapsByOrganismAndChromosome.containsKey(organism, chromosome))
            return Collections.emptySet();

        return gapsByOrganismAndChromosome.get(organism, chromosome);
    }


    /**
     * Change the default organism used for new genes.
     * This method is a proxy for {@see BasicGeneFactory.setOrganism(String)}.
     *
     * @param organism
     */
    public void setOrganism(String organism) {
        factory.setOrganism(organism);
    }

    /**
     * Change the default chromosome used for new genes.
     * This method is a proxy for {@see BasicGeneFactory.setChromosome(String)}.
     *
     * @param chromosome
     */
    public void setChromosome(String chromosome) {
        factory.setChromosome(chromosome);
    }

    /**
     * Change the default chromosome used for new genes.
     * This method is a proxy for {@see BasicGeneFactory.setChromosome(Chromosome)}.
     *
     * @param chromosome
     */
    public void setChromosome(Chromosome chromosome) {
        factory.setChromosome(chromosome);
    }

    /**
     * Change the default strand used for new genes.
     * This method is a proxy for {@see BasicGeneFactory.setStrand(int)}.
     *
     * @param strand
     */
    public void setStrand(int strand) {
        factory.setStrand(strand);
    }

    public BasicGene findGeneByUniqueName(String name) {
        for (BasicGene gene: genes) {
            if (gene.getUniqueName().equals(name))
                return gene;
        }
        return null;
    }

    public List<String> findGeneNamesByPartialName(String search) {
        throw new RuntimeException("Did not expect ContextMapDiagram to call findGeneNamesByPartialName");
    }

    public Collection<BasicGene> findGenesOverlappingRange(String organism,
        String chromosome, int strand, long locMin, long locMax) {

        List<BasicGene> ret = new ArrayList<BasicGene> ();
        for (BasicGene gene: genes) {
            if (gene.getOrganism().equals(organism)
                    && gene.getChromosomeName().equals(chromosome)
                    && gene.getStrand() == strand
                    && gene.getFmin() < locMax
                    && gene.getFmax() >= locMin)
                ret.add(gene);
        }
        return ret;
    }

    public Collection<BasicGene> findGenesOnStrand(String organism,
            String chromosome, int strand) {

        List<BasicGene> ret = new ArrayList<BasicGene> ();
        for (BasicGene gene: genes) {
            if (gene.getOrganism().equals(organism)
                    && gene.getChromosomeName().equals(chromosome)
                    && gene.getStrand() == strand)
                ret.add(gene);
        }
        return ret;
    }

    public Collection<BasicGene> findGenesExtendingIntoRange(String organism,
            String chromosome, int strand, long locMin, long locMax) {

        List<BasicGene> ret = new ArrayList<BasicGene> ();
        for (BasicGene gene: genes) {
            if (gene.getOrganism().equals(organism)
                    && gene.getChromosomeName().equals(chromosome)
                    && gene.getStrand() == strand
                    && gene.getFmax() < locMax
                    && gene.getFmax() >= locMin)
                ret.add(gene);
        }
        return ret;
    }

    public Collection<Gap> findGapsOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, long locMin, long locMax) {

        List<Gap> ret = new ArrayList<Gap> ();
        for (Gap gap: findGapsOnChromosome(organismCommonName, chromosomeUniqueName)) {
            if (gap.getFmin() < locMax
             && gap.getFmax() >= locMin)
                ret.add(gap);
        }
        return ret;
    }

}

