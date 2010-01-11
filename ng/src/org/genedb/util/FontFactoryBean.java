package org.genedb.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public class FontFactoryBean implements FactoryBean<Font> {

    private int size;
    private int style;
    private Resource source;

    private Font font;

    private void makeFont() {
        try {
            InputStream is = source.getInputStream();

            // Creates the font at 1pt size
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

            is.close();

            // Makes a derived font at 12pt
            font = baseFont.deriveFont(style, size);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open diagram font file", e);
        } catch (FontFormatException e) {
            throw new RuntimeException("Failed to open diagram font file", e);
        }
    }



    public void setStyle(int style) {
        this.style = style;
    }



    public void setSize(int size) {
        this.size = size;
    }

    public void setSource(Resource source) {
        this.source = source;
    }


    @Override
    public Font getObject() throws Exception {
        makeFont();
        return font;
    }


    @Override
    public Class<? extends Font> getObjectType() {
        return Font.class;
    }


    @Override
    public boolean isSingleton() {
        return false;
    }

}
