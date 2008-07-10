package org.genedb.web.mvc.controller.cgview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class VirtualDigest {
    private static final Logger logger = Logger.getLogger(VirtualDigest.class);
    private int gelWidth = 200;
    private int totalWidth = 300;
    private int height = 600;
    private List<CutSize> cutSizes;
    private int length;
    float scale;
    private Map<String,List<String>> coords = new HashMap<String,List<String>>();
    
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
            if(cz.length < 0) {
                cz.length = ( this.length - lastCutPos ) + cutSite.getStart();
            }
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
        if(cz.length < 0) {
            cz.length = (this.length - lastCutPos ) + firstCutPos;
        }
        cutSizes.add(cz);
        if (cz.length > max) {
            max = cz.length;
        }
        scale = 0.8f*height/max;
    }
    
    
    public BufferedImage draw() throws IOException {
        DiagramDetails ret = new DiagramDetails();

        BufferedImage img = new BufferedImage(totalWidth, height, BufferedImage.TYPE_INT_RGB);
        ret.setImage(img);
        Graphics2D g2d = img.createGraphics();
        
        g2d.setPaint(Color.BLUE);
        g2d.fillRect(0, 0, gelWidth, height);
        g2d.setPaint(Color.BLUE);
        g2d.fillRect(gelWidth, 0, totalWidth - gelWidth, height);
        
        g2d.setPaint(Color.WHITE);
        Collections.sort(cutSizes);
        int count = 0;
        for (CutSize cutSize : cutSizes) {
            
            float y = cutSize.length * scale;
            Line2D.Float line = new Line2D.Float(0.0f, height - y, gelWidth, height - y);
            g2d.draw(line);

            //float newY = (1.0f * count/cutSizes.size())*height;

            //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            //        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            //g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            //g2d.setColor(Color.white);
            List<String> coord = new ArrayList<String>();
            coord.add(String.valueOf(height - y));
            coord.add(String.valueOf(gelWidth));
            coord.add(String.valueOf(height- y));
            this.coords.put(String.valueOf(cutSize.counter), coord);

            //Line2D.Float line2 = new Line2D.Float(gelWidth, height - y, totalWidth, height - newY);
            //g2d.draw(line2);
            count++;
        }
        
        return img;
    }


    public Map<String, List<String>> getCoords() {
        return coords;
    }


    public void setCoords(Map<String, List<String>> coords) {
        this.coords = coords;
    }


    public int getLength() {
        return length;
    }


    public void setLength(int length) {
        this.length = length;
    }


}

class CutSize implements Comparable<CutSize> {
    int counter;
    int length;
    
    public int compareTo(CutSize other) {
        if (length > other.length) {
            return 1;
        }
        if (length == other.length) {
            return 0;
        }
        return -1;
    }
    
}
