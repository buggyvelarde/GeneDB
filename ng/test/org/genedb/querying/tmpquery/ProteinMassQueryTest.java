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
public class ProteinMassQueryTest {

    @Autowired
    private ProteinMassQuery proteinMassQuery;


    @SuppressWarnings("unchecked")
    @Test
    public void basic() throws QueryException {

        proteinMassQuery.setMin(10);
        proteinMassQuery.setMax(11);
        List results = proteinMassQuery.getResults();
        assertEquals(2542, results.size());
    }

}
