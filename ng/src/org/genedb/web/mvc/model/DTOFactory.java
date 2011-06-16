package org.genedb.web.mvc.model;

import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.DatabasePolypeptideRegion;
import org.genedb.db.domain.objects.InterProHit;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.db.domain.objects.SimpleRegionGroup;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.ImageCreationException;
import org.genedb.web.gui.ImageMapSummary;
import org.genedb.web.gui.ProteinMapDiagram;
import org.genedb.web.gui.RenderedDiagramFactory;
import org.genedb.web.gui.RenderedProteinMap;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.NcRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.TransmembraneRegion;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeaturePub;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DTOFactory {

	private transient Logger logger = Logger
			.getLogger(DTOFactory.class);
	
	private SequenceDao sequenceDao;
	
	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	private Map<String, Object> prepareAlgorithmData(Polypeptide polypeptide) {
		Map<String, Object> aData = new HashMap<String, Object>();
		putIfNotEmpty(aData, "SignalP", prepareSignalPData(polypeptide));
		putIfNotEmpty(aData, "DGPI", prepareDGPIData(polypeptide));
		putIfNotEmpty(aData, "PlasmoAP", preparePlasmoAPData(polypeptide));
		putIfNotEmpty(aData, "TMHMM", prepareTMHMMData(polypeptide));
		return aData;
	}

	private Map<String, Object> prepareSignalPData(Polypeptide polypeptide) {
		Map<String, Object> signalPData = new HashMap<String, Object>();

		String prediction = polypeptide.getProperty("genedb_misc",
				"SignalP_prediction");
		String peptideProb = polypeptide.getProperty("genedb_misc",
				"signal_peptide_probability");
		String anchorProb = polypeptide.getProperty("genedb_misc",
				"signal_anchor_probability");

		putIfNotNull(signalPData, "prediction", prediction);
		putIfNotNull(signalPData, "peptideProb", peptideProb);
		putIfNotNull(signalPData, "anchorProb", anchorProb);

		Collection<SignalPeptide> signalPeptides = polypeptide
				.getRegions(SignalPeptide.class);
		if (!signalPeptides.isEmpty()) {
			if (signalPeptides.size() > 1) {
				logger.error(String
						.format("Polypeptide '%s' has %d signal peptide regions; only expected one",
								polypeptide.getUniqueName(),
								signalPeptides.size()));
			}
			SignalPeptide signalPeptide = signalPeptides.iterator().next();
			FeatureLoc signalPeptideLoc = signalPeptide.getRankZeroFeatureLoc();

			signalPData.put("cleavageSite", signalPeptideLoc.getFmax());
			signalPData.put("cleavageSiteProb", signalPeptide.getProbability());
		}
		return signalPData;
	}

	private Map<String, Object> prepareDGPIData(Polypeptide polypeptide) {
		/*
		 * If the GPI_anchored property is not present, we do not add the
		 * predicted cleavage site, even if there is one.
		 */
		if (!polypeptide.hasProperty("genedb_misc", "GPI_anchored")) {
			return Collections.emptyMap();
		}

		Map<String, Object> dgpiData = new HashMap<String, Object>();
		dgpiData.put("anchored", true);

		Collection<GPIAnchorCleavageSite> cleavageSites = polypeptide
				.getRegions(GPIAnchorCleavageSite.class);
		if (!cleavageSites.isEmpty()) {
			if (cleavageSites.size() > 1) {
				logger.error(String
						.format("There are %d GPI anchor cleavage sites on polypeptide '%s'; only expected one",
								cleavageSites.size(),
								polypeptide.getUniqueName()));
			}
			GPIAnchorCleavageSite cleavageSite = cleavageSites.iterator()
					.next();
			FeatureLoc cleavageSiteLoc = cleavageSite.getRankZeroFeatureLoc();
			dgpiData.put("location", cleavageSiteLoc.getFmax());
			dgpiData.put("score", cleavageSite.getScore());
		}

		return dgpiData;
	}

	private Map<String, Object> preparePlasmoAPData(Polypeptide polypeptide) {
		Map<String, Object> plasmoAPData = new HashMap<String, Object>();
		String score = polypeptide.getProperty("genedb_misc", "PlasmoAP_score");
		if (score != null) {
			plasmoAPData.put("score", score);
			switch (Integer.parseInt(score)) {
			case 0:
			case 1:
			case 2:
				plasmoAPData.put("description", "Unlikely");
				break;
			case 3:
				plasmoAPData.put("description", "Unknown");
				break;
			case 4:
				plasmoAPData.put("description", "Likely");
				break;
			case 5:
				plasmoAPData.put("description", "Very likely");
				break;
			default:
				throw new RuntimeException(
						String.format(
								"Polypeptide '%s' has unrecognised PlasmoAP score '%s'",
								polypeptide.getUniqueName(), score));
			}
		}

		return plasmoAPData;
	}

	private List<String> prepareTMHMMData(Polypeptide polypeptide) {
		List<String> tmhmmData = new ArrayList<String>();

		for (TransmembraneRegion transmembraneRegion : polypeptide
				.getRegions(TransmembraneRegion.class)) {
			tmhmmData.add(String.format("%d-%d", 1 + transmembraneRegion
					.getRankZeroFeatureLoc().getFmin(), transmembraneRegion
					.getRankZeroFeatureLoc().getFmax()));
		}

		return tmhmmData;
	}

	private <S> void putIfNotEmpty(Map<S, ? super Collection<?>> map, S key,
			Collection<?> value) {
		if (!value.isEmpty()) {
			map.put(key, value);
		}
	}

	private <S, T> void putIfNotNull(Map<S, T> map, S key, T value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	private <S> void putIfNotEmpty(Map<S, ? super Map<?, ?>> map, S key,
			Map<?, ?> value) {
		if (!value.isEmpty()) {
			map.put(key, value);
		}
	}
	
	
	
//	public TranscriptDTO make(Feature feature) {
//		logger.info("Populating feature without any trawling...");
//		TranscriptDTO ret = new TranscriptDTO();
//		ret.setProteinCoding(false);
//		populateNames(ret, feature);
//		populateUsingFeature(ret, feature);
//		return ret;
//	}
	
	public TranscriptDTO make(Transcript transcript) {
		
		TranscriptDTO ret = new TranscriptDTO();
		AbstractGene gene = sequenceDao.getGene(transcript);
		
		populateUsingGene(ret, gene);
		
		Polypeptide polypeptide = transcript.getPolypeptide();
		if (polypeptide != null) {
			logger.info("Populating polypeptide... " + polypeptide.getClass() + " : " + polypeptide.getUniqueName());
			populateUsingFeature(ret, polypeptide);
			populateUsingPolypeptide(ret, polypeptide);
		} else {
			logger.info("Populating transcript... " + transcript.getClass() + " : " + transcript.getUniqueName());
			populateUsingFeature(ret, transcript);
		}
		
		// bodge to make sure we always use the transcript name as key
		ret.setUniqueName(transcript.getUniqueName());
		
		return ret;
	}
	
	public TranscriptDTO make(AbstractGene gene) {
		logger.info("Populating gene... " + gene.getClass() + " : " + gene.getUniqueName());
		if (gene.getNonObsoleteTranscripts().size() > 0) {
			return make(gene.getFirstTranscript());
		}
		return make((Feature)gene);
	}
	
	public TranscriptDTO make(Feature feature) {
		
		TranscriptDTO ret = new TranscriptDTO();
		logger.info("Populating feature... " + feature.getClass() + " : " + feature.getUniqueName());
		
		AbstractGene gene = sequenceDao.getGene(feature);
		
		populateUsingGene(ret, gene);
		populateUsingFeature(ret, feature);
		
		return ret;
	}
	
//	public TranscriptDTO make(AbstractGene gene) {
//		TranscriptDTO ret = new TranscriptDTO();
//		
//		populateNames(ret, gene);
//		populateUsingGene(ret, gene);
//		
//		// if there is a transcript, return its details
//		if (gene.getTranscripts().size() > 0) {
//			for (Transcript transcript : gene.getTranscripts()) {
//				logger.info("Populating gene using transcript");
//				Polypeptide polypeptide = transcript.getPolypeptide();
//				if (polypeptide != null) {
//					logger.info("Populating transcript using polypeptide");
//					populateUsingFeature(ret, polypeptide);
//					populateUsingPolypeptide(ret, polypeptide);
//				} else {
//					logger.info("Populating transcript using transcript");
//					populateUsingFeature(ret, transcript);
//				}
//				
//				// we only do this for the first transcript
//				break;
//			}
//		} else {
//			populateUsingFeature(ret, gene);
//		}
//		
//		return ret;
//	}

//	public TranscriptDTO make(Transcript transcript, AbstractGene gene) {
//		TranscriptDTO ret = new TranscriptDTO();
//		
//		populateNames(ret, gene);
//		populateUsingGene(ret, transcript, gene);
//		
//		Polypeptide polypeptide = transcript.getPolypeptide();
//		if (polypeptide != null) {
//			logger.info("Populating transcript using polypeptide");
//			populateUsingFeature(ret, polypeptide);
//			populateUsingPolypeptide(ret, polypeptide);
//		} else {
//			logger.info("Populating transcript using transcript");
//			populateUsingFeature(ret, transcript);
//		}
//		
//		populateUsingFeature(ret, transcript);
//		
//		logger.info("returning ?" +ret);
//		
//		return ret;
//	}
//		
//	public TranscriptDTO make(Polypeptide polypeptide, AbstractGene gene) {
//		TranscriptDTO ret = new PolypeptideDTO();
//		populateNames(ret, gene);
//		populateUsingGene(ret, polypeptide, gene);
//		populateUsingFeature(ret, polypeptide);
//		populateUsingPolypeptide (ret, polypeptide);
//		
//		return ret;
//	}
	
	
	private void populateUsingGene(TranscriptDTO ret, AbstractGene gene) {
		if (gene == null) {
			return;
		}
		
		String geneName = gene.getName();
		if (geneName == null || geneName == "") {
			geneName = gene.getUniqueName();
		}
		ret.setGeneName(geneName);
		
		if (gene.getNonObsoleteTranscripts().size()>1) {
            ret.setAnAlternateTranscript(true);
        }
		
	}
	
	private void populateUsingPolypeptide(TranscriptDTO ret, Polypeptide polypeptide) {
		logger.info("** About to set algorithm data");
		ret.setAlgorithmData(prepareAlgorithmData(polypeptide));
		logger.info("** About to set p. props");
		ret.setPolypeptideProperties(polypeptide.calculateStats());
		logger.info("** About to set domain info");
		ret.setDomainInformation(prepareDomainInformation(polypeptide));
		ret.setProteinCoding(true);
	}
	
	private void populateUsingFeature(TranscriptDTO ret, Feature feature) {
		
		ret.setUniqueName(feature.getUniqueName());
		
		populateOrganismDetails(ret, feature);
		populateParentDetails(ret, feature);
		
		populateType(ret, feature);
		
		logger.info("** About to set from feature props");
		populateFromFeatureProps(ret, feature);

		logger.info("** About to set from products");
		ret.setProducts(populateFromFeatureCvTermsIncludingCount(feature,
				"genedb_products"));
		logger.info("** About to set from CC");
		ret.setControlledCurations(populateFromFeatureCvTerms(feature,
				"CC_"));
		logger.info("** About to set from bio. process");
		ret.setGoBiologicalProcesses(populateFromFeatureCvTerms(feature,
				"biological_process"));
		logger.info("** About to set from mol. function");
		ret.setGoMolecularFunctions(populateFromFeatureCvTerms(feature,
				"molecular_function"));
		logger.info("** About to set from cell. component");
		ret.setGoCellularComponents(populateFromFeatureCvTerms(feature,
				"cellular_component"));
		logger.info("** About to set from feature dbxref");
		populateFromFeatureDbXrefs(ret, feature);

		logger.info("** About to set feature rels");
		populateFromFeatureRelationships(ret, feature);

		logger.info("** About to set feature pubs");
		populateFromFeaturePubs(ret, feature);
		
		populateLastModified(ret, feature);
		
		ret.setTranscriptSynonymsByTypes(findFromSynonymsByType(feature.getFeatureSynonyms()));
		
	}
	
	

	

	private void populateLastModified(TranscriptDTO ret,
			Feature feature) {
		Timestamp date = feature.getTimeLastModified();

		if (date != null) {
			ret.setLastModified(date.getTime());
		}
	}

	private void populateOrganismDetails(TranscriptDTO ret,
			Feature feature) {
		ret.setOrganismCommonName(feature.getOrganism().getCommonName());
		ret.setOrganismHtmlShortName(feature.getOrganism()
				.getHtmlShortName());
	}

	private List<PolypeptideRegionGroup> prepareDomainInformation(
			Polypeptide polypeptide) {

		Map<DbXRef, InterProHit> interProHitsByDbXRef = Maps.newHashMap();
		SimpleRegionGroup otherMatches = new SimpleRegionGroup("Other matches",
				"Other");

		for (PolypeptideDomain domain : polypeptide
				.getRegions(PolypeptideDomain.class)) {

			DatabasePolypeptideRegion thisHit = DatabasePolypeptideRegion
					.build(domain);

			if (thisHit == null) {
				continue;
			}

			DbXRef interProDbXRef = domain.getInterProDbXRef();
			Hibernate.initialize(interProDbXRef);
			if (interProDbXRef == null) {
				otherMatches.addRegion(thisHit);
			} else {
				if (!interProHitsByDbXRef.containsKey(interProDbXRef)) {
					interProHitsByDbXRef.put(interProDbXRef, new InterProHit(
							interProDbXRef));
				}
				interProHitsByDbXRef.get(interProDbXRef).addRegion(thisHit);
			}
		}

		List<PolypeptideRegionGroup> domainInfo = new ArrayList<PolypeptideRegionGroup>(
				interProHitsByDbXRef.values());

		if (!otherMatches.isEmpty()) {
			domainInfo.add(otherMatches);
		}

		return domainInfo;
	}

	private void populateFromFeatureRelationships(TranscriptDTO transcriptDTO,
			Feature polypeptide) {

		List<String> clusterIds = Lists.newArrayList();
		List<String> orthologueNames = Lists.newArrayList();
		List<FeatureRelationship> filtered = polypeptide
				.getFeatureRelationshipsForSubjectIdFilteredByCvNameAndTermName(
						"sequence", "orthologous_to");

		for (FeatureRelationship featureRelationship : filtered) {
			Feature f = featureRelationship.getObjectFeature();
			if (f.getType().getName().equals("protein_match")) {
				clusterIds.add(f.getUniqueName());
			} else {
				if (f.getType().getName().equals("polypeptide")) {
					orthologueNames.add(f.getUniqueName());
				}
			}
		}

		transcriptDTO.setClusterIds(clusterIds);
		transcriptDTO.setOrthologueNames(orthologueNames);

	}

	private void populateFromFeatureDbXrefs(TranscriptDTO transcriptDTO,
			Feature polypeptide) {
		List<DbXRefDTO> dbXRefDTOs = new ArrayList<DbXRefDTO>();
		for (FeatureDbXRef fdx : polypeptide.getFeatureDbXRefs()) {
			dbXRefDTOs.add(new DbXRefDTO(fdx.getDbXRef().getDb().getName(), fdx
					.getDbXRef().getAccession(), fdx.getDbXRef().getDb()
					.getUrlPrefix()));
		}

		// Special case for EC numbers which are stored as featureprops
		for (FeatureProp featureProp : polypeptide
				.getFeaturePropsFilteredByCvNameAndTermName("genedb_misc",
						"EC_number")) {
			dbXRefDTOs.add(new DbXRefDTO("EC", featureProp.getValue(),
					"/DbLinkRedirect/EC/"));
		}

		if (dbXRefDTOs.size() > 0) {
			transcriptDTO.setDbXRefDTOs(dbXRefDTOs);
		}
	}

	private void populateFromFeaturePubs(TranscriptDTO transcriptDTO,
			Feature feature) {
		List<String> pubNames = new ArrayList<String>();
		for (FeaturePub fp : feature.getFeaturePubs()) {
			String name = fp.getPub().getUniqueName();
			if (name.startsWith("PMID")) {
				pubNames.add(name);
			} else {
				logger.warn(String.format(
						"Got a pub that isn't a PMID ('%s') for '%s'", name,
						feature.getUniqueName()));
			}
		}
		if (pubNames.size() > 0) {
			transcriptDTO.setPublications(pubNames);
		}
	}
//	
//	private Map<String, FeatureCvTerm> terms (Feature feature) {
//		Assert.notNull(feature);
//		
//		
//	}
//	
//	private List<FeatureCvTermDTO> populateFromFeatureCvTerms(
//			Feature feature, 
//			Map<String, FeatureCvTerm> terms, 
//			String cvNamePrefix) {
//		
//		Assert.notNull(feature);
//		
//		Organism org = feature.getOrganism();
//		List<FeatureCvTermDTO> dtos = new ArrayList<FeatureCvTermDTO>();
//		for (String key : terms.keySet()) {
//			if (key.startsWith(cvNamePrefix)){
//				FeatureCvTerm featureCvTerm = terms.get(key);
//				FeatureCvTermDTO fctd = new FeatureCvTermDTO(featureCvTerm);
//				fctd.setCount(sequenceDao.getFeatureCvTermCountInOrganism(
//						featureCvTerm.getCvTerm().getName(), org));
//				dtos.add(fctd);	
//			}
//			
//		}
//		if (dtos.size() > 0) {
//			return dtos;
//		}
//		return null;
//	}
	
	private List<FeatureCvTermDTO> populateFromFeatureCvTerms(Feature feature,
			String cvNamePrefix) {
		Assert.notNull(feature);

		//Organism org = feature.getOrganism();
		List<FeatureCvTermDTO> dtos = new ArrayList<FeatureCvTermDTO>();
		for (FeatureCvTerm featureCvTerm : feature
				.getFeatureCvTermsFilteredByCvNameStartsWith(cvNamePrefix)) {
			FeatureCvTermDTO fctd = new FeatureCvTermDTO(featureCvTerm);
//			fctd.setCount(sequenceDao.getFeatureCvTermCountInOrganism(
//					featureCvTerm.getCvTerm().getName(), org));
			dtos.add(fctd);
		}
		if (dtos.size() > 0) {
			return dtos;
		}
		return null;
	}
	
	private List<FeatureCvTermDTO> populateFromFeatureCvTermsIncludingCount(Feature feature,
			String cvNamePrefix) {
		Assert.notNull(feature);

		Organism org = feature.getOrganism();
		List<FeatureCvTermDTO> dtos = new ArrayList<FeatureCvTermDTO>();
		
		
		
		for (FeatureCvTerm featureCvTerm : feature
				.getFeatureCvTermsFilteredByCvNameStartsWith(cvNamePrefix)) {
			FeatureCvTermDTO fctd = new FeatureCvTermDTO(featureCvTerm);
			
			Long count = sequenceDao.getFeatureCvTermCountInOrganism(fctd.getTypeName(), org);
			logger.warn(cvNamePrefix + "! " + fctd.getTypeName()  + " - count : " + count);
			
			
			fctd.setCount(count);
			dtos.add(fctd);
		}
		if (dtos.size() > 0) {
			return dtos;
		}
		return null;
	}
	
	private void populateType(TranscriptDTO ret, Feature feature) {
		String type = feature.getType().getName();
		
		if (feature instanceof Polypeptide) {
			type = "Protein coding gene";
		} else if (type.contains("pseudo")) {
			type = "Pseudogene";
		}
		
		ret.setTypeDescription(type);
	}

	
//	private void populateMisc(TranscriptDTO ret, Transcript transcript) {
//		String type = transcript.getType().getName();
//		if ("mRNA".equals(type)) {
//			type = "Protein coding gene";
//		} else {
//			if ("pseudogenic_transcript".equals(type)) {
//				type = "Pseudogene";
//			}
//		}
//		ret.setTypeDescription(type);
//	}

	private void populateParentDetails(TranscriptDTO ret, Feature gene) {
		FeatureLoc top = gene.getRankZeroFeatureLoc();
		ret.setMin(top.getFmin());
		ret.setMax(top.getFmax());
		ret.setStrand((top.getStrand()) != null ? top.getStrand() : 0);

		Feature topLevelFeature = top.getSourceFeature();
		ret.setTopLevelFeatureType(topLevelFeature.getType().getName());
		ret.setTopLevelFeatureDisplayName(topLevelFeature.getDisplayName());
		ret.setTopLevelFeatureUniqueName(topLevelFeature.getUniqueName());
		ret.setTopLevelFeatureLength(topLevelFeature.getSeqLen());
	}

	private void populateFromFeatureProps(TranscriptDTO ret, Feature feature) {
		Assert.notNull(feature);
		ret.setNotes(stringListFromFeaturePropList(feature, "feature_property",
				"comment"));
		ret.setComments(stringListFromFeaturePropList(feature, "genedb_misc",
				"curation"));

		// Set the selenoprotein flag if it has the right property
		String fp = feature.getFeatureProp("sequence",
				"stop_codon_redefined_as_selenocysteine");
		if (fp != null) {
			ret.setSelenoprotein(true);
		}

	}

	private List<String> stringListFromFeaturePropList(Feature feature,
			String cvName, String cvTermName) {
		List<String> ret = new ArrayList<String>();
		List<FeatureProp> featurePropNotes = feature
				.getFeaturePropsFilteredByCvNameAndTermName(cvName, cvTermName);
		for (FeatureProp featureProp : featurePropNotes) {
			ret.add(featureProp.getValue());
		}
		logger.info(String.format(
				"Got '%d' results for filtering featureprops for '%s' in '%s'",
				ret.size(), cvTermName, cvName));
		if (ret.size() > 0) {
			return ret;
		}
		return Collections.emptyList();
	}

//	private void populateNames(TranscriptDTO ret, Feature feature) {
//		
//		String uniqueName = feature.getUniqueName();
//		ret.setUniqueName(uniqueName);
//		
//		Collection<FeatureSynonym> featureSynonyms = feature.getFeatureSynonyms();
//		// Get the map of lists of synonyms
//		ret.setTranscriptSynonymsByTypes(findFromSynonymsByType(featureSynonyms));
//		
////		String uniqueName = transcript.getUniqueName();
////		ret.setUniqueName(uniqueName);
////
////		String geneName = gene.getName();
////		if (geneName == null || geneName == "") {
////			geneName = gene.getUniqueName();
////		}
////		ret.setGeneName(geneName);
////
////		if (transcript.getName() != null
////				&& !transcript.getName().equals(uniqueName)) {
////			ret.setProperName(transcript.getName());
////		}
////		Collection<FeatureSynonym> featureSynonyms = transcript
////				.getFeatureSynonyms();
////		// Get the map of lists of synonyms
////		ret.setTranscriptSynonymsByTypes(findFromSynonymsByType(featureSynonyms));
////
////		featureSynonyms = gene.getFeatureSynonyms();
////		// Get the map of lists of synonyms
////		ret.setGeneSynonymsByTypes(findFromSynonymsByType(featureSynonyms));
////
////		if (transcript instanceof ProductiveTranscript) {
////
////			Polypeptide polypeptide = ((ProductiveTranscript) transcript)
////					.getProtein();
////
////			// trying to avoid a NullPointerException
////			if (polypeptide != null) {
////				featureSynonyms = polypeptide.getFeatureSynonyms();
////
////				// Get the map of lists of synonyms
////				ret.setProteinSynonymsByTypes(findFromSynonymsByType(featureSynonyms));
////			}
////
////		}
//	}

	/**
	 * Create lists of synonyms, grouped by the synonym type. (Only current
	 * synonyms are included in the lists.)
	 * 
	 * @param synonyms
	 * @return a map from the type to a list of synonyms
	 */
	private Map<String, List<String>> findFromSynonymsByType(
			Collection<FeatureSynonym> synonymCollection) {
		HashMap<String, List<String>> synonymsByType = new HashMap<String, List<String>>();
		for (FeatureSynonym featSynonym : synonymCollection) {
			if (!featSynonym.isCurrent()) {
				continue;
			}
			Synonym synonym = featSynonym.getSynonym();
			String typeName = formatSynonymTypeName(synonym.getType().getName());
			List<String> filtered = synonymsByType.get(typeName);
			if (filtered == null) {
				filtered = new ArrayList<String>();
				synonymsByType.put(typeName, filtered);
			}
			filtered.add(synonym.getName());
		}

		if (synonymsByType.size() > 0) {
			return synonymsByType;
		}
		return null;
	}

	/**
	 * Re-format the synonym type name
	 * 
	 * @param rawName
	 * @return
	 */
	private String formatSynonymTypeName(String rawName) {

		char formattedName[] = rawName.toCharArray();
		for (int i = 0; i < formattedName.length; ++i) {

			// Replace underscores with spaces
			if (formattedName[i] == '_') {
				formattedName[i] = ' ';

				// Replace first char lowercase to a uppercase char
			} else if (i == 0 && Character.isLowerCase(formattedName[i])) {
				formattedName[i] = Character.toUpperCase(formattedName[i]);

				// Replace any occurrence of a lowercase char preceeded a space
				// with a upper case char
			} else if (i > 0 && formattedName[i - 1] == ' '
					&& Character.isLowerCase(formattedName[i])) {
				formattedName[i] = Character.toUpperCase(formattedName[i]);
			}
		}
		return String.valueOf(formattedName).trim();
	}

}
