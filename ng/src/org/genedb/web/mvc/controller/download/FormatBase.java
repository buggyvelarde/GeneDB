package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.genedb.web.mvc.model.TranscriptDTO;

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
	
	public void format(Iterator<TranscriptDTO> transcriptDTOs) throws IOException {
		formatHeader();
		formatBody(transcriptDTOs);
		formatFooter();
	}
	
	abstract public void formatHeader() throws IOException;
	
	abstract public void formatBody(Iterator<TranscriptDTO> transcriptDTOs) throws IOException;
	
	abstract public void formatFooter() throws IOException;
	
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
