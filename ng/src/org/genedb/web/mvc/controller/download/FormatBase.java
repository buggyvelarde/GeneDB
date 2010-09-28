package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.web.mvc.model.BerkeleyMapFactory;

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
	
	private SequenceDao sequenceDao;
	
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
    
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
    
    
    public void format(List<GeneDetail> entries) throws IOException {
    	formatHeader();
    	formatBody(entries);
    	formatFooter();
    }
    
	
	abstract public void formatHeader() throws IOException;
	
	abstract public void formatBody(List<GeneDetail> entries) throws IOException;
	
	abstract public void formatFooter() throws IOException;
	
	protected GeneDetailFieldValueExctractor facade(GeneDetail entry) {
		return new GeneDetailFieldValueExctractor(entry, bmf, sequenceDao, fieldInternalSeparator, blankField);
	}
	
	protected List<String> getFieldValues(GeneDetailFieldValueExctractor entry, List<OutputOption> outputOptions) {
		List<String> values = new ArrayList<String>();
		for (OutputOption outputOption : outputOptions) {
			values.add(entry.getFieldValue(outputOption));
		}
		return values;
	}
	

}
