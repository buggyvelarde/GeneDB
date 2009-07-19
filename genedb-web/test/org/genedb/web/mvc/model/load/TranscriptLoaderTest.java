package org.genedb.web.mvc.model.load;


import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import common.Logger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TranscriptLoaderTest {
    Logger logger = Logger.getLogger(TranscriptLoaderTest.class);

    @Autowired
    private TranscriptLoader transcriptLoader;
    
    @Before
    public void setUpLogging() {
        String log4jprops = "/log4j.TranscriptLoader.properties";
        URL url = this.getClass().getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);                
    }
    
    @Test
    public void loadTbruceibrucei427(){
        transcriptLoader.load("Tbruceibrucei427");
    }
}
