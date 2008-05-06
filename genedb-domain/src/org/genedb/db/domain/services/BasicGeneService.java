package org.genedb.db.domain.services;

import java.util.Collection;
import java.util.List;

import org.genedb.db.domain.objects.BasicGene;

public interface BasicGeneService {
    BasicGene findGeneByUniqueName(String name);

    Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax);

    List<String> findGeneNamesByPartialName(String search);
}
