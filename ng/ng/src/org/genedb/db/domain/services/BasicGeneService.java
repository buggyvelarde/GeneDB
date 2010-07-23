package org.genedb.db.domain.services;

import java.util.Collection;
import java.util.List;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Gap;

public interface BasicGeneService {
    BasicGene findGeneByUniqueName(String name);

    /**
     * Find all genes on a particular strand of a particular chromosome.
     *
     * @param organismCommonName
     * @param chromosomeUniqueName
     * @param strand
     * @return
     */
    Collection<BasicGene> findGenesOnStrand(String organismCommonName,
            String chromosomeUniqueName, int strand);

    /**
     * Find all genes either end of which is in the range [<code>locMin</code>, <code>locMax</code>)
     * specified in interbase co-ordinates. This amounts to finding all genes any part of which
     * lies between the specified interbase locations, as well as any gene whose 3' end lies
     * precisely at <code>locMin</code>.
     *
     * @param organismCommonName
     * @param chromosomeUniqueName
     * @param strand
     * @param locMin
     * @param locMax
     * @return
     */
    Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax);

    /**
     * Find all gaps either end of which is in the range [<code>locMin</code>, <code>locMax</code>)
     * specified in interbase co-ordinates. This amounts to finding all gaps any part of which
     * lies between the specified interbase locations, as well as any gap whose right-hand end lies
     * precisely at <code>locMin</code>.
     *
     * @param organismCommonName
     * @param chromosomeUniqueName
     * @param locMin
     * @param locMax
     * @return
     */
    public Collection<Gap> findGapsOverlappingRange(String organismCommonName,
        String chromosomeUniqueName, long locMin, long locMax);

    /**
     * Find all gaps on the specified chromosome.
     *
     * @param organismCommonName
     * @param chromosomeUniqueName
     * @return
     */
    public Collection<Gap> findGapsOnChromosome(String organismCommonName,
        String chromosomeUniqueName);

    /**
     * Find all genes whose 3' end is contained in the range [<code>locMin</code>, <code>locMax</code>).
     * I.e. includes genes that stop precisely at <code>locMin</code>, but not those that stop
     * precisely at <code>locMax</code>.
     *
     * @param organismCommonName
     * @param chromosomeUniqueName
     * @param strand
     * @param locMin
     * @param locMax
     * @return
     */
    Collection<BasicGene> findGenesExtendingIntoRange(String organismCommonName,
        String chromosomeUniqueName, int strand, long locMin, long locMax);

    List<String> findGeneNamesByPartialName(String search);
}
