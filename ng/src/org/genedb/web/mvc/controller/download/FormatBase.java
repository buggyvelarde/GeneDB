package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Synonym;

/**
 * 
 * A base for formatter class collating common properties.
 * 
 * @author gv1
 */
public abstract class FormatBase {
	
	protected String fieldSeparator = "";
	protected String fieldInternalSeparator = "";
	protected String recordSeparator = "";
	
	protected String postFieldSeparator = "";
	protected String postFieldInternalSeparator = "";
	protected String postRecordSeparator = "";
	
	protected String headerContentStart = "";
	protected String footerContentStart = "";
	
	protected String blankField = "";
	
	protected List<OutputOption> outputOptions = new ArrayList<OutputOption>();
	
	protected boolean header;
	
	protected Writer writer;
	
	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}
	
	public void setPostFieldSeparator(String postFieldSeparator) {
		this.postFieldSeparator = postFieldSeparator;
	}
	
	public void setFieldInternalSeparator(String fieldInternalSeparator) {
		this.fieldInternalSeparator = fieldInternalSeparator;
	}
	
	public void setPostFieldInternalSeparator(String postFieldInternalSeparator) {
		this.postFieldInternalSeparator = postFieldInternalSeparator;
	}
	
	public void setBlankField(String blankField) {
		this.blankField = blankField;
	}
	
	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}
	
	public void setPostRecordSeparator(String postRecordSeparator) {
		this.postRecordSeparator = postRecordSeparator;
	}
	
	public void setOutputOptions(List<OutputOption> outputOptions) {
		this.outputOptions = outputOptions;
	}
	
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void setHeader(boolean header) {
		this.header = header;
	}
	
	public void setHeaderContentStart(String headerContentStart) {
		this.headerContentStart = headerContentStart;
	}
	
	public void setFooterContentStart(String footerContentStart) {
		this.footerContentStart = footerContentStart;
	}
	
	private BerkeleyMapFactory bmf;
    
    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }
	
    /**
     * Formats a list of features.
     * @param features
     * @throws IOException
     */
	public void format(List<Feature> features) throws IOException {
		formatHeader();
		formatBody(features);
		formatFooter();
	}
	
	abstract public void formatHeader() throws IOException;
	
	abstract public void formatBody(List<Feature> features) throws IOException;
	
	abstract public void formatFooter() throws IOException;
	
	/**
	 * Returns a list of field values for a particular feature and output options. 
	 * @param feature
	 * @param outputOptions
	 * @return
	 */
	protected List<String> getFieldValues(Feature feature, List<OutputOption> outputOptions) {
		
		List<String> values = new ArrayList<String>();
		
		if (feature instanceof Transcript) {
			
			int id = feature.getFeatureId();
			TranscriptDTO dto = bmf.getDtoMap().get(id);
			TranscriptDTOAdaptor adaptor = new TranscriptDTOAdaptor(dto, fieldInternalSeparator);
			
			for (OutputOption outputOption : outputOptions) {
				values.add(this.getFieldValue(adaptor, outputOption));
			}
			
		} else {
			
			for (OutputOption outputOption : outputOptions) {
				values.add(getFieldValue(feature, outputOption));
			}
			
		}
		
		return values;
	}
	
	
	
	/**
	 * Gets the field value for any Feature. Used when the feature in question is not a transcript. 
	 * @param feature
	 * @param outputOption
	 * @return
	 */
	protected String getFieldValue(Feature feature, OutputOption outputOption) {
		String fieldValue = null;
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
			fieldValue = top2.getFmin() + " - " + top2.getFmax(); 
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
		
		fieldValue = (fieldValue == null || fieldValue.equals("")) ? this.blankField : fieldValue;
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
	protected String getFieldValue(TranscriptDTOAdaptor adaptor, OutputOption outputOption) {
		String fieldValue = null;
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
		
		fieldValue = (fieldValue == null || fieldValue.equals("")) ? this.blankField : fieldValue;
		
		return fieldValue;
	}
	
}
