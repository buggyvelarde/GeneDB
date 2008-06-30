package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.lucene.index.IndexReader;
import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.web.gui.ContextMapCache;
import org.genedb.web.gui.ContextMapDiagram;
import org.genedb.web.gui.RenderedContextMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

public class ContextMapController extends PostOrGetFormController {
    /*
     * More than 30,000 pixels per tile, and almost all browsers
     * fail. More than 5,000 or so, and Linux Firefox crashes.
     */
    private static final int TILE_WIDTH = 5000; // in pixels

    private LuceneDao luceneDao; // Injected by Spring
    private View view; // Defined in genedb-servlet.xml

    public static class Command {
        private String organism, chromosome;
        private int chromosomeLength = 0;
        private int thumbnailDisplayWidth = 0;

        public boolean hasRequiredData() {
            return organism != null
                && chromosome != null
                && chromosomeLength > 0
                && thumbnailDisplayWidth > 0;
        }

        // Getters and setters
        public String getOrganism() {
            return organism;
        }

        public void setOrganism(String organism) {
            this.organism = organism;
        }

        public String getChromosome() {
            return chromosome;
        }

        public void setChromosome(String chromosome) {
            this.chromosome = chromosome;
        }

        public int getChromosomeLength() {
            return chromosomeLength;
        }

        public void setChromosomeLength(int chromosomeLength) {
            this.chromosomeLength = chromosomeLength;
        }

        public int getThumbnailDisplayWidth() {
            return thumbnailDisplayWidth;
        }

        public void setThumbnailDisplayWidth(int displayWidth) {
            this.thumbnailDisplayWidth = displayWidth;
        }
    }

    private Map<String,Object> populateModel(RenderedContextMap chromosomeThumbnail, RenderedContextMap contextMap,
            List<RenderedContextMap.Tile> tiles) throws IOException {
        String chromosomeThumbnailURI = ContextMapCache.fileForDiagram(chromosomeThumbnail, getServletContext());

        ContextMapDiagram diagram = contextMap.getDiagram();

        Map<String,Object> model = new HashMap<String,Object>();

        model.put("organism", diagram.getOrganism());
        model.put("chromosome", diagram.getChromosome());
        model.put("numberOfPositiveTracks", diagram.numberOfPositiveTracks());
        model.put("geneTrackHeight", contextMap.getGeneTrackHeight());
        model.put("scaleTrackHeight", contextMap.getScaleTrackHeight());
        model.put("exonRectHeight", contextMap.getExonRectHeight());
        model.put("tileHeight", contextMap.getHeight());
        model.put("basesPerPixel", contextMap.getBasesPerPixel());

        model.put("products", contextMap.getProducts());
        model.put("features", contextMap.getRenderedFeatures());

        model.put("start", diagram.getStart());
        model.put("end", diagram.getEnd());

        model.put("tilePrefix", getServletContext().getContextPath() + renderDirectory + "/" + contextMap.getRelativeRenderDirectory());
        model.put("tiles", tiles);

        Map<String,Object> chromosomeThumbnailModel = new HashMap<String,Object>();
        chromosomeThumbnailModel.put("src", chromosomeThumbnailURI);
        chromosomeThumbnailModel.put("width", chromosomeThumbnail.getWidth());
        model.put("chromosomeThumbnail", chromosomeThumbnailModel);

        return model;
    }

    private static final String PROP_CONTEXT_RENDER_DIRECTORY = "contextMap.render.directory";
    private static final ResourceBundle projectProperties = ResourceBundle.getBundle("project");
    private static final String renderDirectory = projectProperties
            .getString(PROP_CONTEXT_RENDER_DIRECTORY);

    @Override
    protected ModelAndView onSubmit(Object rawCommand) throws Exception {
        Command command = (Command) rawCommand;

        IndexReader indexReader = luceneDao.openIndex("org.gmod.schema.sequence.Feature");
        BasicGeneService basicGeneService = new BasicGeneServiceImpl(indexReader);

        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forChromosome(basicGeneService,
            command.getOrganism(), command.getChromosome(), command.getChromosomeLength());

        RenderedContextMap renderedContextMap = new RenderedContextMap(chromosomeDiagram);
        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeDiagram).asThumbnail(command.getThumbnailDisplayWidth());

        String renderDirectoryPath = getServletContext().getRealPath(renderDirectory);
        List<RenderedContextMap.Tile> tiles = renderedContextMap.renderTilesTo(renderDirectoryPath, TILE_WIDTH);

        Map<String,Object> model = populateModel(renderedChromosomeThumbnail, renderedContextMap, tiles);

        return new ModelAndView(view, model);
    }

    public LuceneDao getLuceneDao() {
        return luceneDao;
    }

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
