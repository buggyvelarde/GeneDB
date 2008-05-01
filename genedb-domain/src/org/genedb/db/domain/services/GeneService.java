package org.genedb.db.domain.services;

import org.genedb.db.domain.objects.Gene;

import java.util.List;

public interface GeneService {
    Gene findGeneByUniqueName(String name);

    List<String> findGeneNamesByPartialName(String search);
}
