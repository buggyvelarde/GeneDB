package org.genedb.db.dao;

import java.util.List;

import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.gmod.schema.mapped.Phylonode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext.xml"})
public class PhylogenyDaoTest {
    @Autowired private PhylogenyDao phylogenyDao;
    
    @Test
    public void testGetAllPhylonodes() {
        List<Phylonode> phylonodes = phylogenyDao.getAllPhylonodes();
        assertNotNull(phylonodes);
        assertTrue("Phylonode list is empty", phylonodes.size() > 0);
    }
}