package org.genedb.querying.tmpquery;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.core.HqlQuery;
import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryUtils;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProteinMatch;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.google.common.collect.Lists;

public class ProteinMatchClusterOrthologueQuery extends OrganismHqlQuery {
	
	private static final Logger logger = Logger.getLogger(ProteinMatchClusterOrthologueQuery.class);
	
	@Autowired
	private SequenceDao sequenceDao;
	
	private String clusterName;
	
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
		logger.info("Set cluster name " + clusterName);
	}
	
	public String getClusterName() {
		return clusterName;
	}
	
	@Override
	public int getOrder() {
		return 0;
	}


	@Override
	public void validate(Object arg0, Errors arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getQueryDescription() {
		return "Returns orthologues in a protein match cluster";
	}

	/*
	@Override
	public List getResults() throws QueryException {
		List<GeneSummary> orthologs = Lists.newArrayList();
		
		
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
                String uniqueName = protein.getTranscript().getUniqueName();
                GeneSummary summary = new GeneSummary(uniqueName);
                summary.taxonDisplayName = f.getOrganism().getCommonName();
                
                FeatureLoc fl = f.getFeatureLoc(0, 0);
                summary.topLevelFeatureName = fl.getSourceFeature().getUniqueName();
                summary.displayId = f.getUniqueName();
                summary.product = protein.getProductsAsSeparatedString();
                
                orthologs.add(summary);
                
            }
        }
        
        return orthologs;
	}*/

	@Override
	public String getParseableDescription() {
		return QueryUtils.makeParseableDescription(getQueryName(), getParamNames(), this);
	}

	@Override
	public Map<String, Object> prepareModelData() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("clusterName", clusterName);
		return map;
	}

	@Override
	public boolean isMaxResultsReached() {
		return false;
	}

	@Override
	public String getQueryName() {
		return "Protein Match Orthologues";
	}


	
	public String[] getParamNames() {
		return new String[]{ "clusterName" };
	}

	//queries.add(
//			" select f.uniqueName from FeatureDbXRef fd  " +
//				" inner join fd.feature f " + 
//				" inner join fd.dbXRef dx " +
//				" inner join dx.db d " +
//				" where dx.accession = :dbxref :appendage " +
//				" @ORGANISM@ " +
//				RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES +
//				" order by f.organism, f.uniqueName ");
//	
	@Override
	protected String getHql() {
		return "select @SELECTOR@ from FeatureRelationship fr join fr.subjectFeature f join fr.objectFeature fo where fo.uniqueName=:clusterName " + RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES ;
	}

	
	@Override
	protected String getOrderBy() {
		return null;
	}

	protected void populateQueryWithParams(org.hibernate.Query query) {
        super.populateQueryWithParams(query);
        query.setString("clusterName", clusterName);
    }

}
