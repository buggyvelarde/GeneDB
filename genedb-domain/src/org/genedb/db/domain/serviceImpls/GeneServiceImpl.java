package org.genedb.db.domain.serviceImpls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.Gene;
import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.services.GeneService;
import org.genedb.db.domain.services.ProductService;
import org.gmod.schema.analysis.AnalysisFeature;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.FeatureSynonym;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Repository
@Transactional
public class GeneServiceImpl implements GeneService {
	
	private SessionFactory sessionFactory;
	private ProductService productService;

	@SuppressWarnings("unchecked")
	@Transactional
	public Gene findGeneByUniqueName(String name) {
		// TODO Auto-generate
		//return null;

		List<Feature> features = (List<Feature>) sessionFactory.getCurrentSession().createQuery(
		        "from Feature f where f.uniqueName=:name and f.cvTerm.name='gene'")
		        .setString("name", name).list();
        if (features.size() == 0) {
            return null;
        }

		Feature feat = features.get(0);
		
		Gene ret = new Gene();
		
		ret.setSystematicId(feat.getUniqueName());
		
		if (StringUtils.hasText(feat.getName())) {
			ret.setName(feat.getName());
		}
		
		List<String> synonyms = new ArrayList<String>();
		//System.err.println("The number of fcvts for '"+feat.getUniqueName()+"' is '"+fcvts.size()+"'");
		for (FeatureSynonym fs : feat.getFeatureSynonyms()) {
			String type = fs.getSynonym().getCvTerm().getName();
			if (type.equals("synonym")) {
				synonyms.add(fs.getSynonym().getName());
			}
			if (type.equals("reserved_name")) {
				ret.setReservedName(fs.getSynonym().getName());
			}
		}
		ret.setSynonyms(synonyms);
		
		for (FeatureRelationship fr : feat.getFeatureRelationshipsForObjectId()) {
			Feature otherFeat = fr.getFeatureBySubjectId();
			if (otherFeat.getCvTerm().getName().equals("mRNA")) {
				ret.addTranscript(Transcript.makeTranscript(otherFeat));
			}
		}
		if (ret.getTranscripts().size()>1) {
			System.err.println("Multiple transcripts for '"+feat.getUniqueName()+"' not handled yet");
			return null;
		}
		
		Transcript transcript = ret.getTranscripts().get(0);
		Feature protein = transcript.getProtein();
		
		List<String> products = new ArrayList<String>();
		Collection<FeatureCvTerm> fcvts = protein.getFeatureCvTerms();
		//System.err.println("The number of fcvts for '"+feat.getUniqueName()+"' is '"+fcvts.size()+"'");
		for (FeatureCvTerm fcvt : fcvts) {
			if (fcvt.getCvTerm().getCv().getName().equals("genedb_products")) {
				products.add(fcvt.getCvTerm().getName());
			}
		}
		ret.setProducts(products);
		
		List<String> clusters = new ArrayList<String>();
		List<String> orthologues = new ArrayList<String>();
		List<String> paralogues = new ArrayList<String>();
		for (AnalysisFeature af : protein.getAnalysisFeatures()) {
			paralogues.add(af.getAnalysis().getName());
		}
		for (FeatureRelationship fr : protein.getFeatureRelationshipsForObjectId()) {
			Feature otherFeat = fr.getFeatureBySubjectId();
			processOrthoParaClusters(fr, otherFeat, clusters, orthologues, paralogues);
		}
		for (FeatureRelationship fr : protein.getFeatureRelationshipsForSubjectId()) {
			Feature otherFeat = fr.getFeatureByObjectId();
			processOrthoParaClusters(fr, otherFeat, clusters, orthologues, paralogues);
		}
		ret.setOrthologues(orthologues);
		ret.setParalogues(paralogues);
		ret.setClusters(clusters);
		
		ret.setOrganism(feat.getOrganism().getFullName());
		//ret.setOrganism("Not fetched");
		
		return ret;
	}

	private void processOrthoParaClusters(FeatureRelationship fr, Feature otherFeat, List<String> clusters, List<String> orthologues, List<String> paralogues) {
		String type = fr.getCvTerm().getName();
		if (type.equals("orthologous_to")) {
			if (otherFeat.getOrganism().getFullName().equals("dummy")) {
				clusters.add(otherFeat.getUniqueName());
			} else {
				orthologues.add(otherFeat.getUniqueName());
			}
		}
		if (type.equals("paralogous_to")) {
			paralogues.add(otherFeat.getUniqueName());
		}
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List<String> findGeneNamesByPartialName(String search) {
		List<String> names = (List<String>) sessionFactory.getCurrentSession().createQuery(
        "select f.uniqueName from Feature f where f.uniqueName like '%"+search+"%' and f.cvTerm.name='gene'")
        .list();
		if (names.size() == 0) {
			return Collections.<String>emptyList();
		}
		return names;
	}

}
