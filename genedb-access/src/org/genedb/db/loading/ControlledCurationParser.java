package org.genedb.db.loading;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.Annotation;

public class ControlledCurationParser {

	public List<ControlledCurationInstance> getAllControlledCurationFromAnnotation(Annotation an) {
		List<ControlledCurationInstance> ret = new ArrayList<ControlledCurationInstance>();
		
		List<String> terms = MiningUtils.getProperties("controlled_curation", an);
		
		if (terms == null || terms.size() == 0) {
			return null;
		}
		for (String term : terms) {
			ControlledCurationInstance cc = new ControlledCurationInstance();
			
			String sections[] = term.split(";");
			for (int i=0;i<sections.length;i++) {
				String expression = sections[i].trim();
				int index = expression.indexOf("=");
			    if ( index == -1) {
				System.err.println("WARN: Got naked expression in controlled_curation term:" + term
					+ ": (maybe need qualifier=?). Please fix");
				continue;
			    }
			    String key = expression.substring(0,index).trim();
			    String value = expression.substring(index+1).trim();
			    
			    if("term".equals(key)) {
			    	cc.setTerm(value);
			    }
			    
			    if("db_xref".equals(key)) {
			    	cc.setDbXRef(value);
			    }
			    
			    if("date".equals(key)) {
			    	cc.setDate(value);
			    }
			    
			    if("cv".equals(key)) {
			    	cc.setCv(value);
			    }
			    
			    if("qualifier".equals(key)) {
			    	cc.setQualifier(value);
			    }
			    
			    if("evidence".equals(key)) {
			    	cc.setEvidence(value);
			    }
			    
			    if("attribution".equals(key)) {
			    	cc.setAttribution(value);
			    }
			}
			ret.add(cc);
		}
		return ret;
	}
}
