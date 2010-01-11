package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class CsvOutputFormatter implements OutputFormatter {

    private Writer writer;
    boolean header;
    private List<OutputOption> outputOptions;
    private String seperator;


    public void setHeader(boolean header) {
        this.header = header;
    }

    public void setOutputOptions(List<OutputOption> outputOptions) {
        this.outputOptions = outputOptions;
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
            writer.write("</tr>");
        }
    }

    public void writeBody(Iterator<DataRow> it) throws IOException {

        while (it.hasNext()) {
            DataRow row = it.next();
            boolean firstColumn = true;
            for (OutputOption outputOption : outputOptions) {
                if (firstColumn) {
                    firstColumn = false;
                } else {
                    writer.write(seperator);
                }
                writer.write(row.getValue(outputOption));
            }
            writer.write("\n");
        }

    }

}
