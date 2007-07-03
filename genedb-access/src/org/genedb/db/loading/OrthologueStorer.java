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

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;



/**
 * This class is the loads up orthologue data into GeneDB.
 *
 * Usage: OrthologueStorer orthologue_file [orthologue_file ...]
 *
 *
 * @author Adrian Tivey (art)
 */
public class OrthologueStorer {

    private static String usage="OrthologueStorer orthologue_file";

    protected static final Log logger = LogFactory.getLog(OrthologueStorer.class);

	private static CvTerm PARALOGOUS_RELATIONSHIP;

	private static CvTerm ORTHOLOGOUS_RELATIONSHIP;

//    private FeatureHandler featureHandler;
//
//    private RunnerConfig runnerConfig;
//
//    private RunnerConfigParser runnerConfigParser;
//
//    private Set<String> noInstance = new HashSet<String>();
//
//    private FeatureUtils featureUtils;
//
//    private Organism organism;
//
//    private ApplicationContext applicationContext;
//
    private SequenceDao sequenceDao;
//
//    private OrganismDao organismDao;
//
    private CvDao cvDao;
//    
//    private PubDao pubDao;
//
//    private GeneralDao generalDao;
//    
    private HibernateTransactionManager hibernateTransactionManager;
//
//    Map<String,String> cdsQualifiers = new HashMap<String,String>();
//    
//	private Set<String> handeledQualifiers = new HashSet<String>();
//    
//    private OrthologueStorage orthologueStorage = new OrthologueStorage();
    
    
    public void setHibernateTransactionManager(
			HibernateTransactionManager hibernateTransactionManager) {
		this.hibernateTransactionManager = hibernateTransactionManager;
	}




    /**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args organism_common_name, [conf file path]
     * @throws XMLStreamException 
     * @throws FileNotFoundException 
     */
    public static void main (String[] args) throws FileNotFoundException, XMLStreamException {

        String[] filePaths = args;

        if (filePaths.length == 0) {
        	System.err.println("No input files specified");
        	System.exit(-1);
        }
        
        // Override properties in Spring config file (using a
        // BeanFactoryPostProcessor) based on command-line args
        Properties overrideProps = new Properties();
        //overrideProps.setProperty("dataSource.username", loginName);
        //overrideProps.setProperty("runner.organismCommonName", organismCommonName);
        //overrideProps.setProperty("runnerConfigParser.organismCommonName", organismCommonName);

//        if (configFilePath != null) {
//            overrideProps.setProperty("runnerConfigParser.configFilePath", configFilePath);
//        }


        //PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);


        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"OrthologueStorer.xml"});

        OrthologueStorer ostore = (OrthologueStorer) ctx.getBean("ostore", OrthologueStorer.class);

        for (int i = 0; i < filePaths.length; i++) {
			File input = new File(filePaths[i]);
			ostore.process(input);
		}
    }

    private void process(final File input) throws FileNotFoundException, XMLStreamException {
    	long start = new Date().getTime();
    	        
		if (checkOrgs(input)) {
          TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
          tt.execute(
        		  new TransactionCallbackWithoutResult() {
        			  @Override
        			  public void doInTransactionWithoutResult(TransactionStatus status) {
        				  storeOrthologues(input);
        			  }
        		  });

		}
        long duration = (new Date().getTime()-start)/1000;
        logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
    }
    
    private boolean checkOrgs(File input) {
    	return true; // FIXME Should go through orgs to check all loaded
    }
    
    private void storeOrthologues(File input) {
    	InputStream in = null;
		try {
			in = new FileInputStream(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(in);
			int event = parser.next();
			while (event != XMLStreamConstants.END_DOCUMENT) {
				if (event == XMLStreamConstants.START_ELEMENT) {
					if (parser.getLocalName().equals("gene")) {
						processGene(parser);
					}
				}
				event = parser.next();
			}
		}
		catch (XMLStreamException exp) {
			exp.printStackTrace();
			System.exit(-1);
		}
    }
    
    private void processGene(XMLStreamReader parser) throws XMLStreamException {
    	int event = parser.next();
    	if (event != XMLStreamConstants.ATTRIBUTE) {
    		// Problem FIXME
    	} else {
    		String name = findNameFromAttribute(parser);
    		Feature gene = sequenceDao.getFeatureByUniqueName(name, "gene");
        	event = parser.next();
        	while (event == XMLStreamConstants.START_ELEMENT) {
        		if (parser.getLocalName().equals("curated_orthologue")) {
        			processCuratedOrthologue(parser, gene);
        		}
        		if (parser.getLocalName().equals("paralogue")) {
        			processParalogue(parser, gene);
        		}
        		if (parser.getLocalName().equals("cluster")) {
        			processCluster(parser, gene);
        		}
        	}
    	}
    }




	private String findNameFromAttribute(XMLStreamReader parser) {
		if (!parser.getAttributeLocalName(0).equals("name")) {
			// Problem FIXME
		}
		String name = parser.getAttributeValue(0);
		return name;
	}
    
    
    private void processCluster(XMLStreamReader parser, Feature gene) {
		String clusterName = findNameFromAttribute(parser);
		
	}


	private void processAlogue(XMLStreamReader parser, Feature gene, CvTerm relationship) {
		String name = findNameFromAttribute(parser);
		Feature otherGene = sequenceDao.getFeatureByUniqueName(name, "gene");
		// TODO Check that relationship doesn't already exist in other direction
		FeatureRelationship fr = new FeatureRelationship(gene, otherGene, relationship, 0);
//		addGeneToCluster("paralogues", otherGene);
	}

	private void processParalogue(XMLStreamReader parser, Feature gene) {
		processAlogue(parser, gene, PARALOGOUS_RELATIONSHIP);
	}




	private void processCuratedOrthologue(XMLStreamReader parser, Feature gene) {
		processAlogue(parser, gene, ORTHOLOGOUS_RELATIONSHIP);
	}




//    public void setOrganismDao(OrganismDao organismDao) {
//        this.organismDao = organismDao;
//    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }
//
//    public void setGeneralDao(GeneralDao generalDao) {
//        this.generalDao = generalDao;
//    }
//
//	public void setPubDao(PubDao pubDao) {
//		this.pubDao = pubDao;
//	}

//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
    
}
