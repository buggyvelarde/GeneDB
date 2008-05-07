package org.genedb.db.domain.services;

import java.util.Collection;
import java.util.List;

import org.genedb.db.domain.objects.BasicGene;

public interface BasicGeneService {
    BasicGene findGeneByUniqueName(String name);

    /**
     * Find all genes any part of which is contained in the specified range.
     * Should NOT include genes whose <code>fmax</code> is exactly equal to <code>locMax</code>.
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
     * Find all genes whose 3' end is contained in the specified range but whose 5' end is not.
     * SHOULD include genes whose <code>fmax</code> is exactly equal to <code>locMax</code>,
     * provided that their <code>fmin</code> is less that <code>locMin</code>.
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
