package org.genedb.web.tags.db;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.directwebremoting.util.Logger;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureRelationship;

/**
 * Displays orthologous (cluster and individual) for a particular gene on gene page
 * 
 * @author Chinmay Patel (cp2)
 */
public class DisplayOrthologues extends SimpleTagSupport{
	
	private static final Logger logger = Logger.getLogger(DisplayOrthologues.class);
	Feature polypeptide;
	
	public void doTag() throws JspException, IOException {
		if (polypeptide != null) {
			Map<String,String> clusters = new HashMap<String,String>();
			Map<String,String> orthologs = new HashMap<String,String>();
			Collection<FeatureRelationship> featureRels = polypeptide.getFeatureRelationshipsForSubjectId();
			for (FeatureRelationship featRel : featureRels) {
				if("orthologous_to".equals(featRel.getCvTerm().getName())) {
					Feature feat = featRel.getFeatureByObjectId();
					if("protein_match".equals(feat.getCvTerm().getName())) {
						int cluster = 0;
						cluster = feat.getFeatureRelationshipsForObjectId().size();
						clusters.put(feat.getUniqueName(), String.valueOf(cluster));
						logger.info(String.format("cluster name - %s  %d others", feat.getUniqueName(),cluster));
					} else {
						String name = feat.getUniqueName();
						Collection<FeatureCvTerm> featCVTerms = feat.getFeatureCvTerms();
						for (FeatureCvTerm featureCvt : featCVTerms) {
							CvTerm cvTerm = featureCvt.getCvTerm();
							if ("genedb_products".equals(cvTerm.getCv().getName())) {
								String product = cvTerm.getName();
								orthologs.put(name, product);
							}
						}
					}
				}
			}
			JspWriter out = getJspContext().getOut();
			out.println("<ul style=\"display: block;text-align: left;\">");
			for (Iterator iter = clusters.entrySet().iterator(); iter.hasNext();)
			{ 
			    Map.Entry<String,String> entry = (Map.Entry<String,String>)iter.next();
			    String name = (String)entry.getKey();
			    String size = (String)entry.getValue();
			    out.println(String.format("<li> %s <a href=\"./Orthologs?cluster=%s\"> %d others </a>", name,name,size));
			}
			out.println("</ul>");
			out.println("<ul style=\"display: block;text-align: left;\">");
			for (Iterator iter = orthologs.entrySet().iterator(); iter.hasNext();)
			{ 
			    Map.Entry<String,String> entry = (Map.Entry<String,String>)iter.next();
			    String name = (String)entry.getKey();
			    String product = (String)entry.getValue();
			    out.println(String.format("<li> <a href=\"./NamedFeature?name=%s\"> %s </a> %s", name,name,product));
			}
			out.println("</ul>");
		}
	}

	public void setPolypeptide(Feature polypeptide) {
		this.polypeptide = polypeptide;
	}
}
