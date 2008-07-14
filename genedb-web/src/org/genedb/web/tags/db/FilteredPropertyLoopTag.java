package org.genedb.web.tags.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.gmod.schema.utils.propinterface.PropertyI;

public class FilteredPropertyLoopTag extends LoopTagSupport {
    private String cvName;
    private String cvTermName;
    private Collection<PropertyI> items;
    private Iterator<PropertyI> filteredItemsIterator;

    @Override
    protected boolean hasNext() throws JspTagException {
        return filteredItemsIterator.hasNext();
    }

    @Override
    protected PropertyI next() throws JspTagException {
        return filteredItemsIterator.next();
    }

    @Override
    protected void prepare() throws JspTagException {
        // Filter the values list based on the cv and possibly the cvterm
        List<PropertyI> filteredItems = new ArrayList<PropertyI>();
        for (PropertyI propertyI : items)
            if ((cvName == null || propertyI.getType().getCv().getName().equals(cvName))
            && (cvTermName == null || propertyI.getType().getName().equals(cvTermName)))
                    filteredItems.add(propertyI);

        filteredItemsIterator = filteredItems.iterator();
    }

    public void setCv(String cvName) {
        this.cvName = cvName;
    }

    public void setCvTerm(String cvTermName) {
        this.cvTermName = cvTermName;
    }

    public void setItems(Collection<PropertyI> items) {
        this.items = items;
    }
}
