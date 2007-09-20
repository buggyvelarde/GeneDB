package org.genedb.jogra.drawing;

import org.genedb.jogra.domain.ExpressionZone;
import org.genedb.jogra.domain.ExpressionZoneDao;

import org.bushe.swing.event.EventBus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;


public class ClickableImageArea extends JComponent implements MouseListener {
    private static final int INVALID_X_PRESS = -1;

    private static Map<String, Box> coords = new HashMap<String, Box>();
    
    private ExpressionZoneDao expressionZoneDao; 
    
    private ExpressionZone expressionZone;
    private String expressionZoneName; // TODO Remove once expression zone has getCode()
    private BufferedImage background;
    private int pressX;
    private int pressY;
    
    public void afterPropertiesSet() {
        ExpressionZone ez = expressionZoneDao.retrieveExpressionZoneByLabel("A");
        ExpressionEvent ee = new ExpressionEvent(this, ez);
        EventBus.publish(ee);
    }
    
    ClickableImageArea() {
        // Deliberately empty
    }
    
    ClickableImageArea(BufferedImage background) {
        this.background = background;
        addMouseListener(this);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void setImage(BufferedImage background) {
        this.background = background;
        addMouseListener(this);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(background.getWidth(), background.getHeight());
    }
    
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(background.getWidth(), background.getHeight());
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(background.getWidth(), background.getHeight());
    }
    
    @Override
    public void paint(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(background, 0, 0, null);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        
//        Box selected = coords.get(expressionZone.getCode());
        Box box = coords.get(expressionZoneName);
        if (box != null) {
            //System.err.println(box);
            g2d.drawRect(box.left, box.top, box.right-box.left, box.bottom-box.top);
        } else {
            System.err.println("Unable to find box for '"+expressionZone+"'");
        }
        
    }

//    public boolean keyDown(Event e,int k) {
//        if(k=='x' || k=='X') {
//        }
//        return true;
//    }


    public void mouseClicked(MouseEvent e) {  
        String category = getCategory(e.getX(), e.getY());
        //System.err.println("category is '"+category+"'");
        ExpressionZone ez = expressionZoneDao.retrieveExpressionZoneByLabel(category);
        if (ez != null) {
            expressionZone = ez;
            expressionZoneName = category;
            ExpressionEvent ee = new ExpressionEvent(this, ez);
            EventBus.publish(ee);
            repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseEntered(MouseEvent evt) {}

    public void mouseExited(MouseEvent evt) {}

    public void mousePressed(MouseEvent e) {}

    private String getCategory(int x, int y) {
        for (Map.Entry<String, Box> entry : coords.entrySet()) {
            if (x >= entry.getValue().left && x < entry.getValue().right 
                    && y >= entry.getValue().top && y < entry.getValue().bottom) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    static {
        List<Box> tmp = new ArrayList<Box>();
        tmp.add(new Box("A1", 10, 5, 109, 34));
        tmp.add(new Box("A2", 10, 34, 109, 62));
        tmp.add(new Box("A3", 10, 62, 109, 90));
        tmp.add(new Box("C1", 10, 90, 109, 118));
        tmp.add(new Box("C2", 10, 118, 109, 147));
        tmp.add(new Box("A", 109, 5, 194, 91));
        tmp.add(new Box("C", 109, 90, 194, 148));
        tmp.add(new Box("A-E", 194, 6, 237, 148));
        tmp.add(new Box("B", 237, 6, 322, 91));
        tmp.add(new Box("D", 237, 91, 322, 119));
        tmp.add(new Box("E", 237, 119, 322, 148));
        tmp.add(new Box("B1", 322, 6, 422, 35));
        tmp.add(new Box("B2", 322, 35, 422, 63));
        tmp.add(new Box("B3", 322, 63, 422, 91));
        tmp.add(new Box("F1", 436, 4, 521, 34));       
        tmp.add(new Box("F2", 436, 34, 521, 63));
        tmp.add(new Box("F", 521, 4, 606, 63));
        tmp.add(new Box("F-H", 606, 4, 649, 63));
        tmp.add(new Box("G", 649, 4, 734, 34));
        tmp.add(new Box("H", 649, 34, 734, 63));
        tmp.add(new Box("I", 576, 105, 661, 134));
        tmp.add(new Box("J", 576, 134, 661, 163));
        tmp.add(new Box("K", 661, 105, 749, 134)); 
        tmp.add(new Box("L", 661, 134, 749, 163));
        tmp.add(new Box("O", 348, 104, 434, 133));
        tmp.add(new Box("P", 348, 133, 434, 161));
        tmp.add(new Box("M", 434, 75, 519, 104)); 
        tmp.add(new Box("O-P", 434, 104, 519, 161)); 
        tmp.add(new Box("M-P", 519, 75, 561, 161));
        tmp.add(new Box("Q", 748, 5, 875, 34));
        tmp.add(new Box("R", 748, 34, 875, 62));
        tmp.add(new Box("S", 748, 62, 875, 89));
        tmp.add(new Box("Q-V", 875, 5, 918, 89));
        tmp.add(new Box("T", 918, 6, 1046, 34));
        tmp.add(new Box("U", 918, 34, 1046, 62));       
        tmp.add(new Box("V", 918, 62, 1046, 89));
        
        coords = new HashMap<String, Box>();
        for (Box box : tmp) {
            coords.put(box.msg, box);
        }
        
    }
    
    
    public void setExpressionZoneDao(ExpressionZoneDao expressionZoneDao) {
        this.expressionZoneDao = expressionZoneDao;
    }
    
}

    class Box {
        String msg;
        int left;
        int top;
        int right;
        int bottom;
        
        public Box(String msg, int left, int top, int right, int bottom) {
            this.msg = msg;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

