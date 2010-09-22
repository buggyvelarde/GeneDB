package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.Iterator;

import org.genedb.web.mvc.model.TranscriptDTO;
import org.genedb.web.utils.DownloadUtils;
import org.gmod.schema.feature.Transcript;
import org.genedb.db.dao.SequenceDao;

public class FormatFASTA extends FormatBase {
	
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
	public void formatBody(Iterator<TranscriptDTO> transcriptDTOs)
			throws IOException {
		
		while (transcriptDTOs.hasNext()) {
			TranscriptDTO transcriptDTO = transcriptDTOs.next();
			TranscriptDTOAdaptor adaptor = new TranscriptDTOAdaptor(transcriptDTO, fieldInternalSeparator);
			
			StringBuffer header = new StringBuffer(); 
			
			boolean first = true;
			for (OutputOption outputOption : outputOptions) {
				if (! first) {
					header.append(fieldSeparator);
				} else {
					first = false;
				}
				String value = getFieldValue(adaptor, outputOption);
				header.append(value);
			}
			
			String uniqueName = transcriptDTO.getUniqueName();
			Transcript transcript = (Transcript) sequenceDao.getFeatureByUniqueName(uniqueName, Transcript.class);
			
			String sequence = DownloadUtils.getSequence(transcript, sequenceType, prime3, prime5);
    		
    		String entry;
            if (sequence != null) {
                entry = DownloadUtils.writeFasta(header.toString(), sequence);
            } else {
                entry = String.format("%s \n Alternately spliced or sequence not attached ", uniqueName);
            }
            
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
