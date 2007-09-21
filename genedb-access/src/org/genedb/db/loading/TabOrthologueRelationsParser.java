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
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.analysis.Analysis;
import org.gmod.schema.analysis.AnalysisFeature;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.PairwiseOverlayRenderer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
public class TabOrthologueRelationsParser implements OrthologueRelationsParser {

    protected static final Log logger = LogFactory.getLog(TabOrthologueRelationsParser.class);

	/**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args organism_common_name, [conf file path]
     * @throws XMLStreamException 
     * @throws FileNotFoundException 
     */
//    public static void main (String[] args) throws FileNotFoundException, XMLStreamException {
//
//        String[] filePaths = args;
//
//        if (filePaths.length == 0) {
//        	System.err.println("No input files specified");
//        	System.exit(-1);
//        }
//        
//        // Override properties in Spring config file (using a
//        // BeanFactoryPostProcessor) based on command-line args
//        Properties overrideProps = new Properties();
//        overrideProps.setProperty("dataSource.username", "chado");
//        //overrideProps.setProperty("runner.organismCommonName", organismCommonName);
//        //overrideProps.setProperty("runnerConfigParser.organismCommonName", organismCommonName);
//
////        if (configFilePath != null) {
////            overrideProps.setProperty("runnerConfigParser.configFilePath", configFilePath);
////        }
//
//
//        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);
//
//
//        ApplicationContext ctx = new ClassPathXmlApplicationContext(
//                new String[] {"NewRunner.xml"});
//
//        XmlOrthologueRelationsParser ostore = (XmlOrthologueRelationsParser) ctx.getBean("ostore", XmlOrthologueRelationsParser.class);
//        ostore.afterPropertiesSet();
//        long start = new Date().getTime();
//        for (int i = 0; i < filePaths.length; i++) {
//			File input = new File(filePaths[i]);
//	        ostore.afterPropertiesSet();
//			ostore.process(input);
//		}
//        ostore.writeToDb();
////      long duration = (new Date().getTime()-start)/1000;
////      logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
//    }


	public void parseInput(final Reader r, Set<GenePair> orthologues, Set<GenePair> paralogues, 
			Map<String,List<String>> clusters)  {

		//System.err.println("Processing '"+input.getName()+"'");
    	// Read in data from file
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(r);
			int event = parser.next();
			while (event != XMLStreamConstants.END_DOCUMENT) {
				if (event == XMLStreamConstants.START_ELEMENT) {
					if ("orthologue".equals(parser.getLocalName())) {
						processAlogue(parser, orthologues, "orthologue");
					} else {
						if ("paralogue".equals(parser.getLocalName())) {
							processAlogue(parser, paralogues, "paralogue");
						} else {
							if ("cluster".equals(parser.getLocalName())) {
								processCluster(parser, clusters);
							}
						}
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
    
    private void processCluster(XMLStreamReader parser, Map<String,List<String>> clusters) throws XMLStreamException {
    		String name = findIdFromAttribute(parser);
        	List<String> ids = getChildren(parser, "cluster");
        	for (String id : ids) {
            	CollectionUtils.addItemToMultiValuedMap(name, id, clusters);
			}
    	}
    
    private void processAlogue(XMLStreamReader parser, Set<GenePair> set, String element) throws XMLStreamException {
    		String name = findIdFromAttribute(parser);
    		//Feature gene = sequenceDao.getFeatureByUniqueName(name, "gene");
        	//event = parser.next();
        	List<String> ids = getChildren(parser, element);
        	for (String id : ids) {
				GenePair pair = new GenePair(name, id);
				set.add(pair);
			}
    	}


	private List<String> getChildren(XMLStreamReader parser, String element) throws XMLStreamException {
		List<String> ret = new ArrayList<String>();
		int event = parser.next();
		while (!(event == XMLStreamConstants.END_ELEMENT && parser.getLocalName().equals(element))) {
			if (event == XMLStreamConstants.START_ELEMENT) {
				ret.add(findIdFromAttribute(parser));
			}
			event = parser.next();
		}
		return ret;
	}




	private String findIdFromAttribute(XMLStreamReader parser) {
		if (!parser.getAttributeLocalName(0).equals("id")) {
			throw new RuntimeException("Found an attribute, but not called id");
		}
		String name = parser.getAttributeValue(0);
		return name;
	}

//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
    
}
