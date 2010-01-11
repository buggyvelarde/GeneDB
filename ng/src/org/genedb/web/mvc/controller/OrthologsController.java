package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.web.mvc.controller.download.BaseCachingController;

import org.apache.log4j.Logger;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProteinMatch;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;

/**
 * Returns all features (orthologs) that belong to a particular cluster
 *
 * @author Chinmay Patel (cp2)
 */
@Controller
@RequestMapping("/Orthologs")
public class OrthologsController extends BaseCachingController {

    private static final Logger logger = Logger.getLogger(OrthologsController.class);

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

    @RequestMapping(method = RequestMethod.GET , value="/{cluster}")
    public String processForm(
            @PathVariable(value="cluster") String clusterName,
            @RequestParam(value="suppress", required=false) String suppress,
            ServletRequest request,
            HttpSession session,
            Model model) throws QueryException {

        String viewName = listResultsView;

        List<String> orthologs = Lists.newArrayList();

        Feature cluster = sequenceDao.getFeatureByUniqueName(clusterName, ProteinMatch.class);
        if (cluster == null) {
            logger.error(String.format("Unable to find cluster '%s' of type ProteinMatch", clusterName));
        } else {

            Collection<FeatureRelationship> relations = cluster.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship featureRel : relations) {
                Feature f = featureRel.getSubjectFeature();

                if (! (f instanceof Polypeptide)) {
                    logger.error(String.format("Didn't get a polypeptide when I expected one - got '%s'", f.getClass().toString()));
                    continue;
                }

                Polypeptide protein = (Polypeptide) f;
                orthologs.add(protein.getTranscript().getUniqueName());
            }
        }


        switch (orthologs.size()) {
        case 0:
            // TODO return a proper error message
            viewName = "redirect:/Homepage";
            break;
        case 1:
            String gene = orthologs.get(0);
            model.addAttribute("name", gene);
            viewName = genePage;
            break;
        default:
            List<GeneSummary> gs = possiblyConvertList(orthologs);
            String resultsKey = cacheResults(gs, null, null, session.getId());

//            model.addAttribute("key", resultsKey);
//            model.addAttribute("taxonNodeName", taxonName);
            logger.debug("Found results for query (Size: '"+gs.size()+"' key: '"+resultsKey+"')- redirecting to Results controller");
            model.addAttribute("resultsSize", gs.size());
            //viewName = "list/results2";
            return "redirect:/Results/"+resultsKey;
        }
        return viewName;
    }

}
