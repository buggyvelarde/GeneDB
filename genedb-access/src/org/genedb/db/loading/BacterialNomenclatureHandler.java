package org.genedb.db.loading;


import org.biojava.bio.Annotation;

import java.util.List;

public class BacterialNomenclatureHandler extends BaseNomenclatureHandler implements NomenclatureHandler {

    private String[] prefixes; 
    
    @Override
    public Names findNamesInternal(Annotation an) {

    	Names ret = new Names();
		String tmpPrefix = getOptions().get("prefix");
		this.prefixes = tmpPrefix.split(";");
		String name	=	null;
		int count = 0;
		
		/* get the prefix from the config file and find the gene names accordingly
		 * if more than one gene found with the same prefix give an error message
		 * all the other gene names go as synonyms
		 */
        List<String> names = MiningUtils.getProperties("gene", an);
        if (names == null) {
            throw new RuntimeException("BacterialNomenclatureHandler: No 'gene' keys to work with");
        }

        if(names.size() > 1) {
        	count = 0;
        	for (int i=0;i<names.size();i++) {
        		name = names.get(i);
        		for (int j = 0; j < prefixes.length; j++) {
					String prefix = prefixes[j];
					if(name.startsWith(prefix)) {
						count++;
						if(count>1) {
							throw new RuntimeException("BacterialNomenclatureHandler: More than one gene with same name " + name);
						}
						names.remove(i);
						ret.setSystematicIdAndTemp(name, false);
					}
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
