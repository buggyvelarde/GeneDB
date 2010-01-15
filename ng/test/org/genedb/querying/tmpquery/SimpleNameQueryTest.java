package org.genedb.querying.tmpquery;

import static org.junit.Assert.*;

import org.genedb.querying.core.QueryException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext-query.xml"})
public class SimpleNameQueryTest {

    @Autowired
    private SimpleNameQuery simpleNameQuery;


    @SuppressWarnings("unchecked")
    @Test
    public void basic() throws QueryException {

        simpleNameQuery.setSearch("Tb927.1.*");
        List results = simpleNameQuery.getResults();
        assertEquals(522, results.size());
    }

}
