package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.querying.tmpquery.GeneDetail;

public class FormatCSV extends FormatBase {
	
	private Logger logger = Logger.getLogger(FormatCSV.class);
	
	public FormatCSV() {
		super();
		this.postRecordSeparator = "\n";
		this.headerContentStart = "#";
	}
	
	@Override
	public void formatBody(List<GeneDetail> entries) throws IOException {
		
		logger.error(String.format("Formatting with separator '%s' and internal '%s'.", fieldSeparator, fieldInternalSeparator));
		
		for (GeneDetail entry: entries) {
			
			boolean first = true;
			for (String fieldValue : getFieldValues(getExtractor(entry), outputOptions)) {
				if (! first) {
					writer.append(fieldSeparator);
				} else {
					first = false;
				}
				
				writer.append(fieldValue);
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
