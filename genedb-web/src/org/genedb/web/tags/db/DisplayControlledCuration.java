package org.genedb.web.tags.db;

import org.genedb.web.mvc.controller.WebUtils;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermProp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DisplayControlledCuration extends SimpleTagSupport{
	
	Feature polypeptide;
	
	@Override
    public void doTag() throws JspException, IOException {
		if (polypeptide != null) {
			Collection<FeatureCvTerm> listFeatCvTerm = polypeptide.getFeatureCvTerms();	
			System.out.println("featureCvTerm Collection size is " + listFeatCvTerm.size());
			JspWriter out = getJspContext().getOut();
			int totalRows = 0;
			List<FeatureCvTerm> controlledCuration = new ArrayList<FeatureCvTerm>();
			List<HashMap> featureProps = new ArrayList<HashMap>();
			List<Integer> qualifiers = new ArrayList<Integer>();
			List<Integer> otherGenes = new ArrayList<Integer>();
			for (FeatureCvTerm featCvTerm : listFeatCvTerm) {
				if("CC_genedb_controlledcuration".equals(featCvTerm.getCvTerm().getCv().getName())){
					System.out.println(featCvTerm.getCvTerm().getName());
					controlledCuration.add(totalRows, featCvTerm);
					int totalQualifier = 0;
					HashMap<Integer,FeatureCvTermProp> props = new HashMap<Integer,FeatureCvTermProp>();
					for(FeatureCvTermProp featCvTermProp : featCvTerm.getFeatureCvTermProps()){
						System.out.println("prop value is : " + featCvTermProp.getValue());
						if("qualifier".equals(featCvTermProp.getCvTerm().getName())){
							props.put(1 + totalQualifier,featCvTermProp);
							System.out.println("count " + totalQualifier + " value " + featCvTermProp.getValue());
							totalQualifier++;
						}
					
					}
					qualifiers.add(totalRows, totalQualifier);
					featureProps.add(totalRows, props);
				
					//List<Feature> temp = sequenceDao.getFeaturesByCvTermName(featCvTerm.getCvTerm().getName());
					//otherGenes.add(totalRows,temp.size());
					totalRows++;
				}
			}
			/*
			 * now loop again to display the CC in a table if totalRows is > 0
			 */
			if (totalRows > 0){
				out.println("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\">");
				out.println("<tr>");
				out.println("<th>Term</th>");
				out.println("<th>DbXRef/URL</th>");
				out.println("<th>Qualifier</th>");
				out.println("<th>Other genes annatoted to this term</th>");
				out.println("</tr>");
				System.out.println("total rows is " + totalRows);
				for (int i=0;i<totalRows;i++) {
					if(i%2 == 0){
						out.println("<tr bgcolor=\"#FAFAD2\">");
					} else {
						out.println("<tr>");
					}
					FeatureCvTerm fct = controlledCuration.get(i);
					int qualifier = qualifiers.get(i);
					String startTag = null;
					String endTag = "</td>";
					if(qualifier > 1){
						startTag = "<td colspan=\"1\" rowspan=\"" + qualifier + "\" style=\"vertical-align: top;\">"; 
					} else {
						startTag = "<td>";
					}
					out.println(startTag + fct.getCvTerm().getName() + endTag);
					HashMap fcp = featureProps.get(i);
				
	
					String pub = fct.getPub().getUniqueName();
					if("NULL".equals(pub)){
						if(fct.getFeatureCvTermDbXRefs() == null || fct.getFeatureCvTermDbXRefs().size() == 0){
							out.println(startTag + endTag);
						} else {
							String acc = fct.getFeatureCvTermDbXRefs().iterator().next().getDbXRef().getAccession();
							out.println(startTag + acc + endTag);
						}
					} else {
						String[] sections = pub.split(":");
						out.println(startTag + "<a href=\"http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&dopt=Abstract&list_uids=" + sections[1] + "\">" + pub + "</a>" + endTag);
					}
					if(fcp.containsKey(1)){
						out.println("<td>" + ((FeatureCvTermProp) fcp.get(1)).getValue() + endTag);
					} else {
						out.println(startTag + " " + endTag);
					}
					//System.out.println(sequenceDao.toString());
					int size;
					size = WebUtils.featureListSize(fct.getCvTerm().getName(),"CC_genedb_controlledcuration");
					if(size > 1) {
						size = size - 1;
						out.println(startTag + "<a href=\"http://holly.internal.sanger.ac.uk:8080/genedb-web/Search/FeatureByCvTermNameAndCvName?name=" + fct.getCvTerm().getName() + "&cvName=" + fct.getCvTerm().getCv().getName() + "\">" + "(" + size + " others)" + "</a>" + endTag );
					} else {
						out.println(startTag + "( 0 others )" + endTag);
					}
					out.println("</tr>");
					System.out.println("Qualifier is : " + qualifier);
					if(qualifier > 1){
						int count = 1;
					
						while(qualifier  > count){
							int num = 1 + count;
							System.out.println("number is : " + num);
							if(i%2 == 0){
								out.println("<tr bgcolor=\"#FAFAD2\">");
							} else {
								out.println("<tr>");
							}
							out.println("<td>" + ((FeatureCvTermProp) fcp.get(num)).getValue() + endTag);
							out.println("</tr>");
							count++;
						}
					}
				}
				out.println("</table>");
			}
		}
	}

	public void setPolypeptide(Feature polypeptide) {
		this.polypeptide = polypeptide;
	}


}
