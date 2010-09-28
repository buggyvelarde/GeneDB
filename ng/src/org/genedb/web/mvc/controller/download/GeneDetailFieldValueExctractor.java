package org.genedb.web.mvc.controller.download;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Synonym;
import org.springframework.util.StringUtils;

public class GeneDetailFieldValueExctractor {
	
	private Logger logger = Logger.getLogger(GeneDetailFieldValueExctractor.class);
	
	private String fieldInternalSeparator;
	private String blankField;
	private GeneDetail entry;
	private BerkeleyMapFactory bmf;
	private SequenceDao sequenceDao;
	
	private boolean isTranscript = false;
	private TranscriptDTOAdaptor adaptor;
	private TranscriptDTO dto;
	
	private int featureId;
	private String systematicId;
	
	private Feature feature;
	
	public GeneDetailFieldValueExctractor(GeneDetail entry, BerkeleyMapFactory bmf, SequenceDao sequenceDao, String fieldInternalSeparator, String blankField) {
		
		this.entry = entry;
		this.bmf = bmf;
		this.sequenceDao = sequenceDao;
		
		this.fieldInternalSeparator = fieldInternalSeparator;
		this.blankField = blankField;
		
		featureId = entry.getFeatureId();
		systematicId = entry.getSystematicId();
		
		logger.error(systematicId);
		
		if (entry.getType().equals("mRNA")) {
			isTranscript = true;
		}
	}
	
	public String getFieldValue (OutputOption outputOption) {
		
		// try first to fetch from lucene because it's faster
		String fieldValue = getFieldValue(entry, outputOption);
		
		if (fieldValue == null) {
			if (isTranscript) {
				
				// use the DTO if it's a transcript
				// logger.debug(String.format("looking up %s in transcript, output %s", systematicId, outputOption.name()));
				fieldValue = getFieldValue(getAdaptor(), outputOption);
			} else {
				
				// fall back on feature (for pseudogenes, etc.), slow but no other way left to get the info
				// logger.debug(String.format("looking up %s in feature, output %s", systematicId, outputOption.name()));
				fieldValue = getFieldValue(getFeature(), outputOption);
			}
		} 
		
		fieldValue = (fieldValue == null || fieldValue.equals("")) ? blankField : fieldValue;
		return fieldValue;
	}
	
	
	private String getFieldValue(GeneDetail entry, OutputOption outputOption) {
		String fieldValue = null;
		
		switch (outputOption) {
		case ORGANISM:
			fieldValue = entry.getTaxonDisplayName();
			break;
		case SYS_ID:
			fieldValue = entry.getSystematicId();
			break;
		case PRIMARY_NAME:
			fieldValue = entry.getPrimaryName();
			break;
		case PRODUCT:
			fieldValue = entry.getProduct();
			break;
		case GENE_TYPE:
			fieldValue = entry.getType();
			break;
		case SYNONYMS:
			fieldValue = StringUtils.collectionToDelimitedString(entry.getSynonyms(), fieldInternalSeparator);
			break;
		case PREV_SYS_ID:
			break;
		case CHROMOSOME:
			fieldValue = entry.getTopLevelFeatureName();
			break;
		case LOCATION:
			fieldValue = entry.getLocation();
			break;
		case EC_NUMBERS:
			break;
		case NUM_TM_DOMAINS:
			break;
		case SIG_P:
			break;
		case GPI_ANCHOR:
			break;
		case MOL_WEIGHT:
			break;
		case ISOELECTRIC_POINT:
			break;
		case GO_IDS:
			break;
		case PFAM_IDS:
			break;
		case INTERPRO_IDS:
			break;
		}
		
		return fieldValue;
	}
	
	
	
	/**
	 * Gets field values for transcripts. Because this gets called several times for each transcript, 
	 * and in each case an adaptor is needed, this method takes an adaptor parameter rather than 
	 * the transcript itself, so as to be able to reuse the same adaptor instance. 
	 * @param adaptor
	 * @param outputOption
	 * @return
	 */
	private String getFieldValue(TranscriptDTOAdaptor adaptor, OutputOption outputOption) {
		String fieldValue = null;
		
		if (adaptor != null) {
			
			switch (outputOption) {
			case CHROMOSOME:
				fieldValue =  adaptor.getContig();
				break;
			case EC_NUMBERS:
				fieldValue = adaptor.getEc();
				break;
			case GENE_TYPE:
				fieldValue = adaptor.getType();
				break;
			case GO_IDS:
				fieldValue = adaptor.getGO();
				break;
			case GPI_ANCHOR:
				fieldValue = adaptor.getGpiAnchor();
				break;
			case INTERPRO_IDS:
				fieldValue = adaptor.getInterpro();
				break;
			case ISOELECTRIC_POINT:
				fieldValue = adaptor.getIsoelectricPoint();
				break;
			case LOCATION:
				fieldValue = adaptor.getLocation();
				break;
			case MOL_WEIGHT:
				fieldValue = adaptor.getMolWeight();
				break;
			case NUM_TM_DOMAINS:
				fieldValue = adaptor.getNumTM();
				break;
			case ORGANISM:
				fieldValue = adaptor.getOrganism();
				break;
			case PFAM_IDS:
				fieldValue = adaptor.getPfam();
				break;
			case PREV_SYS_ID:
				fieldValue = adaptor.getPrevIds();
				break;
			case PRIMARY_NAME:
				fieldValue = adaptor.getPrimaryName();
				break;
			case PRODUCT:
				fieldValue = adaptor.getProduct();
				break;
			case SIG_P:
				fieldValue = adaptor.isSigP();
				break;
			case SYNONYMS:
				fieldValue = adaptor.getSynonyms();
				break;
			case SYS_ID:
				fieldValue = adaptor.getId();
				break;
			}
			
		}
		
		return fieldValue;
	}
	
	/**
	 * Gets the field value for any Feature. Used when the feature in question is not a transcript. 
	 * @param feature
	 * @param outputOption
	 * @return
	 */
	private String getFieldValue(Feature feature, OutputOption outputOption) {
		String fieldValue = null;
		
		if (feature != null) {
			
			switch (outputOption) {
			case CHROMOSOME:
				FeatureLoc top = feature.getRankZeroFeatureLoc();
				Feature topLevelFeature = top.getSourceFeature();
				fieldValue = topLevelFeature.getDisplayName();
				break;
			case GENE_TYPE:
				fieldValue = feature.getType().getName();
				break;
			case LOCATION:
				FeatureLoc top2 = feature.getRankZeroFeatureLoc();
				fieldValue = top2.getFmin() + " - " + top2.getFmax() + ( (top2.getStrand() < 0 ) ? " (reverse strand)" : ""  );
				break;
			case ORGANISM:
				fieldValue = feature.getOrganism().getCommonName();
				break;
			case PRIMARY_NAME:
				fieldValue = feature.getUniqueName();
				break;
			case SYNONYMS:
				Collection<Synonym> synonyms = feature.getSynonyms();
				for (Synonym synonym : synonyms) {
					fieldValue += synonym.getName() + fieldInternalSeparator;
				}
				break;
			case SYS_ID:
				fieldValue = feature.getUniqueName();
				break;
			}
			
		}
		
		return fieldValue;
	}
	
	
	
	private TranscriptDTOAdaptor getAdaptor() {
		
		if (dto == null) {
			dto = bmf.getDtoMap().get(featureId);
		}
		
		if (adaptor == null) {
			adaptor = new TranscriptDTOAdaptor(dto, fieldInternalSeparator);
		}
		
		return adaptor;
	}
	
	public Feature getFeature() {
		if (feature == null) {
			feature = sequenceDao.getFeatureByUniqueName(this.systematicId, Feature.class);
		}
		return feature;
	}

}
