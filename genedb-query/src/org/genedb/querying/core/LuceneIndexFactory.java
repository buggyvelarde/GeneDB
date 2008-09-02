package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class LuceneIndexFactory {

    private static final Logger logger = Logger.getLogger(LuceneIndexFactory.class);

    private Map<String, LuceneIndex> mapping = new HashMap<String, LuceneIndex>();

    public LuceneIndex getIndex(String key) {
    	return mapping.get(key);
    }

	public void setLuceneIndexList(List<LuceneIndex> luceneIndexList) {
		for (LuceneIndex luceneIndex : luceneIndexList) {
			//String shortName = StringUtils.unqualify(luceneIndex.getIndexName());
			mapping.put(luceneIndex.getIndexName(), luceneIndex);
		}
	}

}
