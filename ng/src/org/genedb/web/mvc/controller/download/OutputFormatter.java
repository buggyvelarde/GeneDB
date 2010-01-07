package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface OutputFormatter {

    public void writeHeader() throws IOException;

    public void writeFooter() throws IOException;

    public void writeBody(Iterator<DataRow> iterator) throws IOException;

    public void setOutputOptions(List<OutputOption> outputOptions);

}
