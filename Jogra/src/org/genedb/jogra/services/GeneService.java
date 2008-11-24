package org.genedb.jogra.services;

import org.genedb.jogra.domain.Gene;

import java.util.List;


public interface GeneService {
    Gene findGeneByUniqueName(String name);
    List<String> findTranscriptNamesByPartialName(String search);
}
