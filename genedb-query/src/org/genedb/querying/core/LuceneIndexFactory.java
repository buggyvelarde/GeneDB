package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class LuceneIndexFactory {

    private static final Logger logger = Logger.getLogger(LuceneIndexFactory.class);

    private Map<String, LuceneIndex> mapping = new HashMap<String, LuceneIndex>();

    public LuceneIndex getIndex(String key) {
        if (mapping.containsKey(key)) {
            return mapping.get(key);
        }
        throw new RuntimeException(String.format("Unable to find a LuceneIndex of name '%s'", key));
    }

    public void setLuceneIndexList(List<LuceneIndex> luceneIndexList) {
        for (LuceneIndex luceneIndex : luceneIndexList) {
            mapping.put(luceneIndex.getIndexName(), luceneIndex);
            logger.trace(String.format("Storing '%s' in Lucene index list", luceneIndex.getIndexName()));
        }
    }


}
