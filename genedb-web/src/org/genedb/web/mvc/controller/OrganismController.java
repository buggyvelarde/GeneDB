package org.genedb.web.mvc.controller;

import org.genedb.domain.ExtendedOrganism;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 *
 * @author Adrian Tivey
 */
public class OrganismController extends AbstractController {

    	private ExtendedOrganism root;
    
	public void setRoot(ExtendedOrganism root) {
	    this.root = root;
	}

	/**
	 * Custom handler for homepage
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
	    response.setContentType("text/html");
	    try {
		Writer out = response.getWriter();
		out.write("<html><body>\n");
		showChildren(out, root, 1);
		out.write("</body></html>");
		out.close();
	    }
	    catch (IOException exp) {
		exp.printStackTrace();
	    }
	    return null;
	}

	private void showChildren(Writer out, ExtendedOrganism org, int level) throws IOException {
	    List<ExtendedOrganism> children = org.getChildren();
	    out.write("<h"+level+">"+org.getFullName()+"<h"+level+">\n");
	    for (ExtendedOrganism child : children) {
		showChildren(out, child, level+1);
	    }
	}
	
}