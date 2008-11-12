package org.genedb.pipeline.domain;

import java.util.List;
import java.util.Map;

public class Embl {
	
	private String key;
	private Location Location;
	private Map<String, List<String>> qualifiers;
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public Location getLocation() {
		return Location;
	}
	
	public void setLocation(Location location) {
		Location = location;
	}
	
	public Map<String, List<String>> getQualifiers() {
		return qualifiers;
	}
	
	public void setQualifiers(Map<String, List<String>> qualifiers) {
		this.qualifiers = qualifiers;
	}
	
	
	

}
