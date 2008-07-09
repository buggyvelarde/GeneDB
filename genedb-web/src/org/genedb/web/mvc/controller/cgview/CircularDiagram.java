package org.genedb.web.mvc.controller.cgview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CircularDiagram {
    private List<CircularTrack> tracks = new ArrayList<CircularTrack>(1);
    private int width = 500;
    private int size;

    public CircularDiagram() {
        // CircularTrack track = new CircularTrack();

        // tracks.add(track);
    }

    public DiagramDetails draw() throws IOException {
        DiagramDetails ret = new DiagramDetails();

        BufferedImage img = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
        ret.setImage(img);
        Graphics2D g2d = img.createGraphics();
        g2d.translate(width / 2, width / 2);
        for (CircularTrack track : tracks) {
            track.draw(g2d);
        }

        return ret;
    }

    public static void main(String[] args) throws IOException {
        CircularDiagram cd = new CircularDiagram();
        cd.setSize(100);
        CircularTrack track = new CircularTrack();
        track.add(new TrackFeature(5, 20));
        track.add(new TrackFeature(20, 30));
        track.add(new TrackFeature(30, 65));
        track.add(new TrackFeature(65, 70));
        track.add(new TrackFeature(70, 5));
        cd.addTrack(track);
        DiagramDetails dd = cd.draw();
        JFrame frame = new JFrame();
        MyPanel panel = new MyPanel();
        panel.setBi(dd.getImage());
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }

    private void setSize(int size) {
        this.size = size;
    }

    private void addTrack(CircularTrack track) {
        this.tracks.add(track);
    }

}

class MyPanel extends JPanel {

    private BufferedImage bi;

    public MyPanel() {
        setBackground(Color.LIGHT_GRAY);
    }

    public void setBi(BufferedImage bi) {
        this.bi = bi;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(bi.getWidth(), bi.getHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getPreferredSize();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        super.paint(g2d);
        g2d.drawImage(bi, 0, 0, null);
    }

}

class DiagramDetails {
    private BufferedImage image;

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
