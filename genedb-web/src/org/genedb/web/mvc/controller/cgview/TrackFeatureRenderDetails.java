package org.genedb.web.mvc.controller.cgview;

import java.awt.Paint;
import java.awt.Rectangle;

public class TrackFeatureRenderDetails {
	
	private Rectangle labelRect;
	
	private Paint foreground;
	
	private double startAngle;
	
	private double extentAngle;
	
	private TrackFeature trackFeature;

	public TrackFeatureRenderDetails(TrackFeature trackFeature) {
		this.trackFeature = trackFeature;
	}

	public Paint getForeground() {
		return foreground;
	}

	public void setForeground(Paint foreground) {
		this.foreground = foreground;
	}

	public double getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(double startAngle) {
		this.startAngle = startAngle;
	}

	public double getExtentAngle() {
		return extentAngle;
	}

	public void setExtentAngle(double extentAngle) {
		this.extentAngle = extentAngle;
	}
	
	public String toString() {
		return "startAngle='"+startAngle+"' extentAngle='"+extentAngle+"'";
		
	}

	public TrackFeature getTrackFeature() {
		return trackFeature;
	}

}
