package org.genedb.web.mvc.controller;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import java.util.List;

public class FeatureSummary {
	String id;
	int min;
	int max;
	char strand = '+';
	private int size;
	String product;
	
	protected void setSize(int size) {
		this.size = size;
	}

	public String getProduct() {
		return product;
	}

	protected void setProduct(String product) {
		this.product = product;
	}

	public int getSize() {
		return size;
	}

	public FeatureSummary(Feature feat) {
		Annotation an = feat.getAnnotation();
		this.id = "Unsure";
		if (an.containsProperty("locus_tag")) {
			this.id = (String) an.getProperty("locus_tag");
		} else {
			if (an.containsProperty("systematic_id")) {
				this.id = (String) an.getProperty("systematic_id");
			} else {
				if (an.containsProperty("gene")) {
					Object o = an.getProperty("gene");
					if (o instanceof String) {
						this.id = (String) o;
					} else {
						this.id = (String)((List)o).get(0);
					}
				}
			}
		}
		Location loc = feat.getLocation();
		this.min = loc.getMin();
		this.max = loc.getMax();
		this.size = this.max - this.min;
		if (feat instanceof StrandedFeature) {
			this.strand = ((StrandedFeature)feat).getStrand().getToken();
	}
		if (an.containsProperty("product")) {
			Object o = an.getProperty("product");
			if (o instanceof String) {
				this.product = (String) o;
			} else {
				this.product = (String)((List)o).get(0);
			}
		}
	 
	 
 }

	public String getId() {
		return id;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public char getStrand() {
		return strand;
	}

	protected void setId(String id) {
		this.id = id;
	}

	protected void setMax(int max) {
		this.max = max;
	}

	protected void setMin(int min) {
		this.min = min;
	}

	protected void setStrand(char strand) {
		this.strand = strand;
	}
}
