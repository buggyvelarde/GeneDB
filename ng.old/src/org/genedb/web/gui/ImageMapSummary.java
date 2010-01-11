package org.genedb.web.gui;

import java.io.Serializable;

public class ImageMapSummary implements Serializable {

   
	private static final long serialVersionUID = 663680933938480853L;
	private int width;
    private int height;
    private String path;
    private String imageMap;

    public ImageMapSummary() {
        width = -1;
        height = -1;
    }


    public ImageMapSummary(int width, int height, String path, String imageMap) {
        this.width = width;
        this.height = height;
        this.path = path;
        this.imageMap = imageMap;
    }

    public boolean isValid() {
        return height > 0;
    }


    public int getWidth() {
        return width;
    }


    public int getHeight() {
        return height;
    }


    public String getPath() {
        return path;
    }


    public String getImageMap() {
        return imageMap;
    }



}
