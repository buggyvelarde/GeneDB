package org.genedb.jogra.services;

import org.genedb.jogra.domain.Gene;


public interface GeneService extends BasicGeneService {
    Gene findGeneByUniqueName(String name);
}
