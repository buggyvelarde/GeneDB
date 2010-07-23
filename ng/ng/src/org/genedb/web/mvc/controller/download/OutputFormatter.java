package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface OutputFormatter {

    public void writeHeader() throws IOException;

    public void writeFooter() throws IOException;

    public String prepareExpression(List<OutputOption> outputOptions);

    public void writeBody(Iterator<String> iterator) throws IOException;

}
