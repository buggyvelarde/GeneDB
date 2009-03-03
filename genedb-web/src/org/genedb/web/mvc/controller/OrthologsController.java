package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns all features (orthologs) that belong to a particular cluster
 *
 * @author Chinmay Patel (cp2)
 */

public class OrthologsController extends AbstractController {

    private static final String NO_VALUE_SUPPLIED = "_NO_VALUE_SUPPLIED";

    private SequenceDao sequenceDao;
    private String listResultsView;
    private String genePage;

    public void setGenePage(String genePage) {
        this.genePage = genePage;
    }

    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String clusterName = ServletRequestUtils.getStringParameter(request, "cluster",
            NO_VALUE_SUPPLIED);
        String type = "protein_match";
        String viewName = listResultsView;
        Map<String, Object> model = null;
        List<Feature> orthologs = null;

        if (clusterName.equals(NO_VALUE_SUPPLIED)) {
            // TODO redirect it to an error page
        }
        Feature cluster = sequenceDao.getFeatureByUniqueName(clusterName, type);
        Collection<FeatureRelationship> relations = cluster.getFeatureRelationshipsForObjectId();
        orthologs = new ArrayList<Feature>();
        /*
         * The below code gets Gene names from the corresponding polypeptides
         * this isn't the right approach and needs to be changed so that
         * something like polypeptide.getGene() can be used either
         */
        for (FeatureRelationship featureRel : relations) {
            Feature protein = featureRel.getSubjectFeature();
            Feature mRNA = null;
            Collection<FeatureRelationship> frs = protein.getFeatureRelationshipsForSubjectId();
            if (frs != null) {
                for (FeatureRelationship fr : frs) {
                    if (fr.getType().getName().equals("derives_from")) {
                        mRNA = fr.getObjectFeature();
                        break;
                    }
                }
                if (mRNA != null) {
                    Feature gene = null;
                    Collection<FeatureRelationship> frs2 = mRNA
                            .getFeatureRelationshipsForSubjectId();
                    for (FeatureRelationship fr : frs2) {
                        if (fr.getType().getName().equals("part_of")) {
                            gene = fr.getObjectFeature();
                            break;
                        }
                    }
                    if (gene != null)
                        orthologs.add(gene);
                }
            }
        }
        model = new HashMap<String, Object>();

        switch (orthologs.size()) {
        case 0:
            // TODO return to an error page displaying proper message
        case 1:
            String gene = orthologs.get(0).getUniqueName();
            model.put("name", gene);
            viewName = genePage;
            break;
        default:
            model.put("results", orthologs);
        }
        return new ModelAndView(viewName, model);
    }
}
