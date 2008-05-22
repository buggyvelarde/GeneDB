package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Returns all features (orthologs) that belong to a particular cluster
 * 
 * @author Chinmay Patel (cp2)
 */

public class OrthologsController extends AbstractController{
	
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
		
		String clusterName = ServletRequestUtils.getStringParameter(request, "cluster", NO_VALUE_SUPPLIED);
		String type = "protein_match";
		String viewName = listResultsView;
		Map<String,Object> model = null;
		List<Feature> orthologs = null;
		
		if(clusterName.equals(NO_VALUE_SUPPLIED)) {
			//TODO redirect it to an error page
		} 
		Feature cluster = sequenceDao.getFeatureByUniqueName(clusterName, type);
		Collection<FeatureRelationship> relations = cluster.getFeatureRelationshipsForObjectId();
		orthologs = new ArrayList<Feature>();
		for (FeatureRelationship featureRel : relations) {
			orthologs.add(featureRel.getFeatureBySubjectId());
		}
		model = new HashMap<String,Object>(1);
		
		switch (orthologs.size()) {
		case 0:
			//TODO return to an error page displaying proper message
		case 1:
			String gene = orthologs.get(0).getUniqueName();
			model = GeneDBWebUtils.prepareGene(gene, model);
            viewName = genePage;
            break;
		default:
			model.put("orthologs", orthologs);
		}
		return new ModelAndView(viewName,model);
	}
	
}
