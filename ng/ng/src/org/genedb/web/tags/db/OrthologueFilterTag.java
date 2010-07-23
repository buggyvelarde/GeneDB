package org.genedb.web.tags.db;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.gmod.schema.mapped.Feature;

/**
 * Filters orthologue names given by a transcript DTO, just in case this includes ones from unpublished organisms that have been dropped in the db.  
 * @author gv1
 *
 */
public class OrthologueFilterTag extends SimpleTagSupport {
	
	private static final Logger logger = Logger.getLogger(OrthologueFilterTag.class);
	
	private String baseUrl;
	private TranscriptDTO dto;
	private SequenceDao sequenceDao;
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public void setDto(TranscriptDTO dto) {
		this.dto = dto;
	}
	
	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
	
	@Override
	public void doTag() throws JspException, IOException {
		
		// PageContext pageContext = (PageContext) getJspContext();
		// WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
		// SequenceDao sequenceDao = wac.getBean(SequenceDao.class);
		
		JspWriter out = getJspContext().getOut();
        for (String ortho : dto.getOrthologueNames()) {
        	Feature ortho_f = sequenceDao.getFeatureByUniqueName(ortho, Feature.class);
        	
        	// the assumption is that if there is a feature with that uniquename available on the web layer then it is public
        	if (ortho_f != null) {
        		logger.info(ortho + " - " + ortho_f);
        		out.write ("<a href=\"" + baseUrl + "/gene/\" >" + ortho_f.getUniqueName() + "</a> ");
        	} else {
        		logger.info(ortho + " - NOT FOUND");
        	}
        }
	}
}
