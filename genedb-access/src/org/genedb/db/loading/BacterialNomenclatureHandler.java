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

    private Pattern pattern;
    
    public BacterialNomenclatureHandler() {
	//pattern = Pattern.compile("SP(A|B|C)(P|C)(\\w+)\\.\\d+c?");
    }
    
    public Names findNamesInternal(Annotation an) {
	
	Names ret = new Names();
	//String geneId = null;
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
        			//geneId = temp;
        			names.remove(i);
        			ret.setSystematicIdAndTemp(temp, false);
        			 		
        		}
        	}
        	ret.setSynonyms(names);
        }
        else
        {
        	//geneId = names.get(0);
        	ret.setSystematicIdAndTemp(names.get(0), false);
        }
        
        
        // Remove duplicates, maintaining order
        //      Set tmp = new LinkedHashSet();
        /*
        Set<String> tmp = new InsertionOrderedSet<String>();
        tmp.addAll(names);
        names.clear();
        names.addAll(tmp);

        // Move all the systematics ids into a new list
        List<String> systematics = new ArrayList<String>();
        Iterator nameIt = names.iterator();
        while (nameIt.hasNext()) {
            String test = (String) nameIt.next();
            // SP(A|B|C)(P|C)$cosmid.num(c?)
            //Matcher matcher = pattern.matcher(test);
            //if (matcher.matches()) {
                systematics.add(test);
                nameIt.remove();
            //}
        }

        // Set the gene name if there is one
        if (names.size() > 0) {
            ret.setPrimary(names.get(0));
            names.remove(0);
        }

        String geneId = null;
        if (systematics.size() == 0) {
            System.err.println("Annotation '"+an+"'");
            throw new RuntimeException("SpombeNomenclatureHandler: No systematic id");
            //System.err.println("WARN: Setting gene id to gene name ie " + gene.getName());
            //geneId = ret.getPrimary();
        }

        if (systematics.size() > 0) {
            // Have more than one systematic name. Find the "alphabetically" first
            Collections.sort(systematics);
            geneId = systematics.get(0);
            systematics.remove(0);
        }*/

        

        //names.addAll(systematics);
        
                
        return ret;
    }
    
}
