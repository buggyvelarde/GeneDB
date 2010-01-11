package org.genedb.web.tags.db;

import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DisplaySimilarity extends SimpleTagSupport {
    private final Logger logger = Logger.getLogger(DisplaySimilarity.class);

    Feature polypeptide;
    Feature transcript;

    @Override
    public void doTag() throws JspException, IOException {
        /*
         * define the variables to hold the values
         *
         */
        List<Feature> similarityFeatures = new ArrayList<Feature>();

        String overlap = null;
        String evalue = null;
        String algorithm = null;
        String id = null;
        String accession = null;
        String database = null;
        String description = null;
        String organism = null;

        /*
         * loop through all the featurelocs associated with polypeptide if any
         * MatchFeature found, store it in the arraylist
         */
        Collection<FeatureLoc> flocs = polypeptide.getFeatureLocsForSrcFeatureId();
        JspWriter out = getJspContext().getOut();

        for (FeatureLoc loc : flocs) {
            if ("protein_match".equals(loc.getFeature().getType().getName())) {
                similarityFeatures.add(loc.getFeature());
            }
        }

        /*
         * loop through all the featurelocs associated with the transcript if
         * any MatchFeature found, store it in the arraylist
         */
        flocs = transcript.getFeatureLocsForSrcFeatureId();
        for (FeatureLoc loc : flocs) {
            if ("nucleotide_match".equals(loc.getFeature().getType().getName())) {
                similarityFeatures.add(loc.getFeature());
            }
        }

        /*
         * if the arraylist contains something loop through the MatchFeatures to
         * find the values required to display on the page
         */
        int count = 1;
        if (similarityFeatures.size() > 0) {
            out.println("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\">");
            out.println("<tr>");
            out.println("<th>Db</th>");
            out.println("<th>Acc</th>");
            out.println("<th>Organism</th>");
            out.println("<th>Description</th>");
            out.println("<th>Value</th>");
            out.println("<th>id</th>");
            out.println("<th>Overlap</th>");
            out.println("<th>Organism</th>");
            out.println("</tr>");
            for (Feature matchFeature : similarityFeatures) {

                for (FeatureProp prop : matchFeature.getFeatureProps()) {
                    if (prop.getType().getName().equals("overlap")) {
                        overlap = prop.getValue();
                    }
                }

                AnalysisFeature aFeature = null;
                for (AnalysisFeature feature : matchFeature.getAnalysisFeatures()) {
                    aFeature = feature;
                    break;
                }
                evalue = aFeature.getSignificance().toString();
                id = aFeature.getIdentity().toString();
                algorithm = aFeature.getAnalysis().getAlgorithm();

                Feature subjectFeature = null;
                for (FeatureLoc floc : matchFeature.getFeatureLocs()) {
                    logger.debug("subjecFeature can be : "
                            + floc.getSourceFeature().getType().getName());
                    if (floc.getSourceFeature().getType().getName().equals(
                        "similarity_region")) {
                        subjectFeature = floc.getSourceFeature();
                    }
                }
                accession = subjectFeature.getDbXRef().getAccession();
                database = subjectFeature.getDbXRef().getDb().getName();

                for (FeatureProp prop : subjectFeature.getFeatureProps()) {
                    if (prop.getType().getName().equals("product")) {
                        description = prop.getValue();
                    } else if (prop.getType().getName().equals("organism")) {
                        organism = prop.getValue();
                    }
                }
                if (count % 2 != 0) {
                    out.println("<tr bgcolor=\"#FAFAD2\">");
                } else {
                    out.println("<tr>");
                }
                count++;
                out.println("<td>" + database + "</td>");
                out.println("<td>" + accession + "</td>");
                out.println("<td>" + organism + "</td>");
                out.println("<td>" + description + "</td>");
                out.println("<td>" + evalue + "</td>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + overlap + "</td>");
                out.println("<td>" + algorithm + "</td>");
                out.println("</tr>");
            }
            out.println("</table>");
            // out.close();
        }

    }

    public void setPolypeptide(Feature polypeptide) {
        this.polypeptide = polypeptide;
    }

    public void setTranscript(Feature transcript) {
        this.transcript = transcript;
    }

}
