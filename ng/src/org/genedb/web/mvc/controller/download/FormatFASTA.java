package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.web.utils.DownloadUtils;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

public class FormatFASTA extends FormatBase {
	
	private static Logger logger = Logger.getLogger(FormatFASTA.class);
	
	protected SequenceType sequenceType;
	
	protected int prime3;
	protected int prime5;
	
	public FormatFASTA() {
		super();
		this.postRecordSeparator = "\n";
	}
	
	public void setSequenceType (SequenceType sequenceType) {
		this.sequenceType = sequenceType;
	}
	
	public void setPrime3(int prime3) {
		this.prime3 = prime3;
	}
	
	public void setPrime5(int prime5) {
		this.prime5 = prime5;
	}
	
	/**
	 * FASTA formatting can't rely on lucenes, so this is always false
	 * @see org.genedb.web.mvc.controller.download.FormatBase#onlyNeedLuceneLookups()
	 */
	@Override
	protected boolean onlyNeedLuceneLookups() {
		return false;
	}
	
	/**
	 * FASTA formatting will always require feature objects
	 * @see org.genedb.web.mvc.controller.download.FormatBase#requireFeatures(java.util.List)
	 */
	@Override
	protected boolean requireFeatures(List<GeneDetail> entries) {
		return true;
	}
	
	@Override
	public void formatBody(List<GeneDetail> entries) throws IOException {
		
		if (entries.size() > 0) {
			logger.info("fasta export " + entries.get(0).getDisplayId() + " ...");
		}
		
		for (GeneDetail entry : entries) {
			
			StringBuffer header = new StringBuffer(); 
			
			GeneDetailFieldValueExctractor facade = getExtractor(entry);
			
			boolean first = true;
			for (String fieldValue : getFieldValues(facade, outputOptions)) {
				if (! first) {
					header.append(fieldSeparator);
				} else {
					first = false;
				}
				header.append(fieldValue);
			}
			
			String headerString = header.toString();
			
			String sequence = null;
			
			Feature feature = facade.getFeature();
			
			if (feature instanceof Transcript) {
				sequence = DownloadUtils.getSequence( (Transcript) feature, sequenceType, prime3, prime5);
			} else if (feature instanceof AbstractGene ) {
				sequence = DownloadUtils.getSequence( (AbstractGene) feature, sequenceType, prime3, prime5);
			} else if (feature instanceof Polypeptide ) {
				sequence = DownloadUtils.getSequence( (Polypeptide) feature, sequenceType, prime3, prime5);
			} else {
				logger.error("unexpected class: " + feature.getClass());
			}
			
    		String fasta;
            if (sequence != null) {
            	fasta = DownloadUtils.writeFasta(headerString, sequence);
            } else {
            	fasta = String.format(">%s\nAlternately spliced or sequence not attached", headerString);
            }
            
            writer.append(fasta.trim() + postRecordSeparator);
            
		}
		
	}

	@Override
	public void formatFooter() throws IOException {
		// blank
	}

	@Override
	public void formatHeader() throws IOException {
		// blank
	}

}
