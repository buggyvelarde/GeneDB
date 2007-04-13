package org.genedb.web.mvc.controller.cgview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualDigest {
	private int width = 100;
    private int height = 600;
    private List<CutSize> cutSizes;
    float scale;
    
    public void setCutSites(List<CutSite> cutSites) {
        int max = -1;
        this.cutSizes = new ArrayList<CutSize>(cutSites.size());
        
        int counter = 1;
        Iterator<CutSite> it = cutSites.iterator();
        CutSite cutSite = it.next();
        int firstCutPos = cutSite.getStart();
        int lastCutPos = cutSite.getEnd();
        while (it.hasNext()) {
            cutSite = it.next();
            CutSize cz = new CutSize();
            cz.counter = counter;
            cz.length = cutSite.getStart() - lastCutPos;
            lastCutPos = cutSite.getEnd();
            cutSizes.add(cz);
            if (cz.length > max) {
                max = cz.length;
            }
            counter++;
        }
        CutSize cz = new CutSize();
        cz.counter = counter;
        cz.length = firstCutPos - lastCutPos;
        cutSizes.add(cz);
        if (cz.length > max) {
            max = cz.length;
        }
        scale = 1.0f*height/max;
    }
	
	
	public BufferedImage draw() throws IOException {
		DiagramDetails ret = new DiagramDetails();

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ret.setImage(img);
		Graphics2D g2d = img.createGraphics();
        
        // Draw backgrounds
		g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, 80, height);
        g2d.setPaint(Color.GRAY);
        g2d.fillRect(80, 0, 20, height);
        
        g2d.setPaint(Color.WHITE);
        System.err.println("scale='"+scale+"'");
        for (CutSize cutSize : cutSizes) {
            System.err.println("cutSize.length='"+cutSize.length+"'");
            
            float y = cutSize.length * scale;
            System.err.println("Drawing line at y='"+y+"'"); 
            Line2D.Float line = new Line2D.Float(0.0f, y, 80.0f, y);
            g2d.draw(line);
            // draw label next to it
        }
        
		return img;
	}


}

class CutSize {
    int counter;
    int length;
}
