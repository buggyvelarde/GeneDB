package org.genedb.web.tags.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureProp;

public class DisplayComment extends SimpleTagSupport {
	
	Feature polypeptide;
	
	@Override
    public void doTag() throws JspException, IOException {
		if (polypeptide != null) {
			Collection<FeatureProp> fprops = null;
			fprops = polypeptide.getFeatureProps();
			
			if (fprops != null) {
				List<String> comments = new ArrayList<String>();
				
				// Need to check CV
				for (FeatureProp prop : fprops) {
					if (prop.getCvTerm().getName().equals("comment")) {
						comments.add(prop.getValue());
					}
				}
				if(comments.size() > 0) {
	 				JspWriter out = getJspContext().getOut();
					out.println("<ul style=\"display: block;text-align: left;\">");
					for (String comment : comments) {
						out.println("<li>" + comment + "</li>");
					}
					
					out.println("</ul>");
				}
			}
		}
	}

	public void setPolypeptide(Feature polypeptide) {
		this.polypeptide = polypeptide;
	}
}
