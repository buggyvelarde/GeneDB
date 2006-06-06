package org.genedb.db.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Synthetic {
    
    private int offSet = 0;
    private String name;
    private String soType;
    private Map properties;
    private List<Part> parts = new ArrayList<Part>();
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Map getProperties() {
        return this.properties;
    }
    public void setProperties(Map properties) {
        this.properties = properties;
    }
    public String getSoType() {
        return this.soType;
    }
    public void setSoType(String soType) {
        this.soType = soType;
    }
    public void addPart(Part part) {
	this.parts.add(part);
	part.setOffSet(offSet);
	offSet += part.getSize();
    }

    public int getSize() {
	return offSet;
    }
    public List<Part> getParts() {
        return this.parts;
    }

}
