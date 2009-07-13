package org.genedb.web.mvc.model;

import java.net.URL;

import junit.framework.Assert;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sleepycat.collections.StoredMap;

import common.Logger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DtoDbTest {
    Logger logger = Logger.getLogger(DtoDbTest.class);
    
    @Autowired 
    private DtoDb dtoDb;
    
    @Autowired 
    private BerkeleyMapFactory bmf;
    
    @Before
    public void setUpLogging() {
        String log4jprops = "/log4j.DtoDbTest.properties";
        URL url = this.getClass().getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
        
        
    }
    
    //@Test 
    public void testInserts()throws Exception{
        logger.debug("Started...");
        StoredMap<Integer, TranscriptDTO> dtoMap = bmf.getDtoMap();
        
        int index = 0;
        int updates = 0;
        try{
            
            for (StoredMap.Entry<Integer, TranscriptDTO> entry : dtoMap.entrySet()) {
                
                ++index;                
                TranscriptDTO dto = entry.getValue();
                try{
                    dto.setTranscriptId(entry.getKey());
                    logger.debug("GeneName: "  + dto.getUniqueName());
                    updates = updates + dtoDb.persistDTO(dto);
                }catch(Exception e){
                    logger.error("Failed:\n " + dto.toString() +"\n", e);
                    throw e;
                }
            }
            
            
        }finally{
            logger.debug(String.format("\n%d successful comparisons out of %d", updates, dtoMap.entrySet().size()));
        }
    }
    
    @Test
    public void testCompare()throws Exception{
        logger.debug("Started...");
        StoredMap<Integer, TranscriptDTO> dtoMap = bmf.getDtoMap();
        
        int updates = 0;
        try{
            
            for (StoredMap.Entry<Integer, TranscriptDTO> entry : dtoMap.entrySet()) {
                try{ 
                    TranscriptDTO transcriptDto = dtoDb.retrieveDTO(entry.getKey());                    
                    Assert.assertEquals(transcriptDto, entry.getValue());                    
                }catch(Exception e){
                    logger.error(e);
                    throw e;
                }
            }
            
            
        }finally{
            logger.debug(String.format("\n%d successful updates out of %d insert attempts", updates, dtoMap.entrySet().size()));
        }
    }

    
    //@Test 
    public void findCacheSize(){
        logger.debug("Started...");
        StoredMap<Integer, TranscriptDTO> dtoMap = bmf.getDtoMap();
        
        logger.debug("Pfalciparum Size: " + dtoMap.size());
    }

}
