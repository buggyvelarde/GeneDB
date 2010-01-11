/**
 * 
 */
package org.genedb.db.domain.hibernateImpls;


import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.services.BasicGeneService;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author rh11
 *
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BasicGeneServiceImplTest {
    private BasicGeneService basicGeneService;
    @Required @Autowired
    public void setBasicGeneService(BasicGeneService basicGeneService) {
        this.basicGeneService = basicGeneService;
    }

    @Test
    public void serviceExists() {
        assertNotNull(basicGeneService);
    }
    
    @Test
    public void findGeneByUniqueNameReturnsSomething () {
        CompoundLocatedFeature gene = basicGeneService.findGeneByUniqueName("PFB0770c");
        assertNotNull(gene);
    }

}
