package org.genedb.jogra.controller;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_MITER;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageUtils {
    
    private static final int POINT = 6;
    private static final int HALF_POINT = POINT/2;
    private static final Stroke STROKE = new BasicStroke(POINT, CAP_ROUND, JOIN_MITER);
    
    private BufferedImage background;
    private int width;
    private int height;
    private int scale = 1;
    private boolean invert = false;
    //private String localFileName;
    private Map<String, Color> colourMap = new HashMap<String, Color>();
    
    public void setScale(int scale) {
        this.scale = scale;
    }
    
    public void setInvert(boolean invert) {
        this.invert = invert;
    }
    
    public ImageUtils() {
//        EventBus.subscribeStrongly(StageEvent.class, new EventSubscriber() {
//            public void onEvent(EventServiceEvent event) {
//                StageEvent se = (StageEvent) event;
//                Stage stage = se.getEventObject();
//                if (invert) {
//                    stage = se.getAlternateStage();
//                }
//                background = backgroundImages.get(stage);
//                //System.err.println("Set a background image '"+background+"'");
//            }
//        });
    }

    public void setBackgroundImageNames(Map<String, String> backgroundImageNames) throws IOException {
        //this.backgroundImages = new HashMap<Stage, BufferedImage>(backgroundImageNames.size());
        BufferedImage bi = null;
//        for (Map.Entry<String, String> entry : backgroundImageNames.entrySet()) {
//            bi = makeBackgroundFromClasspath(entry.getValue());
//            if (scale != 1) {
//                // Scale image
//                bi = getFasterScaledInstance(bi, bi.getWidth()/scale, bi.getHeight()/scale, 
//                        RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
//            }
//            this.backgroundImages.put(Stage.valueOf(entry.getKey()), bi);
//        }
//        this.width = bi.getWidth();
//        this.height = bi.getHeight();
//        this.background = backgroundImages.get(Stage.HOUR_31);
//        if (invert) {
//            this.background = backgroundImages.get(Stage.HOUR_48);
//        }
    }

//    public String getLocalFileName() {
//        return this.localFileName;
//    }
//
//    public void setLocalFileName(String localFileName) {
//        this.localFileName = localFileName;
//    }

    public BufferedImage makeBackgroundFromAbsoluteFile(File file) throws IOException {
        // Load an image from this file
        //System.err.println("Trying to make background '"+file.getAbsolutePath()+"'");
        BufferedImage bi = ImageIO.read(file);
        return bi;
    }
    
    public static BufferedImage makeBackgroundFromClasspath(String name) throws IOException {
        // Load an image from this file
        System.err.println("Trying to make background '"+name+"'");
        System.err.println(ImageUtils.class.getClassLoader());
        URL url = ImageUtils.class.getClassLoader().getResource(name);
        BufferedImage bi = ImageIO.read(url);
        return bi;
    }
    
    
    public void drawBackground(Graphics2D g2d) {
        g2d.drawImage(background, 0, 0, null);
    }

    public void setColourLookup(Map<String, String> colourLookup) {
        for (Map.Entry<String, String> entry : colourLookup.entrySet()) {
            String[] rgb = entry.getValue().split(",");
            Color colour = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
            colourMap.put(entry.getKey(), colour);
        }
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    private BufferedImage getFasterScaledInstance(BufferedImage img, 
            int targetWidth, int targetHeight, Object hint, 
            boolean progressiveBilinear) { 
        int type = (img.getTransparency() == Transparency.OPAQUE) ? 
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB; 
        BufferedImage ret = img; 
        BufferedImage scratchImage = null; 
        Graphics2D g2 = null; 
        int w, h; 
        int prevW = ret.getWidth(); 
        int prevH = ret.getHeight(); 
        if (progressiveBilinear) { 
          // Use multistep technique: start with original size, 
          // then scale down in multiple passes with drawImage() 
          // until the target size is reached 
          w = img.getWidth(); 
          h = img.getHeight(); 
        } else { 
          // Use one-step technique: scale directly from original 
          // size to target size with a single drawImage() call 
          w = targetWidth; 
          h = targetHeight; 
        } 
        do { 
            if (progressiveBilinear && w > targetWidth) { 
              w /= 2; 
              if (w < targetWidth) { 
                w = targetWidth; 
              } 
            } 
            if (progressiveBilinear && h > targetHeight) { 
              h /= 2; 
              if (h < targetHeight) { 
                h = targetHeight; 
              } 
            } 
            if (scratchImage == null) { 
              // Use a single scratch buffer for all iterations 
              // and then copy to the final, correctly sized image 
              // before returning 
              scratchImage = new BufferedImage(w, h, type); 
              g2 = scratchImage.createGraphics(); 
            } 
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                hint); 
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null); 
            prevW = w; 
            prevH = h; 
            ret = scratchImage; 
          } while (w != targetWidth || h != targetHeight); 
          if (g2 != null) { 
            g2.dispose(); 
          } 
          // If we used a scratch buffer that is larger than our 
          // target size, create an image of the right size and copy 
          // the results into it 
          if (targetWidth != ret.getWidth() || 
              targetHeight != ret.getHeight()) { 
            scratchImage = new BufferedImage(targetWidth, 
                                             targetHeight, type); 
            g2 = scratchImage.createGraphics(); 
            g2.drawImage(ret, 0, 0, null); 
            g2.dispose(); 
            ret = scratchImage; 
          } 
          
          return ret; 
        }
    
}
