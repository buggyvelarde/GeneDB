package org.genedb.web.tags.db;

import org.apache.log4j.Logger;
import org.gmod.schema.utils.propinterface.PropertyI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

public class FilteredPropertyLoopTag extends LoopTagSupport {

	private String cvName;
	
	private String cvTermName;
	
	private Collection<PropertyI> items;
	
	private Iterator<PropertyI> it;
	
	private Logger logger = Logger.getLogger(FilteredPropertyLoopTag.class);
	
	@Override
	protected boolean hasNext() throws JspTagException {
		return it.hasNext();
	}

	@Override
	protected PropertyI next() throws JspTagException {
		return it.next();
	}

	@Override
	protected void prepare() throws JspTagException {
		// Filter the values list based on the cv and possibly the cvterm
		List<PropertyI> passed = new ArrayList<PropertyI>();
		for (PropertyI propertyI : items) {
			logger.info(propertyI.getCvTerm().getCv().getName());
			if (propertyI.getCvTerm().getCv().getName().equals(cvName)) {
				if (cvTermName == null || propertyI.getCvTerm().getName().equals(cvTermName)) {
					passed.add(propertyI);
				}
			}
			
		}
		it = passed.iterator();
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
