package org.genedb.web.mvc.controller.cgview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VirtualDigest {
    private int gelWidth = 80;
    private int totalWidth = 300;
    private float labelStart = 1.0f * totalWidth - 35.0f;
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
        scale = 1.0f * height / max;
    }

    public BufferedImage draw() {
        DiagramDetails ret = new DiagramDetails();

        BufferedImage img = new BufferedImage(totalWidth, height, BufferedImage.TYPE_INT_RGB);
        ret.setImage(img);
        Graphics2D g2d = img.createGraphics();

        for (String font : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()) {
            System.err.println("Available font is '" + font + "'");
        }

        // Draw backgrounds
        g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, gelWidth, height);
        g2d.setPaint(Color.BLACK);
        g2d.fillRect(gelWidth, 0, totalWidth - gelWidth, height);

        g2d.setPaint(Color.WHITE);
        System.err.println("scale='" + scale + "'");
        Collections.sort(cutSizes);
        int count = 0;
        for (CutSize cutSize : cutSizes) {
            System.err.println("cutSize.length='" + cutSize.length + "'");

            float y = cutSize.length * scale;
            System.err.println("Drawing line at y='" + y + "'");
            Line2D.Float line = new Line2D.Float(0.0f, height - y, gelWidth, height - y);
            g2d.draw(line);
            // draw label next to it
            float newY = (1.0f * count / cutSizes.size()) * height;
            FontRenderContext frc = g2d.getFontRenderContext();
            Font f = new Font("SansSerif", Font.PLAIN, 8);
            TextLayout tl = new TextLayout("" + cutSize.counter, f, frc);
            // Rectangle2D theSize=tl.getBounds();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setColor(Color.white);
            tl.draw(g2d, labelStart, (height - newY) + tl.getAscent() / 2);
            // g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            // RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Line2D.Float line2 = new Line2D.Float(gelWidth, height - y, labelStart, height - newY
                    + 1.0f);
            g2d.draw(line2);
            count++;
        }

        return img;
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
