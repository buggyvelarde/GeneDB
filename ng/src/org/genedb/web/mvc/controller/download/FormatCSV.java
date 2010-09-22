package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.model.TranscriptDTO;

public class FormatCSV extends FormatBase {
	
	private Logger logger = Logger.getLogger(FormatCSV.class);
	
	public FormatCSV() {
		super();
		this.postRecordSeparator = "\n";
		this.headerContentStart = "#";
	}
	
	@Override
	public void formatBody(Iterator<TranscriptDTO> transcriptDTOs) throws IOException {
		
		logger.info(String.format("Formatting with separator '%s' and internal '%s'.", fieldSeparator, fieldInternalSeparator));
		
		while (transcriptDTOs.hasNext()) {
			TranscriptDTO transcriptDTO = transcriptDTOs.next();
			TranscriptDTOAdaptor adaptor = new TranscriptDTOAdaptor(transcriptDTO, fieldInternalSeparator);
			
			boolean first = true;
			for (OutputOption outputOption : outputOptions) {
				if (! first) {
					writer.append(fieldSeparator);
				} else {
					first = false;
				}
				String value = getFieldValue(adaptor, outputOption);
				writer.append(value);
			}
			writer.append(postRecordSeparator);
			
		}
		
	}
	

	@Override
	public void formatHeader() throws IOException {
		
		boolean first = true;
		if (header) {
			
			writer.append(headerContentStart);
			for (OutputOption outputOption : outputOptions) {
				if (! first) {
					writer.append(this.fieldSeparator);
				} else {
					first = false;
				}
				writer.append(outputOption.name());
			}
			writer.append(postRecordSeparator);
		}
	}

	@Override
	public void formatFooter() throws IOException {
		// blank
	}
	
}
