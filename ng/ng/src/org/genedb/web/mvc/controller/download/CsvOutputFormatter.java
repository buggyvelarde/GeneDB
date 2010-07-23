package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class CsvOutputFormatter implements OutputFormatter {

    private Writer writer;
    boolean header;
    private List<OutputOption> outputOptions;
    private String seperator = "\t";


    public void setHeader(boolean header) {
        this.header = header;
    }


    public CsvOutputFormatter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void writeFooter() throws IOException {
        // Deliberately empty
    }

    @Override
    public void writeHeader() throws IOException {
        if (header) {
            writer.write("#");
            for (OutputOption outputOption : outputOptions) {
                writer.write(outputOption.name());
                writer.write(seperator);
            }
            writer.write("\n");
        }
    }

    public void writeBody(Iterator<String> it) throws IOException {

        while (it.hasNext()) {
            String row = it.next();
            boolean firstColumn = true;
            for (OutputOption outputOption : outputOptions) {
                if (firstColumn) {
                    firstColumn = false;
                } else {
                    writer.write(seperator);
                }
                writer.write(row);
            }
            writer.write("\n");
        }

    }


	@Override
	public String prepareExpression(List<OutputOption> outputOptions) {
		this.outputOptions = outputOptions;
		return OutputFormatterUtils.prepareExpression(outputOptions, "", "", "\t", "");
	}

}
