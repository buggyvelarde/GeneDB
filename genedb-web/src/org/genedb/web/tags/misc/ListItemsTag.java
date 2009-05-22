package org.genedb.web.tags.misc;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ListItemsTag<T> extends SimpleTagSupport {

    private Collection<T> collection;
    private String separator = ", ";

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setCollection(Collection<T> collection) {
        this.collection = collection;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        boolean first = true;
        for (T item : collection) {
            if (!first) {
                out.write(separator);
            }
            out.write(item.toString());
            first = false;
        }
    }

}
