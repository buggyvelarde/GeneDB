package org.genedb.arttb.sqlt_hibernate.rdbms;

import java.io.File;
import java.io.PrintWriter;

import org.genedb.arttb.sqlt_hibernate.OutputStyle;



public class Index implements ItemRDBMS {

    private String name;
    private String type;
    private String fields;
    private String options;
    private String[] splitFields;
	
    
    
	public String[] getSplitFields() {
		return splitFields;
	}



	public void setName(String name) {
		this.name = name;
	}



	public void outputItem(OutputStyle style, PrintWriter out, File file) {
		if (style.equals(OutputStyle.SQLT_XML)) {
			out.println("\t\tIndex: name='"+name+"' type='" + type + "' fields='" + fields + "' options='" + options + "'");
		}
	}



	public void setType(String type) {
		this.type = type;
	}

	public void setFields(String fields) {
		this.fields = fields;
		this.splitFields = fields.split(",");
	}



	public void setOptions(String options) {
		this.options = options;
	}



	public String getName() {
		return name;
	}

}
