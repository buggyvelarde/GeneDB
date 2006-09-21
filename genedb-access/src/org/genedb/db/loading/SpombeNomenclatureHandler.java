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

public class SpombeNomenclatureHandler extends BaseNomenclatureHandler implements NomenclatureHandler {

    private Pattern pattern;
    
    public SpombeNomenclatureHandler() {
	pattern = Pattern.compile("SP(A|B|C)(P|C)(\\w+)\\.\\d+c?");
    }
    
    @Override
    public Names findNamesInternal(Annotation an) {
	
	Names ret = new Names();

        List<String> names = MiningUtils.getProperties("gene", an);
        if (names == null) {
            throw new RuntimeException("SpombeNomenclatureHandler: No 'gene' keys to work with");
        }

        // Remove duplicates, maintaining order
        //      Set tmp = new LinkedHashSet();
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
            Matcher matcher = pattern.matcher(test);
            if (matcher.matches()) {
                systematics.add(test);
                nameIt.remove();
            }
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
        }

        ret.setSystematicIdAndTemp(geneId, false);

        names.addAll(systematics);
        ret.setSynonyms(names);

        List<String> obsolete = new ArrayList<String>();
        List<String> obsolete1 = MiningUtils.getProperties("obsolete_gene_name", an);
        if (obsolete1 != null) {
            obsolete.addAll(obsolete1);
        }
        obsolete1 = MiningUtils.getProperties("obsolete_name", an);
        if ( obsolete1 != null) {
            obsolete.addAll(obsolete1);
        }
        if (obsolete.size() > 0) {
            ret.setObsolete(obsolete);
        }
        

        List<String> synonyms = MiningUtils.getProperties("synonym", an);
        if ( synonyms != null) {
           Set<String> syn = new HashSet<String>();
           syn.addAll(ret.getSynonyms());
           syn.addAll(synonyms);
           ret.setSynonyms(new ArrayList<String>(syn));
        }

        for (String ob : obsolete) {
            if (ret.getSynonyms().contains(ob)) {
        	ret.getSynonyms().remove(ob);
            }
        }
        
        List<String> reserved = MiningUtils.getProperties("reserved_name", an);
        if (reserved != null) {
            ret.setReserved(reserved);
        }

        return ret;
    }
    
}
