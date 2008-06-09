package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.utils.GeneNameOrganism;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class GenesByCvTermAndCvController extends AbstractController {

	private static final String NO_VALUE_SUPPLIED = "_NO_VALUE_SUPPLIED";

	private SequenceDao sequenceDao;
	private String listResultsView;
	private String genePage;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String cvName = ServletRequestUtils
        .getStringParameter(request, "cvName", NO_VALUE_SUPPLIED);

		String cvTermName = ServletRequestUtils.getStringParameter(request, "cvTermName",
		    NO_VALUE_SUPPLIED);
		String viewName = listResultsView;
		List<GeneNameOrganism> features = sequenceDao.getGeneNameOrganismsByCvTermNameAndCvName(cvTermName, cvName);

		if (features == null || features.size() == 0) {
		    try {
		    	ServletOutputStream out = response.getOutputStream();
		    	out.print("There is no Gene in the database coresponding to CvTerm " + cvTermName);
		        out.close();
		        return null;
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		Map<String,Object> model = new HashMap<String,Object>();
		//List<String> results = new ArrayList<String>();
		/*for (Feature feature : features) {
		    if ("gene".equals(feature.getCvTerm().getName())) {
		    results.add(feature);
		} else {
			The below code gets Gene names from the corresponding polypeptides
			- this isn't the right approach and needs to be changed so that
			something like polypeptide.getGene() can be used either
			Feature mRNA = null;
		    Collection<FeatureRelationship> frs = feature.getFeatureRelationshipsForSubjectId();
		    if (frs != null) {
		        for (FeatureRelationship fr : frs) {
		            if(fr.getCvTerm().getName().equals("derives_from")) {
		            	mRNA = fr.getFeatureByObjectId();
		            	break;
		            }
		        }
		        if (mRNA != null) {
		            Feature gene = null;
		            Collection<FeatureRelationship> frs2 = mRNA
		                    .getFeatureRelationshipsForSubjectId();
		            for (FeatureRelationship fr : frs2) {
		            	if(fr.getCvTerm().getName().equals("part_of")) {
			            	gene = fr.getFeatureByObjectId();
			            	break;
			            }
		             }
	                 if (gene != null) results.add(gene);
		            }
		        }
		    }
		}*/

		if (features.size() == 1) {
			model = GeneDBWebUtils.prepareGene(features.get(0).getGeneName(), model);
			viewName = genePage;
		} else {
			model.put("features", features);
		}
		return new ModelAndView(viewName, model);
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	public void setListResultsView(String listResultsView) {
		this.listResultsView = listResultsView;
	}

	public void setGenePage(String genePage) {
		this.genePage = genePage;
	}
}
