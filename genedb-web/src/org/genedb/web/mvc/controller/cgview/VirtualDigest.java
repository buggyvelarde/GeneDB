package org.genedb.web.mvc.controller.cgview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualDigest {
	private List<CircularTrack> tracks = new ArrayList<CircularTrack>(1);
	private int width = 100;
    private int height = 500;
	private int size;
    private List<CutSize> cutSizes;
    float scale;
    
    public void setCutSites(List<CutSite> cutSites) {
        int max = -1;
        cutSizes = new ArrayList<CutSize>(cutSites.size());
        for (int i = 0; i < cutSites.size(); i++) {
            CutSite ct = cutSites.get(i);
            CutSize cz = new CutSize();
            cz.counter = i;
            cz.length = ct.getEnd() - ct.getStart();
            cutSizes.add(cz);
            if (cz.length > max) {
                max = cz.length;
            }
        }
        scale = max/height;
    }
	
	
	public DiagramDetails draw() throws IOException {
		DiagramDetails ret = new DiagramDetails();

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ret.setImage(img);
		Graphics2D g2d = img.createGraphics();
        
        // Draw backgrounds
		g2d.setPaint(Color.BLACK);
        g2d.drawRect(0, 0, 80, 500);
        g2d.setPaint(Color.WHITE);
        g2d.drawRect(80, 0, 20, 500);
        
        for (CutSize cutSize : cutSizes) {
            float y = cutSize.length * scale;
            Line2D.Float line = new Line2D.Float(0.0f, y, 80.0f, y);
            g2d.draw(line);
            // draw label next to it
        }
        
		return ret;
	}


	private void setSize(int size) {
		this.size = size;
	}


	private void addTrack(CircularTrack track) {
	    this.tracks.add(track);
	}


}

class CutSize {
    int counter;
    int length;
}
