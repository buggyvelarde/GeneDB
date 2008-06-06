package org.genedb.web.mvc.controller.download;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/SequenceDownload")
public class SequenceDownloadController {
	    
	private SequenceDao sequenceDao;
	
	@RequestMapping(method=RequestMethod.GET)
	public String setUpForm() {
		return "err/forminput";
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public String processSubmit(
		@RequestParam("topLevelFeature") String tlf, 
		//BindingResult result, 
		//SessionStatus status,
		NativeWebRequest nwr,
		Writer writer
	) {
		
//		if (result.hasErrors()) {
//			return "err/formtest";
//		}
		
		PrintWriter out;
		if (writer instanceof PrintWriter) {
			out = (PrintWriter) writer;
		} else {
			out = new PrintWriter(writer);
		}
		
		HttpServletResponse hsr = (HttpServletResponse) nwr.getNativeResponse();
		hsr.setContentType("text/plain");
		
		Feature chromosome = sequenceDao.getFeatureByUniqueName(tlf, "linear_double_stranded_DNA_chromosome"); // FIXME - Shouldn't be hard-coded
		if (chromosome == null) {
			out.println("Unable to retrieve feature for '"+tlf+"'");
			return null;
		}
 		for (FeatureLoc fl : chromosome.getFeatureLocsForSrcFeatureId()) {
 			Object ob = fl.getFeatureByFeatureId();
			//out.print(ob.getClass());
			if (ob instanceof Feature) {
				Feature f = (Feature) ob;
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("systematic_id", f.getUniqueName());
				writeEmblEntry(out, f.getCvTerm().getName(), true, fl.getFmin(), fl.getFmax(), attributes);
			}
			out.println();
		}
		
		
		//Map<String, String> attributes = new HashMap<String, String>();
		//attributes.put("systematic_id", "fred");
		//out.println("topLevelFeature="+tlf);
		//writeEmblEntry(out, tlf, true, 1, 10000, attributes);
		//writeEmblEntry(out, "CDS", true, 1, 100, attributes);
		//writeEmblEntry(out, "misc_feature", true, 100, 200, attributes);
		//writeEmblEntry(out, "wibble", false, 1, 10, attributes);
		//writeEmblEntry(out, "fred", false, 1045, 1096, attributes);
		//writeEmblEntry(out, "foo", true, 1, 100, attributes);
		
		
		return null;
	}

	private static String FEATURE_PREFIX_WIDTH = "22";
	private static final String FEATURE_TABLE_PREFIX = String.format("%-"+FEATURE_PREFIX_WIDTH+"s", "FT");
	
	private void writeEmblEntry(PrintWriter out, String featureType, 
			boolean forwardStrand, int min, int max,
			Map<String, String> qualifiers) {

		out.format("FT %-"+FEATURE_PREFIX_WIDTH+"s", featureType);
		if (!forwardStrand) {
			out.print("complement(");
		}
		
		out.println(min+".."+max);
		
		if (!forwardStrand) {
			out.print(")");
		}
		
		for (Map.Entry<String, String> qualifier: qualifiers.entrySet()) {
			out.println(FEATURE_TABLE_PREFIX+qualifier.getKey()+"=\""+qualifier.getValue()+"\"");
		}
		
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
	
	
	
	
}
