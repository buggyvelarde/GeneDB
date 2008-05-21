package org.genedb.web.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class ContextMapCache {
    private static final Logger logger = Logger.getLogger(ContextMapCache.class);

    private static final String PROP_CONTEXT_RENDER_DIRECTORY = "contextMap.render.directory";
    private static final ResourceBundle projectProperties = ResourceBundle.getBundle("project");
    private static final String renderDirectory = projectProperties
            .getString(PROP_CONTEXT_RENDER_DIRECTORY);

    public static String fileForDiagram(RenderedContextMap renderedContextMap,
            ServletContext servletContext) throws IOException {

        ContextMapDiagram diagram = renderedContextMap.getDiagram();

        File cacheRootDir = new File(servletContext.getRealPath(renderDirectory));
        if (!cacheRootDir.exists())
            throw new FileNotFoundException(renderDirectory);

        File organismDir = new File(cacheRootDir, diagram.getOrganism());
        if (!organismDir.exists()) {
            logger.warn(String
                    .format("Directory '%s' not found; attempting to create", organismDir));
            boolean success = organismDir.mkdir();
            if (!success)
                throw new IOException("Failed to create directory: " + organismDir);
        }

        File chromosomeDir = new File(organismDir, diagram.getChromosome());
        if (!chromosomeDir.exists()) {
            logger.warn(String
                    .format("Directory '%s' not found; attempting to create", organismDir));
            boolean success = chromosomeDir.mkdir();
            if (!success)
                throw new IOException("Failed to create directory: " + chromosomeDir);
        }

        String cacheFileName = String.format("%09d-%09d.%s", diagram.getStart(), diagram.getEnd(),
            RenderedContextMap.FILE_EXT);

        File cacheFile = new File(chromosomeDir, cacheFileName);
        String cacheFileRelativePath = servletContext.getContextPath() + renderDirectory + '/'
                + diagram.getOrganism() + '/' + diagram.getChromosome() + '/' + cacheFileName;
        if (cacheFile.exists())
            return cacheFileRelativePath;

        // File does not exist, so create it.
        FileOutputStream cacheOutputStream = new FileOutputStream(cacheFile);
        renderedContextMap.writeTo(cacheOutputStream);
        cacheOutputStream.close();

        assert cacheFile.exists();
        return cacheFileRelativePath;
    }
}
