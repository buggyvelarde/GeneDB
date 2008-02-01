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

package org.genedb.db.domain.miscscripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.services.ProductService;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



/**
 * This class is the main entry point for the new GeneDB data miners. It's designed to be
 * called from the command-line. It looks for a config. file which specifies which files
 * to process.
 *
 * Usage: NewRunner common_nane [config_file]
 *
 *
 * @author Adrian Tivey (art)
 */
public class ScriptRunner {
	
	private ProductService productService;

    private static String usage="NewRunner commonname [config file]";

    //protected static final Log logger = LogFactory.getLog(ScriptRunner.class);
    

    Class interfaceClazz;
    Class implementationClazz;
    
    public void runScript() throws IOException {
    	// Load up new products
    	File productFile = new File("restore_prod.sort");
    	BufferedReader br = null;
    	Map<String, List<String>> map = new HashMap<String, List<String>>();
    	try {
    		br = new BufferedReader(new FileReader(productFile));
    		String in;
    		while ((in=br.readLine())!=null) {
    			//System.err.println(in);
    			String[] parts = in.split("\\|");
    			parts[0] = parts[0].trim();
    			parts[1] = parts[1].trim();
    			//System.err.println(parts[0]+":"+parts[1]);
    			CollectionUtils.addItemToMultiValuedMap(parts[0], parts[1], map);
    		}
    	}
    	finally {
    		if (br != null) {
    			br.close();
    		}
    	}
    	
		List<Product> tmp = productService.getProductList(false);
		Set<String> products = new HashSet<String>(tmp.size());
		for (Product p : tmp) {
			products.add(p.toString());
		}
		// For each product, find Product
    	for (String productName : map.keySet()) {
			//System.err.println("Looking for '"+productName+"'");
			if (!products.contains(productName)) {
				System.err.println("Failed for '"+productName+"'");
			} else {
				System.err.println("Success for '"+productName+"'");
			}

			
		}
    	// Apply product to each gene
    	
    	
    }
    
    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "classpath:domain-client-applicationContext.xml",
                		"classpath:ScriptRunner.xml" });
		ScriptRunner sr = (ScriptRunner) ctx.getBean("scriptRunner", ScriptRunner.class);
        
        sr.runScript();
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

}
