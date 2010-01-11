package org.genedb.web.gui;

import java.io.IOException;

public interface DiagramCache {

    //public abstract String getBaseUri();

    /**
     * Get the path of a file containing an image representing this diagram,
     * relative to the document root. The file will always exist when we return,
     * whether or not it previously existed.
     *
     * @param renderedContextMap
     * @param servletContext
     * @return
     * @throws IOException
     * @throws ImageCreationException
     */
    public abstract String fileForContextMap(
            RenderedContextMap renderedContextMap) throws IOException, ImageCreationException;

    public abstract String fileForProteinMap(
            RenderedProteinMap renderedProteinMap) throws IOException, ImageCreationException;

}