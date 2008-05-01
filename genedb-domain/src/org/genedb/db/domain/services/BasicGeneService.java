package org.genedb.db.domain.services;

import java.util.List;

import org.genedb.db.domain.objects.BasicGene;

public interface BasicGeneService {
    BasicGene findGeneByUniqueName(String name);

    List<String> findGeneNamesByPartialName(String search);
}
