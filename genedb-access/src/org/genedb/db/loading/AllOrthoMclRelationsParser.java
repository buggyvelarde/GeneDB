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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;




/**
 * This class is the loads up orthologue data into GeneDB.
 *
 * Usage: OrthologueStorer orthologue_file [orthologue_file ...]
 *
 *
 * @author Adrian Tivey (art)
 */
public class AllOrthoMclRelationsParser implements OrthologueRelationsParser {

    protected static final Log logger = LogFactory.getLog(AllOrthoMclRelationsParser.class);

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

		BufferedReader br = new BufferedReader(r);
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String[] tmp = line.split(":");
				String clusterId = tmp[0];
				String ids = tmp[1];
				clusterId = loseBrackets(clusterId);
				ids = loseBrackets(ids).trim();
				//System.err.println(ids);
				String[] genes = ids.split("\\s+");
				for (String gene : genes) {
					System.err.println(gene);
				}
				clusters.put(clusterId, Arrays.asList(genes));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private String loseBrackets(String in) {
		StringBuilder ret = new StringBuilder();
		int lb;
		int cursor = 0;
		while ((lb = in.indexOf('(', cursor))!= -1) {
			int rb = in.indexOf(')', lb);
			if (rb != -1) {
				ret.append(in.substring(cursor, lb));
				cursor = rb+1;
				lb = -1;
				rb = -1;
			}
		}
		return ret.toString();
	}
    
	
	
}
