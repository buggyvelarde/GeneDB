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
	
	String temp = null;

        List<String> names = MiningUtils.getProperties("gene", an);
        if (names == null) {
            throw new RuntimeException("BacterialNomenclatureHandler: No 'gene' keys to work with");
        }

        if(names.size() > 1)
        {
        	for(int i=0;i<names.size();i++)
        	{
        		temp = names.get(i);
        		if(temp.charAt(0)== 'T' && temp.charAt(1) == 'W')
        		{
        			names.remove(i);
        			ret.setSystematicIdAndTemp(temp, false);
        			 		
        		}
        	}
        	ret.setSynonyms(names);
        }
        else
        {
        	ret.setSystematicIdAndTemp(names.get(0), false);
        }
                    
        return ret;
    }
    
}
