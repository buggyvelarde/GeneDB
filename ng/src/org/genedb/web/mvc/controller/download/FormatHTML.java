package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.gmod.schema.mapped.Feature;

public class FormatHTML extends FormatBase {
	
	private Logger logger = Logger.getLogger(FormatHTML.class);
	
	protected String headerFieldSeparator;
	protected String postHeaderFieldSeparator;
	
	public void setHeaderFieldSeparator(String headerFieldSeparator) {
		this.headerFieldSeparator = headerFieldSeparator;
	}
	
	public void setPostHeaderFieldSeparator(String postHeaderFieldSeparator) {
		this.postHeaderFieldSeparator = postHeaderFieldSeparator;
	}
	
	public FormatHTML() {
		super();
		fieldSeparator = "<td>";
		postFieldSeparator = "</td>";
		
		recordSeparator = "<tr>";
		postRecordSeparator = "</tr>";
		
		headerFieldSeparator = "<th>";
		postHeaderFieldSeparator = "</th>";
		
		String n = "\n";
		this.headerContentStart = "<html><head><title>GeneDB export results</title>" +
				"<style>" + n +
				"table { " + n +
				" border-collapse: collapse;" + n +
				" border-width:1px;" + n +
				" border-spacing:2px;" + n +
				"} " + n +
				"td, tr { " + n +
				" padding: 5px;" + n +
				"} " + n +
				"table, td, th, tr { " + n +
				" border: 1px solid black; " + n +
				"} " +  n +
				"</style></head><body><table>";
		this.footerContentStart = "</table></body></html>";
	}
	
	@Override
	public void formatBody(List<Feature> features) throws IOException {
		
		logger.info(String.format("Formatting separators : %s %s %s %s", fieldSeparator, postFieldSeparator, recordSeparator, postRecordSeparator));
		
		for (Feature feature : features) {
			
			writer.append(recordSeparator);
			
			for (String fieldValue : getFieldValues(feature, outputOptions)) {
				writer.append(fieldSeparator);
				writer.append(fieldValue);
				writer.append(postFieldSeparator);
			}
			
			writer.append(postRecordSeparator);
			
		}
	}

	@Override
	public void formatHeader() throws IOException {
		writer.append(headerContentStart);
		if (header) {
			writer.append(recordSeparator);
			for (OutputOption outputOption : outputOptions) {
				
				writer.append(headerFieldSeparator);
				writer.append(outputOption.name());
				writer.append(postHeaderFieldSeparator);
				
			}
			writer.append(postRecordSeparator);
		}
	}

	@Override
	public void formatFooter() throws IOException {
		writer.append(footerContentStart);
	}

}
