package org.genedb.querying.core;

import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class LuceneIndexFactory {

    private Map<String, LuceneIndex> mapping = new HashMap<String, LuceneIndex>();

    public LuceneIndex getIndex(String key) {
        if (mapping.containsKey(key)) {
            return mapping.get(key);
        }
        throw new RuntimeException(String.format("Unable to find a LuceneIndex of name '%s'", key));
    }

    public void setLuceneIndexList(List<LuceneIndex> luceneIndexList) {
        for (LuceneIndex luceneIndex : luceneIndexList) {
            //String shortName = StringUtils.unqualify(luceneIndex.getIndexName());
            mapping.put(luceneIndex.getIndexName(), luceneIndex);
        }
    }

}
