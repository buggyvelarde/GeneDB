package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.gmod.schema.mapped.Feature;

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
    
    private Map<String, Feature> features;
    
    private void getFeatures(List<String> uniqueNames) {
		features = new HashMap<String, Feature>();
		List<Feature> allFeatures = sequenceDao.getFeaturesByUniqueNames(uniqueNames);    		
		for (Feature feature : allFeatures) {
			String uniqueName = feature.getUniqueName();
			features.put(uniqueName, feature);
		}
    }
    
    /**
     * Checks to see if all the options set for this formatter can be run only using Lucene.
     * 
     */
    protected boolean onlyNeedLuceneLookups() {
    	boolean available = true;
    	for (OutputOption outputOption : outputOptions) {
    		if (! GeneDetailFieldValueExctractor.availableFromLucene(outputOption)) {
    			available = false;
    		}
    	}
    	return available;
    }
    
    /**
     * Any set of entries that contains something other than mRNA requires features.
     * @param entries
     * @return
     */
    protected boolean requireFeatures(List<GeneDetail> entries) {
    	for (GeneDetail entry : entries) {
    		if (! entry.getType().equals("mRNA")) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Formats the results, with headers and footers. Pages through the entries for chunking Feature lookups.
     * 
     * @param entries
     * @throws IOException
     */
    public void format(List<GeneDetail> entries) throws IOException {
    	formatHeader();
    	
    	final int max = entries.size() -1;
    	int start = 0;
    	int window = 1000;
    	int stop = start + window;
    	
    	boolean allOptionsAvailableFromLucene = onlyNeedLuceneLookups();
    	
    	while (start <= max) {
    		
    		if (stop > max) {
    			stop = max;
    		}
    		
    		if (start > max) {
    			break;
    		}
    		
    		List<GeneDetail> currentEntries = entries.subList(start, stop);
    		
    		if (! allOptionsAvailableFromLucene) {
    			if (requireFeatures(currentEntries)) {
        			
        			List<String> uniqueNames = new ArrayList<String>();
            		
            		for (GeneDetail detail : currentEntries) {
            			uniqueNames.add(detail.getSystematicId());
            		}
            		
            		getFeatures(uniqueNames);
            		
        		} else {
        			// make sure we reset the features map... (no need to carry a live instance of it when it's no longer needed)
        			features = null;
        		}
    		}
    		
    		
    		
    		formatBody(currentEntries);
    		
    		start += window;
    		stop = start + window;
    	}
    	
    	formatFooter();
    }
    
	
	abstract public void formatHeader() throws IOException;
	
	abstract public void formatBody(List<GeneDetail> entries) throws IOException;
	
	abstract public void formatFooter() throws IOException;
	
	protected GeneDetailFieldValueExctractor getExtractor(GeneDetail entry) {
		return new GeneDetailFieldValueExctractor(entry, bmf, sequenceDao, features, fieldInternalSeparator, blankField);
	}
	
	protected List<String> getFieldValues(GeneDetailFieldValueExctractor extractor, List<OutputOption> outputOptions) {
		List<String> values = new ArrayList<String>();
		for (OutputOption outputOption : outputOptions) {
			values.add(extractor.getFieldValue(outputOption));
		}
		return values;
	}
	

}
