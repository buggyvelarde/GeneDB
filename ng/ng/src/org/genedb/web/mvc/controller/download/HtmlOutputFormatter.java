package org.genedb.web.mvc.controller.download;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class HtmlOutputFormatter implements OutputFormatter {

    private Logger logger = Logger.getLogger(HtmlOutputFormatter.class);

    private Writer writer;
    boolean header;
    public void setHeader(boolean header) {
        this.header = header;
    }

    private List<OutputOption> outputOptions;

    public HtmlOutputFormatter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void writeFooter() throws IOException {
        writer.write("</table>");
    }

    @Override
    public void writeHeader() throws IOException {
        writer.write("<table border=\"1\">");
        if (header) {
            writer.write("<tr>");
            for (OutputOption outputOption : outputOptions) {
                writer.write("<th>"+outputOption.name()+"</th>");
            }
            writer.write("</tr>");
        }
    }

    public void writeBody(Iterator<String> it) throws IOException {
        logger.error("About to fetch a new row");
        while (it.hasNext()) {
            String row = it.next();
            writer.write("<tr>");
            for (OutputOption outputOption : outputOptions) {
                logger.error("About to write value for "+outputOption);
                writer.write("<td>"+row+"</td>");
            }
            writer.write("</tr>");
        }
    }

	@Override
	public String prepareExpression(List<OutputOption> outputOptions) {
		this.outputOptions = outputOptions;
		return OutputFormatterUtils.prepareExpression(outputOptions, "<tr>", "</tr>", "<td>","</td>");
	}

}
