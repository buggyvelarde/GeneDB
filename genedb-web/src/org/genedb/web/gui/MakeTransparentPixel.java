package org.genedb.web.gui;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * Simple class that uses the Java2D API to create a 1x1
 * transparent GIF. Used to generate the file
 * 
 *     <code>WebContent/includes/images/transparentPixel.gif</code>
 * 
 * @author rh11
 */
public class MakeTransparentPixel {
    private static final byte[] zero = new byte[] {0};
    private static final IndexColorModel colorModel
        = new IndexColorModel(1, 1, zero, zero, zero, zero);
    private static final BufferedImage image = new BufferedImage(
        1, 1, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: MakeTransparentPixel filename");
            System.exit(1);
        }
        File file = new File(args[0]);
        OutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "gif", out);
    }
}
