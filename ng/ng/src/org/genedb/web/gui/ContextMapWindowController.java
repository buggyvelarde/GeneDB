package org.genedb.web.gui;

import org.genedb.util.ColorUtils;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/ContextMapWindow")
public class ContextMapWindowController {

    private static final int HEIGHT = 11;

    private static final String FORMAT = "gif";
    private static final String MIME_TYPE = "image/gif";

    private static final Color FRAME_COLOR = new Color(0, 0, 200, 255);

    /*
     * IE6 doesn't deal well with PNG alpha, so we generate a GIF89a
     * image. In order for ImageIO to produce the correct result (with
     * transparent pixels) we need to use an explicit indexed colour
     * model.
     */
    private final IndexColorModel colorModel = ColorUtils.colorModelFor(Color.WHITE, FRAME_COLOR);


    @RequestMapping(method=RequestMethod.GET, value="/{width}")
    public void writeImage(HttpServletResponse resp,
            OutputStream out,
            @PathVariable("width") int width) throws IOException {

        resp.setContentType(MIME_TYPE);

        BufferedImage image = new BufferedImage(width + 4, HEIGHT + 4,
            BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        Graphics2D graf = (Graphics2D) image.getGraphics();
        try {
            graf.setColor(FRAME_COLOR);
            graf.drawRect(1, 1, width+1, HEIGHT+1);

            graf.setColor(Color.WHITE);
            graf.drawRect(0, 0, width+3, HEIGHT+3);

            ImageIO.write(image, FORMAT, out);
        }
        finally {
            graf.dispose();
        }
    }
}
