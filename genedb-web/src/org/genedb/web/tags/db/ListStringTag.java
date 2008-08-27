package org.genedb.web.tags.db;

import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Synonym;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ListStringTag extends SimpleTagSupport {

    private Collection<FeatureSynonym> collection;
    private String separator = ", ";

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setCollection(Collection<FeatureSynonym> collection) {
        this.collection = collection;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        boolean first = true;
        for (FeatureSynonym featSynonym : collection) {
            if (!first) {
                out.write(separator);
            }
            Synonym synonym = featSynonym.getSynonym();
            out.write(synonym.getName());
            first = false;
        }
    }

}
