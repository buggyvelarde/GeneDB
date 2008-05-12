package org.gmod.schema.sequence;

import java.util.Collection;
import javax.persistence.Transient;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@SuppressWarnings("serial")
@Indexed
public class Exon extends Feature {
	private int start;
	private int stop;
	private String chr;
	private String strand;
	
	

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public void setChr(String chr) {
		this.chr = chr;
	}

	
	@Transient
	@Field(name = "chr",store=Store.YES)
	public String getChr(){
		String chr = "";
		Collection<FeatureLoc> featureLocs = this.getFeatureLocsForFeatureId();
		for (FeatureLoc featureLoc : featureLocs) {
			Feature f = featureLoc.getFeatureBySrcFeatureId();
			chr = f.getUniqueName();
		}
		return chr;
	}
	
	@Transient
	@Field(name = "start",store=Store.YES) 
	public int getStart() {
		int start = 0;
		Collection<FeatureLoc> featureLocs = this.getFeatureLocsForFeatureId();
		for (FeatureLoc featureLoc : featureLocs) {
			start = featureLoc.getFmin();
		}
		return start;
	}
	

	public void setStart(int start) {
		this.start = start;
	}
	
	@Transient
	@Field(name = "stop",store=Store.YES) 
	public int getStop() {
		int stop = 0;
		Collection<FeatureLoc> featureLocs = this.getFeatureLocsForFeatureId();
		for (FeatureLoc featureLoc : featureLocs) {
			stop = featureLoc.getFmax();
		}
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}
	
	@Transient
	@Field(name = "strand",store=Store.YES) 
	public String getStrand() {
		String strand = "+";
		Collection<FeatureLoc> featureLocs = this.getFeatureLocsForFeatureId();
		for (FeatureLoc featureLoc : featureLocs) {
			if(featureLoc.getStrand() == -1) {
				strand = "-";
			}
		}
		return strand;
	}
}
