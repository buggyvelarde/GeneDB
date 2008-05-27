package org.genedb.web.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContextMapWindowServlet extends HttpServlet {
    private static final int HEIGHT = 11;
    
    private static final String FORMAT = "png";
    private static final String MIME_TYPE = "image/png";
    
    private static final Color FRAME_COLOR = new Color(0, 0, 200);
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(MIME_TYPE);
        int width = Integer.parseInt(req.getParameter("width"));
        
        OutputStream out = resp.getOutputStream();
        BufferedImage image = new BufferedImage(width + 4, HEIGHT + 4, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D graf = (Graphics2D) image.getGraphics();

        graf.setColor(FRAME_COLOR);
        graf.drawRect(1, 1, width+1, HEIGHT+1);
        
        graf.setColor(Color.WHITE);
        graf.drawRect(0, 0, width+3, HEIGHT+3);
        
        ImageIO.write(image, FORMAT, out);
        graf.dispose();
    }
}
