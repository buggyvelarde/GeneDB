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
					if (prop.getCvTerm().getName() == "comment") {
						comments.add(prop.getValue());
					}
				}
				JspWriter out = getJspContext().getOut();
				out.println("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\">");
				out.println("<tr>");
				out.println("<th>No.</th>");
				out.println("<th>Comment</th>");
				out.println("</tr>");
				
				int count = 1;
				for (String comment : comments) {
					out.println("<tr>");
					out.println("<td>" + count + "</td>");
					out.println("<td>" + comment + "</td>");
					out.println("</tr>");
					count++;
				}
				
				out.println("</table>");
			}
		}
	}

	public void setPolypeptide(Feature polypeptide) {
		this.polypeptide = polypeptide;
	}
}
