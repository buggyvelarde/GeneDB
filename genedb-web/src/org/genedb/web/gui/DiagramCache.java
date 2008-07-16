package org.genedb.web.gui;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

public class DiagramCache {
    private static final Logger logger = Logger.getLogger(DiagramCache.class);

    private static final ResourceBundle projectProperties = ResourceBundle.getBundle("project");
    private static final String contextMapRenderDirectory = projectProperties
            .getString("contextMap.render.directory");
    private static final String proteinMapRenderDirectory = projectProperties
        .getString("proteinMap.render.directory");

    /**
     * Get the path of a file containing an image representing this diagram,
     * relative to the document root. The file will always exist when we return,
     * whether or not it previously existed.
     *
     * @param renderedContextMap
     * @param servletContext
     * @return
     * @throws IOException
     */
    public static String fileForDiagram(RenderedContextMap renderedContextMap,
            ServletContext servletContext) throws IOException {

        ContextMapDiagram diagram = renderedContextMap.getDiagram();

        File cacheRootDir = new File(servletContext.getRealPath(contextMapRenderDirectory));
        if (!cacheRootDir.exists())
            throw new FileNotFoundException(contextMapRenderDirectory);

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

        String cacheFileName = fileForDiagram(renderedContextMap, chromosomeDir);

        return servletContext.getContextPath() + contextMapRenderDirectory + '/'
        + diagram.getOrganism() + '/' + diagram.getChromosome() + '/' + cacheFileName;
    }

    public static String fileForDiagram(RenderedProteinMap renderedProteinMap,
            ServletContext servletContext) throws IOException {

        ProteinMapDiagram diagram = renderedProteinMap.getDiagram();

        File cacheRootDir = new File(servletContext.getRealPath(proteinMapRenderDirectory));
        if (!cacheRootDir.exists())
            throw new FileNotFoundException(proteinMapRenderDirectory);

        File organismDir = new File(cacheRootDir, diagram.getOrganism());
        if (!organismDir.exists()) {
            logger.warn(String
                    .format("Directory '%s' not found; attempting to create", organismDir));
            boolean success = organismDir.mkdir();
            if (!success)
                throw new IOException("Failed to create directory: " + organismDir);
        }

        String cacheFileName = fileForDiagram(renderedProteinMap, organismDir);
        return servletContext.getContextPath() + proteinMapRenderDirectory
            + '/' + diagram.getOrganism() + '/' + cacheFileName;
    }


    private static String fileForDiagram(RenderedDiagram renderedDiagram, File chromosomeDir)
        throws IOException {

        String cacheFileName = renderedDiagram.getPreferredFilename();

        File cacheFile = new File(chromosomeDir, cacheFileName);
        File tempFile  = new File(chromosomeDir, cacheFileName + ".new");
        File lockFile  = new File(chromosomeDir, cacheFileName + ".lock");

        /*
         * Currently we ALWAYS create the file, even if it already exists. The reason
         * is that, if the diagram has not been rendered, the <code>renderedFeatures</code>
         * collection is not populated, which is needed in the model. This is a somewhat
         * unsatisfactory situation, which may be reconsidered in future.
         */
//        if (cacheFile.exists()) {
//            return cacheFileRelativePath;
//        }

        FileOutputStream tempOutputStream = new FileOutputStream(tempFile);
        FileOutputStream lockOutputStream = new FileOutputStream(lockFile);
        lockOutputStream.getChannel().lock();
        try {
            renderedDiagram.writeTo(tempOutputStream);
        }
        catch (Exception e) {
            tempOutputStream.close();
            tempFile.delete();
            lockOutputStream.close();
            lockFile.delete();

            throw new RuntimeException(
                String.format("Failed to create context map image '%s'", cacheFile), e);
        }

        tempOutputStream.close();
        if (!tempFile.renameTo(cacheFile)) {
            throw new RuntimeException(String.format("Failed to rename '%s' to '%s'", tempFile, cacheFile));
        }

        lockOutputStream.close(); // Releases lock
        lockFile.delete();

        assert cacheFile.exists();
        return cacheFileName;
    }
}
