package org.genedb.web.mvc.controller;

public class ResultHit {
	
	private String name;
	private String type;
	private String organism;
	private String product;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOrganism() {
		return organism;
	}
	
	public void setOrganism(String organism) {
		this.organism = organism;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}