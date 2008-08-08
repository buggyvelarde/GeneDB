/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.db.loading;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

//import javax.xml.stream.XMLStreamException;



/**
 * This class is the loads up orthologue data into GeneDB.
 *
 * Usage: OrthologueStorer orthologue_file [orthologue_file ...]
 *
 *
 * @author Adrian Tivey (art)
 */
public class OrthoStoreRunner {

    protected static final Log logger = LogFactory.getLog(OrthoStoreRunner.class);



    /**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args organism_common_name, [conf file path]
     * @throws XMLStreamException 
     * @throws FileNotFoundException 
     */
    public static void main (String[] args) throws FileNotFoundException {

        String[] filePaths = args;

        if (filePaths.length == 0) {
            System.err.println("No input files specified");
            System.exit(-1);
        }
        
        // Override properties in Spring config file (using a
        // BeanFactoryPostProcessor) based on command-line args
        Properties overrideProps = new Properties();
        //overrideProps.setProperty("dataSource.username", "chado");
        //overrideProps.setProperty("runner.organismCommonName", organismCommonName);
        //overrideProps.setProperty("runnerConfigParser.organismCommonName", organismCommonName);

//        if (configFilePath != null) {
//            overrideProps.setProperty("runnerConfigParser.configFilePath", configFilePath);
//        }


        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);


        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        OrthologueStorer ostore = (OrthologueStorer) ctx.getBean("ostore", OrthologueStorer.class);
        ostore.afterPropertiesSet();
        File[] files = new File[filePaths.length];
        //long start = new Date().getTime();
        for (int i = 0; i < filePaths.length; i++) {
            files[i] = new File(filePaths[i]);
        }
        ostore.process(files);
    }
    

    

//  public void afterPropertiesSet() {
//      System.err.println("In aps cvDao='"+cvDao+"'");
//        featureUtils = new FeatureUtils();
//        featureUtils.setCvDao(cvDao);
//        featureUtils.setSequenceDao(sequenceDao);
//        featureUtils.setPubDao(pubDao);
//        featureUtils.afterPropertiesSet();
//      System.err.println("In aps cvDao='"+cvDao+"', class is '"+cvDao.getClass()+"'");
//        DUMMY_ORG = organismDao.getOrganismByCommonName("dummy");
//    }
 
}
