package org.genedb.web.mvc.controller;

import org.genedb.domain.Organism;

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

    	private Organism root;
    
	public void setRoot(Organism root) {
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

	private void showChildren(Writer out, Organism org, int level) throws IOException {
	    List<Organism> children = org.getChildren();
	    out.write("<h"+level+">"+org.getFullName()+"<h"+level+">\n");
	    for (Organism child : children) {
		showChildren(out, child, level+1);
	    }
	}
	
}