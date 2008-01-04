package org.genedb.db.domain.services;

import java.util.List;

import org.genedb.db.domain.objects.Gene;

public interface GeneService {
	Gene findGeneByUniqueName(String name);

	List<String> findGeneNamesByPartialName(String search);
}
