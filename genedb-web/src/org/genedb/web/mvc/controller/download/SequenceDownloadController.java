package org.genedb.web.mvc.controller.download;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("systematic_id", "fred");
		writeEmblEntry(out, tlf, true, 1, 10000, attributes);
		writeEmblEntry(out, "CDS", true, 1, 100, attributes);
		writeEmblEntry(out, "misc_feature", true, 100, 200, attributes);
		writeEmblEntry(out, "wibble", false, 1, 10, attributes);
		writeEmblEntry(out, "fred", false, 1045, 1096, attributes);
		writeEmblEntry(out, "foo", true, 1, 100, attributes);
		
		
		return null;
	}

	private void writeEmblEntry(PrintWriter out, String featureType, 
			boolean strand, int min, int max,
			Map<String, String> qualifiers) {

		out.format("FT "+featureType);
		if (!strand) {
			out.print("complement(");
		}
		
		out.print(min+".."+max);
		
		if (!strand) {
			out.print(")");
		}
		
		for (Map.Entry<String, String> qualifier: qualifiers.entrySet()) {
			out.println("FT       "+qualifier.getKey()+"=\""+qualifier.getValue()+"\"");
		}
		
	}
	
	
	
	
}
