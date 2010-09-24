package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.web.utils.DownloadUtils;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.genedb.db.dao.SequenceDao;

public class FormatFASTA extends FormatBase {
	
	private static Logger logger = Logger.getLogger(FormatFASTA.class);
	
	protected SequenceType sequenceType;
	protected SequenceDao sequenceDao;
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
	
	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
	
	
	
	@Override
	public void formatBody(List<Feature> features) throws IOException {
		
		for (Feature feature : features) {
			
			StringBuffer header = new StringBuffer(); 
			
			boolean first = true;
			for (String fieldValue : getFieldValues(feature, outputOptions)) {
				if (! first) {
					header.append(fieldSeparator);
				} else {
					first = false;
				}
				header.append(fieldValue);
			}
			
			String sequence = "";
			
			if (feature instanceof Transcript) {
				Transcript transcript = (Transcript) feature;
				sequence = DownloadUtils.getSequence(transcript, sequenceType, prime3, prime5);
			} else if (feature instanceof AbstractGene ) {				
				AbstractGene abstractGene = (AbstractGene) feature;
				sequence = DownloadUtils.getSequence(abstractGene, sequenceType);
			}
			
			String headerString = header.toString();
    		String entry;
            if (sequence != null) {
                entry = DownloadUtils.writeFasta(headerString, sequence);
            } else {
                entry = String.format(">%s\nAlternately spliced or sequence not attached", headerString);
            }
            
            logger.error(headerString);
            
            writer.append(entry);
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
