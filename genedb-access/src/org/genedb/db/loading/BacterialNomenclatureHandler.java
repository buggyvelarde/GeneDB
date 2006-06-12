package org.genedb.db.loading;


import org.biojava.bio.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BacterialNomenclatureHandler extends BaseNomenclatureHandler implements NomenclatureHandler {

       
    public BacterialNomenclatureHandler() {
	
    }
    
    public Names findNamesInternal(Annotation an) {
	
    	Names ret = new Names();
	   	String prefix = null;
		String name	=	null;
		int count = 0;
		
		/* get the prefix from the config file and find the gene names accordingly
		 * if more than one gene found with the same prefix give an error message
		 * all the other gene names go as synonyms
		 */
		prefix = getOptions().get("prefix");
        List<String> names = MiningUtils.getProperties("gene", an);
        if (names == null) {
            throw new RuntimeException("BacterialNomenclatureHandler: No 'gene' keys to work with");
        }

        if(names.size() > 1) {
        	count = 0;
        	for (int i=0;i<names.size();i++) {
        		name = names.get(i);
				if(name.startsWith(prefix)) {
					count++;
					if(count>1) {
		        		throw new RuntimeException("BacterialNomenclatureHandler: More than one gene with same name " + name);
		        	}
					names.remove(i);
        			ret.setSystematicIdAndTemp(name, false);
				}
			}
        	
        	ret.setSynonyms(names);
        }
        else {
        	ret.setSystematicIdAndTemp(names.get(0), false);
        }
                    
        return ret;
    }
    
}
