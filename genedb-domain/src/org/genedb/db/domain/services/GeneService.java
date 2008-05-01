package org.genedb.db.domain.services;

import org.genedb.db.domain.objects.Gene;

public interface GeneService extends BasicGeneService {
    Gene findGeneByUniqueName(String name);
}
