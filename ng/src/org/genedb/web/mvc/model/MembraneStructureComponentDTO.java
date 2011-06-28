package org.genedb.web.mvc.model;

import java.io.Serializable;

public class MembraneStructureComponentDTO implements Serializable {
	int fmin;
	int fmax;
	String compartment;
	String uniqueName;
	public int getFmin(){return fmin;}
	public int getFmax(){return fmax;}
	public String getCompartment(){return compartment;}
	public String getUniqueName(){return uniqueName;}
}