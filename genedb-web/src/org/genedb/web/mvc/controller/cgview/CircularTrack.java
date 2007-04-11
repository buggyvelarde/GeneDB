package org.genedb.web.mvc.controller.cgview;

import org.w3c.dom.css.Rect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class CircularTrack {
	
	public List<TrackFeature> features = new ArrayList<TrackFeature>();
	public List<TrackFeatureRenderDetails> renderers = new ArrayList<TrackFeatureRenderDetails>();
	private boolean red = true;
	private double lastEnd = 0;
	private Image menuImg;
	
	private void makeRenderers() throws IOException {
		for (TrackFeature feature : features) {
			TrackFeatureRenderDetails tfrd = makeRenderer(feature);
			renderers.add(tfrd);
		}
		
		menuImg = ImageIO.read(new File("/nfs/team81/art/Desktop/prev_on_btn.gif"));
	}
	
	private TrackFeatureRenderDetails makeRenderer(TrackFeature feature) {
		TrackFeatureRenderDetails ret = new TrackFeatureRenderDetails(feature);
		if (red) {
			ret.setForeground(Color.RED);
			red = false;
		} else {
			ret.setForeground(Color.BLUE);
			red = true;
		}
		if (feature.isOverZero()) {
			ret.setForeground(Color.GREEN);
		}
		
		int totalSize = 100;
		
		double start = (360.0 * ((feature.start)*1.0/totalSize));
		double end = 360.0 * (feature.end * 1.0)/totalSize;
		lastEnd = feature.end;
		
		ret.setStartAngle(start);
		ret.setExtentAngle(end - start);
		if (feature.isOverZero()) {
			ret.setExtentAngle(360+end-start);
		}
		
		return ret;
	}
	

	public void draw(Graphics2D g2d) throws IOException {
		makeRenderers();
		
		g2d.setPaint(Color.WHITE);
		g2d.drawLine(0, 0, 0, 100);
		
		
		//g2d.setRenderingHint(, hintValue)
		for (TrackFeatureRenderDetails renderer : renderers) {
			System.err.println("Looking at renderer '"+renderer+"' foreground='"+renderer.getForeground()+"'");
			//if (!(renderer.getExtentAngle()<0)) {
				double width = 360.0;
				Arc2D arc1 = new Arc2D.Double(-180.0, -180.0, width, width, renderer.getStartAngle(), renderer.getExtentAngle(), Arc2D.OPEN);
				g2d.setPaint(renderer.getForeground());
				g2d.setStroke(new BasicStroke(3.0f));
				g2d.draw(arc1);
				
				double half = renderer.getStartAngle() + renderer.getExtentAngle()/2;
				
				float halfRadians = (float)((half/360)*2*Math.PI);
				float x = (float)(220 * Math.cos(halfRadians));
				float y = -1 * (float)(220 * Math.sin(halfRadians));
				
				
				g2d.drawString(renderer.getTrackFeature().toString(), x, y);
				//g2d.drawString(renderer.getTrackFeature().toString(), x, y+12);
				System.err.println("Drawing label at x='"+x+"' y='"+y+"'");
				
				// Next draw image at each one, not a label
				g2d.drawImage(menuImg, (int)x, (int)(y+3), null);
				
 				
//				Arc2D arc2 = new Arc2D.Double(-200.0, -200.0, 400, 400, renderer.getStartAngle(), half, Arc2D.PIE);
//				g2d.setPaint(Color.YELLOW);
//				g2d.setStroke(new BasicStroke(3.0f));
//				g2d.draw(arc2);
				
			//}
		}
		
//		double width = 400.0;
//		Arc2D arc1 = new Arc2D.Double(-200.0, -200.0, width, width, 0, 120, Arc2D.OPEN);
//		g2d.setPaint(Color.RED);
//		g2d.draw(arc1);
//		Arc2D arc2 = new Arc2D.Double(-200.0, -200.0, width, width, 0, -30, Arc2D.OPEN);
//		g2d.setPaint(Color.BLUE);
//		g2d.draw(arc2);
//		Arc2D arc3 = new Arc2D.Double(-200.0, -200.0, width, width, 160, 30, Arc2D.OPEN);
//		g2d.setPaint(Color.RED);
//		g2d.draw(arc3);
//		Arc2D arc4 = new Arc2D.Double(-200.0, -200.0, width, width, 120, 40, Arc2D.OPEN);
//		g2d.setPaint(Color.BLUE);
//		g2d.draw(arc4);
		//Rectangle2D r2d= new Rectangle2D.Double(-100.0, -150.0, 200.0, 300.0);
		//g2d.draw(r2d);
	}
	
	
	private double rad(int deg) {
		return (deg/360)*2*Math.PI;
	}

	public void add(TrackFeature feature) {
		features.add(feature);
	}

}
