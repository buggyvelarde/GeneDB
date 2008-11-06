package org.genedb.web.gui;

import org.genedb.web.mvc.model.BerkeleyMapFactory;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BdbDiagramCache implements DiagramCache {

    private static final Logger logger = Logger.getLogger(BdbDiagramCache.class);

    private BerkeleyMapFactory bmf;

    @Override
    public String fileForContextMap(RenderedContextMap renderedContextMap)
            throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderedContextMap.writeTo(out);
        out.close();

        String key = "wibble";
        bmf.getImageMap().put(key, out.toByteArray());
        return key;
    }

    @Override
    public String fileForProteinMap(RenderedProteinMap renderedProteinMap)
            throws IOException {
        // TODO Auto-generated method stub
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderedProteinMap.writeTo(out);
        out.close();

        String key = "wibble";
        bmf.getImageMap().put(key, out.toByteArray());
        return key;
    }

    @Override
    public String getBaseUri() {
        // TODO Auto-generated method stub
        return null;
    }

}
