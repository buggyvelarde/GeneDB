package org.genedb.querying.tmpquery;

import static org.junit.Assert.*;

import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.querying.core.QueryException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:///Users/art/Documents/sts-workspace/genedb-query/classpath/testContext.xml"})
public class SimpleNameQueryTest {

    @Autowired
    private TaxonNodeManager taxonNodeManager;

    @Autowired
    private transient LuceneIndexFactory luceneIndexFactory;



    @SuppressWarnings("unchecked")
    @Test
    public void basic() throws QueryException {
        SimpleNameQuery q = new SimpleNameQuery();

        q.taxonNodeManager = taxonNodeManager;
        q.luceneIndexFactory = luceneIndexFactory;
        q.afterPropertiesSet();

        q.setSearch("Tb927.1.*");
        List results = q.getResults();
        assertEquals(522, results.size());
    }

}
