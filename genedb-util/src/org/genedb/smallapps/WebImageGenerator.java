package org.genedb.smallapps;

import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class WebImageGenerator {
    // Set your image dimensions here
    private static int IMAGE_WIDTH = 136;
    private static int IMAGE_HEIGHT = 163;
    //private static Color BG = Color.CYAN;
    private static Color BG = new Color(0xde, 0xde, 0xde);
    private static String fileName;
    private static int RADIUS = 4;

    public WebImageGenerator(String fileName) {
        WebImageGenerator.fileName = fileName;
    }

    public static void draw(Graphics2D g2d) throws IOException {
        BufferedImage dest = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        BufferedImage in = null;
        try {
            in = loadCompatibleImage(new URL(fileName));
        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        g2.setPaint(BG);
        g2.fillRect(0, 0, dest.getWidth(), dest.getHeight());

        Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
        hints.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        hints.put(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
        hints.put(KEY_RENDERING, VALUE_RENDER_QUALITY);
        g2.addRenderingHints(hints);

        // Round corners
        g2.clip(new RoundRectangle2D.Float(0, 0, in.getWidth(), in.getHeight(), RADIUS, RADIUS));
        g2.drawImage(in, 0, 0, null);

        g2.setClip(null);

        // Apply colour cast -do we want this?

        // Reflect and fade
        for (int x=0; x < 136; x++) {
            for (int y=0; y < 27; y++) {
                int pixel = dest.getRGB(x, 136-y);
                int r = (pixel & 0xFF0000) >>16;
                int g = (pixel & 0xFF00) >>8;
                int b = pixel & 0xFF;

                float fadeFraction = (y*1.0f/27);
                r = (int)((r + fadeFraction * (0xde -r)));
                g = (int)((g + fadeFraction * (0xde -g)));
                b = (int)((b + fadeFraction * (0xde -b)));
                //System.err.println(String.format("x='%d' y='%d' pc='%f' r='%d' g='%d' b='%d'", x,136-y, fadeFraction, r, g, b));
                int newVal = (pixel & 0xFF000000) | r << 16 | g << 8 | b;
                dest.setRGB(x, y+136, newVal);
            }
        }

        g2d.drawImage(dest, null, 0, 0);
    }

    public static void main(String[] args) throws IOException{
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JComponent component = new JComponent() {
            public void paintComponent(Graphics g) {
                try {
                    draw((Graphics2D) g);
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            public Dimension getPreferredSize() {
                return new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT);
            }
        };
        frame.add(component);
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');
        menuBar.add(menu);
        JMenuItem item = new JMenuItem("Save", 'S');
        menu.add(item);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                    try {
                        saveImage(component, chooser.getSelectedFile().getPath());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
            }
        });
        frame.pack();
        WebImageGenerator gd = new WebImageGenerator(args[0]);
        frame.setVisible(true);
    }

    private static void saveImage(final JComponent comp, String fileName) throws IOException {
        Rectangle rect = comp.getBounds();
        BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.WHITE);
        g.fill(rect);
        g.setColor(Color.BLACK);
        comp.paint(g);
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        ImageIO.write(image, extension, new File(fileName));
        g.dispose();
    }


    private static final GraphicsConfiguration CONFIGURATION =
        GraphicsEnvironment.getLocalGraphicsEnvironment().
        getDefaultScreenDevice().getDefaultConfiguration();


    public static BufferedImage loadCompatibleImage(URL resource)
    throws IOException {
        BufferedImage image = ImageIO.read(resource);
        return toCompatibleImage(image);
    }

    public static BufferedImage toCompatibleImage(BufferedImage image) {
        if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
            return image;
        }

        BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return compatibleImage;
    }

}

//class AlphaFadeFilter implements BufferedImageOp {
//
//    private final int fadeHeight;
//
//    public Rectangle2D getBounds2D(BufferedImage src) {
//        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
//    }
//
//    public BufferedImage createCompatibleDestImage(
//            BufferedImage src, ColorModel destCM) {
//
//        if (destCM == null) {
//            destCM = src.getColorModel();
//        }
//
//        return new BufferedImage(destCM,
//                destCM.createCompatibleWritableRaster(
//                        src.getWidth(), src.getHeight()),
//                        destCM.isAlphaPremultiplied(), null);
//    }
//
//    public Point2D getPoint2D(Point2D srcPt,
//            Point2D dstPt) {
//        return (Point2D) srcPt.clone();
//    }
//
//    public RenderingHints getRenderingHints() {
//        return null;
//    }
//
//    public AlphaFadeFilter(int fadeHeight) {
//        this.fadeHeight = fadeHeight;
//    }
//
//    @Override
//    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
//
//        if (dst == null) {
//            dst = createCompatibleDestImage(src, null);
//        }
//
//        int width = src.getWidth();
//        int height = src.getHeight();
//
//        int[] pixels = new int[width * height];
//        getPixels(src, 0, 0, width,
//                height, pixels);
//        mixColor(pixels, width);
//        setPixels(dst, 0, 0, width,
//                height, pixels);
//        return dst;
//    }
//
//
//    public int[] getPixels(BufferedImage img,
//            int x, int y,
//            int w, int h,
//            int[] pixels) {
//        if (w == 0 || h == 0) {
//            return new int[0];
//        }
//        if (pixels == null) {
//            pixels = new int[w * h];
//        } else if (pixels.length < w * h) {
//            throw new IllegalArgumentException(
//                    "pixels array must have a length >= w*h");
//        }
//
//        int imageType = img.getType();
//        if (imageType == BufferedImage.TYPE_INT_ARGB ||
//                imageType == BufferedImage.TYPE_INT_RGB) {
//            Raster raster = img.getRaster();
//            return (int[]) raster.getDataElements(x, y, w, h, pixels);
//        }
//
//        return img.getRGB(x, y, w, h, pixels, 0, w);
//    }
//
//    public void setPixels(BufferedImage img,
//            int x, int y, int w, int h, int[] pixels) {
//        if (pixels == null || w == 0 || h == 0) {
//            return;
//        } else if (pixels.length < w * h) {
//            throw new IllegalArgumentException("pixels array must have a length" +
//            " >= w*h");
//        }
//
//        int imageType = img.getType();
//        if (imageType == BufferedImage.TYPE_INT_ARGB ||
//                imageType == BufferedImage.TYPE_INT_RGB) {
//            WritableRaster raster = img.getRaster();
//            raster.setDataElements(x, y, w, h, pixels);
//        } else {
//            // Unmanages the image
//            img.setRGB(x, y, w, h, pixels, 0, w);
//        }
//    }
//
//    private void mixColor(int[] inPixels, int width) {
//        int row = 0;
//        int alpha = 0;
//        int maxI = 0;
//        for (int i = 0; i < inPixels.length; i++) {
//            if (i % width == 0) {
//                row++;
//                if (row > fadeHeight) {
//                    System.err.println("Break called");
//                    maxI = i;
//                    break;
//                }
//                int alpha1 = (((fadeHeight - row) * 255 /fadeHeight));
//                //alpha1 = alpha1 < 255 ? alpha1 : 255;
//                System.err.println("alpha="+alpha1);
//                alpha = alpha1 << 24;
//            }
//            int argb = inPixels[i];
//            int rest = argb & 0x00FFFFFF;
//            if (rest == 0) {
//                rest = 0xFFFFFF;
//            }
//            inPixels[i] = alpha | rest;
//            System.err.println(Integer.toHexString(argb)+" : "+Integer.toHexString(rest)+" : "+Integer.toHexString(inPixels[i]));
//        }
//        for (int i = maxI; i < inPixels.length; i++) {
//            inPixels[i] = 0xFFFFFFFF;
//        }
//    }

//}