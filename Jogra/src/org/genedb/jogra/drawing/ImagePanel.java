package org.genedb.jogra.drawing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    
    /**
     * The splash image which is displayed on the splash window.
     */
    private BufferedImage image;
    
    private Dimension size;

    
    
    
    /**
     * Creates a new instance.
     * @param image the splash image.
     */
    public ImagePanel(BufferedImage image) {
        this.image = image;
        
        // Center the window on the screen
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        this.size = new Dimension(imgWidth, imgHeight);
    }
    
    /**
     * Updates the display area of the window.
     */
    @Override
    public void update(Graphics g) {
        paint(g);
    }
    
    /**
     * Paints the image on the window.
     */
    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    @Override
    public Dimension getMaximumSize() {
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        return size;
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }
	
    
}
