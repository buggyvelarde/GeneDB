package org.genedb.web.tags.misc;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
/**
 * Tag to Re-format the systematic name to remove the :pep or :mRNA surfix if found in the Systematic Name
 * @author LOke
 *
 */
public class SystematicNameFormatTag extends SimpleTagSupport {

	private String name;
	
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        String[] surfixes = {":pep", ":mRNA"};
        name = removeSurfixes(name, surfixes);
        out.print(name);
    }
	
	private String removeSurfixes(String name, String surfixes[]){
		for(int i=0; i<surfixes.length; ++i){
			if (name.toLowerCase().endsWith(surfixes[i].toLowerCase())){
				int removeIndex = name.toLowerCase().indexOf(surfixes[i].toLowerCase());
				return name.substring(0, removeIndex);
			}
		}		
		return name;
	}
}
