package org.genedb.db.loading;


import org.biojava.bio.Annotation;

import java.util.List;


public class LocusTagNomenclatureHandler extends BaseNomenclatureHandler implements NomenclatureHandler {
    
    @Override
    public Names findNamesInternal(Annotation an) {
	
	Names ret = new Names();

        List<String> names = MiningUtils.getProperties("locus_tag", an);
        if (names.size() == 0) {
            throw new RuntimeException("LocusTagNomenclatureHandler: No 'locus_tag' key to work with");
        }
        if (names.size() > 1) {
            throw new RuntimeException("LocusTagNomenclatureHandler: Too many 'locus_tag' keys to work with");
        }
        
        ret.setSystematicIdAndTemp(names.get(0), false);

        names = MiningUtils.getProperties("gene", an);
        if (names.size() != 0) {
            ret.setSynonyms(names);
        }

        return ret;
    }
    
}
