package org.genedb.db.domain.objects;

import java.io.Serializable;

public class Product implements Serializable {
	
	private int id;
	private String description;

	public Product(String description, int id) {
		this.description = description;
		this.id = id;
	}

	@Override
	public String toString() {
		return description;
	}
	
	
	
}
