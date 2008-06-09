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
import org.gmod.schema.sequence.feature.Gene;
import org.gmod.schema.sequence.feature.Polypeptide;

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
            
            Feature feat =  featRel.getFeatureByObjectId();
            if (feat instanceof Polypeptide) {
                Gene gene = ((Polypeptide)feat).getGene();
                orthologs.put(gene.getUniqueName(), gene.getProductsAsTabSeparatedString());
            }
            if ("protein_match".equals(feat.getCvTerm().getName())) {
                int clusterSize = feat.getFeatureRelationshipsForObjectId().size();
                clusterSizes.put(feat.getUniqueName(), clusterSize);
                logger.info(String.format("cluster name - %s  %d others", feat.getUniqueName(),
                    clusterSize));
            } 
        }
        
        if (clusterSizes.isEmpty() && orthologs.isEmpty())
            return;
        
        PrintWriter out = new PrintWriter(getJspContext().getOut(), true);

        for (Map.Entry<String, Integer> entry : clusterSizes.entrySet()) {
            String name = entry.getKey();
            int size = entry.getValue();
            out.printf("%s <a href=\"Orthologs?cluster=%1$s\">%d others</a>", name,
                        size);
        }
        out.println("<br>");
        boolean first = true;
        for (Map.Entry<String, String> entry : orthologs.entrySet()) {
            if (first) {
                first = false;
                out.println("<table>");
                out.println("<tr>");
                
            }
            String name = entry.getKey();
            String product = entry.getValue();
            
            out.printf("<td> <a href=\"NamedFeature?name=%s\"> %1$s </a></td><td>%s</td>", name, product);
            out.println("</tr>");
        }
        if(!first) {
            out.println("</tr>");
            out.println("</table>");
        }
    }

    public void setPolypeptide(Feature polypeptide) {
        this.polypeptide = polypeptide;
    }
}
