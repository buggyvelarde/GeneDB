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

public class NoteMungingNomenclatureHandler extends BaseNomenclatureHandler implements NomenclatureHandler {
    
    @Override
    public Names findNamesInternal(Annotation an) {
	
	Names ret = new Names();

        List<String> notes = MiningUtils.getProperties("note", an);
        if (notes == null) {
            throw new RuntimeException("NoteMungingNomenclatureHandler: No 'note' keys to work with");
        }
        boolean found = false;
        for (String note : notes) {
			if (note.startsWith("*systematic_id:")) {
				String id = note.substring("*systematic_id:".length()).trim();
				ret.setSystematicIdAndTemp(id, false);
				found = true;
				break;
			}
			if (note.startsWith("*temporary_systematic_id:")) {
				String id = note.substring("*temporary_systematic_id:".length()).trim();
				ret.setSystematicIdAndTemp(id, true);
				found = true;
				break;
			}
		}
        if (!found) {
        	throw new RuntimeException("Unable to find a systematic id");
        }
        return ret;
    }
    
}
