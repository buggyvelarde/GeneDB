//package org.genedb.web.mvc.model;
//
//import java.io.File;
//import java.net.URL;
//
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.Before;
//import org.springframework.beans.factory.annotation.Autowired;
//
//public abstract class TestAbstractUpdater {
//    
//    @Autowired
//    BerkeleyMapFactory berkeleyMapFactory;
//    
//    @Before
//    public void setUpLogging() {
//        String log4jprops = "/log4j.periodicUpdaterTest.properties";
//        URL url = this.getClass().getResource(log4jprops);
//        System.out.printf("Configuring Log4J from '%s'\n", url);
//        PropertyConfigurator.configure(url);
//    }
//    
//    @Before
//    public void setupBerkeleyMapRootFactory(){
//        String rootDir = berkeleyMapFactory.getRootDirectory();
//        File dir = new File(rootDir);
//        if (!dir.exists()){
//            dir.mkdirs();
//        }
//    }
//}
