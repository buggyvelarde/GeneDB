package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.web.utils.DownloadUtils;
import org.gmod.schema.feature.AbstractGene;
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
	
	
	@Override
	public void formatBody(List<GeneDetail> entries) throws IOException {
		
		logger.info("starting fasta export");
		
		for (GeneDetail entry : entries) {
			
			StringBuffer header = new StringBuffer(); 
			
			GeneDetailFieldValueExctractor facade = facade(entry);
			
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
			logger.error(headerString);
			
			String sequence = "";
			Feature feature = facade.getFeature();
			logger.error(feature);
			
			if (feature instanceof Transcript) {
				Transcript transcript = (Transcript) feature;
				sequence = DownloadUtils.getSequence(transcript, sequenceType, prime3, prime5);
			} else if (feature instanceof AbstractGene ) {				
				AbstractGene abstractGene = (AbstractGene) feature;
				sequence = DownloadUtils.getSequence(abstractGene, sequenceType);
			}
			
    		String fasta;
            if (sequence != null) {
            	fasta = DownloadUtils.writeFasta(headerString, sequence);
            } else {
            	fasta = String.format(">%s\nAlternately spliced or sequence not attached", headerString);
            }
            
            writer.append(fasta);
            writer.append(postRecordSeparator);
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
