package org.genedb.web.tags.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.directwebremoting.util.Logger;
import org.genedb.web.mvc.controller.GeneUtils;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureRelationship;

/**
 * Displays orthologous (cluster and individual) for a particular gene on gene
 * page
 * 
 * @author Chinmay Patel (cp2)
 */
public class DisplayOrthologues extends SimpleTagSupport {

    private static final Logger logger = Logger.getLogger(DisplayOrthologues.class);
    Feature polypeptide;

    @Override
    public void doTag() throws JspException, IOException {
        if (polypeptide == null)
            return;

        Map<String, Integer> clusterSizes = new HashMap<String, Integer>();
        Map<String, String> orthologs = new HashMap<String, String>();
        Collection<FeatureRelationship> featureRels = polypeptide
                .getFeatureRelationshipsForSubjectId();
        for (FeatureRelationship featRel : featureRels) {
            if (!"orthologous_to".equals(featRel.getCvTerm().getName()))
                continue;
            
            Feature feat = featRel.getFeatureByObjectId();
            if ("protein_match".equals(feat.getCvTerm().getName())) {
                int clusterSize = feat.getFeatureRelationshipsForObjectId().size();
                clusterSizes.put(feat.getUniqueName(), clusterSize);
                logger.info(String.format("cluster name - %s  %d others", feat.getUniqueName(),
                    clusterSize));
            } else
                for (FeatureCvTerm featureCvt : feat.getFeatureCvTerms()) {
                    CvTerm cvTerm = featureCvt.getCvTerm();
                    if (!"genedb_products".equals(cvTerm.getCv().getName()))
                        continue;
                    String product = cvTerm.getName();
                    /*
                     * The below line gets Gene name from the
                     * corresponding polypeptide - this can
                     * be changed so that
                     * something like polypeptide.getGene() can be used
                     * either
                     */
                    Feature gene = GeneUtils.getGeneFromPart(feat);
                    orthologs.put(gene.getUniqueName(), product);
                }
        }
        
        if (clusterSizes.isEmpty() && orthologs.isEmpty())
            return;
        
        PrintWriter out = new PrintWriter(getJspContext().getOut(), true);
        out.println("<ul style=\"display: block; text-align: left;\">");
        for (Map.Entry<String, Integer> entry : clusterSizes.entrySet()) {
            String name = entry.getKey();
            int size = entry.getValue();
            out.printf("<li> %s <a href=\"Orthologs?cluster=%1$s\">%d others</a>", name,
                        size);
        }
        for (Map.Entry<String, String> entry : orthologs.entrySet()) {
            String name = entry.getKey();
            String product = entry.getValue();
            out.printf("<li> <a href=\"NamedFeature?name=%s\"> %1$s </a>   %s", name, product);
        }
        out.println("</ul>");
    }

    public void setPolypeptide(Feature polypeptide) {
        this.polypeptide = polypeptide;
    }
}
