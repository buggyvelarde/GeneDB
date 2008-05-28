package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.web.gui.ContextMapCache;
import org.genedb.web.gui.ContextMapDiagram;
import org.genedb.web.gui.RenderedContextMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

public class ContextMapController extends PostOrGetFormController {
    private static final int DIAGRAM_WIDTH = 300000; // in bases

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

    private Map<String,Object> populateModel(List<RenderedContextMap> tiles, RenderedContextMap chromosomeThumbnail) throws IOException {
        String chromosomeThumbnailURI = ContextMapCache.fileForDiagram(chromosomeThumbnail, getServletContext());

        ContextMapDiagram diagram = tiles.get(0).getDiagram();
        
        Map<String,Object> model = new HashMap<String,Object>();
        
        model.put("organism", diagram.getOrganism());
        model.put("chromosome", diagram.getChromosome());
        model.put("basesPerPixel", tiles.get(0).getBasesPerPixel());
        model.put("geneTrackHeight", tiles.get(0).getGeneTrackHeight());
        model.put("scaleTrackHeight", tiles.get(0).getScaleTrackHeight());
        model.put("exonRectHeight", tiles.get(0).getExonRectHeght());
        model.put("tileHeight", tiles.get(0).getHeight());

        model.put("start", diagram.getStart());
        model.put("end", diagram.getEnd());

        List<Map<String,Object>> tileModels = new ArrayList<Map<String,Object>>();
        for (RenderedContextMap tile: tiles) {
            String contextMapURI = ContextMapCache.fileForDiagram(tile, getServletContext());
            Map<String,Object> tileModel = new HashMap<String,Object>();
            
            tileModel.put("src", contextMapURI);
            tileModel.put("width", tile.getWidth());
            tileModel.put("start", tile.getStart());
            tileModel.put("end", tile.getEnd());
         
            tileModels.add(tileModel);
        }
        model.put("tiles", tileModels);
        
        Map<String,Object> chromosomeThumbnailModel = new HashMap<String,Object>();
        chromosomeThumbnailModel.put("src", chromosomeThumbnailURI);
        chromosomeThumbnailModel.put("basesPerPixel", chromosomeThumbnail.getBasesPerPixel());
        model.put("chromosomeThumbnail", chromosomeThumbnailModel);
                
        return model;
    }

    @Override
    protected ModelAndView onSubmit(Object rawCommand) throws Exception {
        Command command = (Command) rawCommand;
        
        IndexReader indexReader = luceneDao.openIndex("org.gmod.schema.sequence.Feature");
        BasicGeneService basicGeneService = new BasicGeneServiceImpl(indexReader);
        
        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forRegion(basicGeneService, command.getOrganism(), command.getChromosome(), 0, command.getChromosomeLength());
        
        List<RenderedContextMap> tiles = new ArrayList<RenderedContextMap>();
        for (int i = 0; i < command.getChromosomeLength(); i += DIAGRAM_WIDTH) {
            tiles.add(new RenderedContextMap(chromosomeDiagram).restrict(i, i+DIAGRAM_WIDTH));
        }

        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeDiagram).asThumbnail(command.getThumbnailDisplayWidth());
        Map<String,Object> model = populateModel(tiles, renderedChromosomeThumbnail);
        
        model.put("positiveTracks", chromosomeDiagram.getPositiveTracks());
        model.put("negativeTracks", chromosomeDiagram.getNegativeTracks());
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
