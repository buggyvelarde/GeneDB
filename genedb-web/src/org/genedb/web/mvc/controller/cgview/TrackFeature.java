package org.genedb.web.mvc.controller.cgview;

public class TrackFeature {
	
	int start;
	int end;
	boolean overZero = false;
	
	
	public TrackFeature(int start, int end) {
		this.start = start;
		this.end = end;
		
		if (start > end) {
			overZero = true;
		}
		
	}
	
	public boolean isOverZero() {
		return overZero;
	}
	
	@Override
    public String toString() {
		return (end-start)+" bp ("+ start + "-" + end+")";
	}

}
