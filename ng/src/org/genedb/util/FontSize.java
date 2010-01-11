package org.genedb.util;

public class FontSize {

    private String fontName;
    private int size;

    public FontSize(String fontName, int size) {
        this.fontName = fontName;
        this.size = size;
    }

    public String getFontName() {
        return fontName;
    }

    public int getSize() {
        return size;
    }

}
