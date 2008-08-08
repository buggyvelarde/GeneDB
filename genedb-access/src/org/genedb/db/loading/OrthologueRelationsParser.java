package org.genedb.db.loading;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OrthologueRelationsParser {
    
    public void parseInput(Reader r, Set<GenePair> orthologues, Set<GenePair> paralogues, 
            Map<String,List<String>> clusters);

}
