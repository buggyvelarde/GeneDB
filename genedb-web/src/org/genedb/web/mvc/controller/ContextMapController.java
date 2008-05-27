package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.util.HashMap;
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
        private String gene;
        private String organism, chromosome;
        private int chromosomeLength;
        private int start, end;
        private int displayWidth;
        private boolean hasStart = false, hasEnd = false;

        public boolean isGeneCommand() {
            return gene != null;
        }

        public boolean isRegionCommand() {
            return hasStart && hasEnd && organism != null && chromosome != null;
        }

        // Getters and setters
        public String getGene() {
            return gene;
        }

        public void setGene(String gene) {
            this.gene = gene;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
            this.hasStart = true;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
            this.hasEnd = true;
        }

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

        public int getDisplayWidth() {
            return displayWidth;
        }

        public void setDisplayWidth(int displayWidth) {
            this.displayWidth = displayWidth;
        }
    }

    private Map<String,Object> populateModel(RenderedContextMap renderedContextMap, RenderedContextMap chromosomeThumbnail) throws IOException {
        String contextMapURI = ContextMapCache.fileForDiagram(renderedContextMap, getServletContext());
        String chromosomeThumbnailURI = ContextMapCache.fileForDiagram(chromosomeThumbnail, getServletContext());

        ContextMapDiagram diagram = renderedContextMap.getDiagram();
        
        Map<String,Object> model = new HashMap<String,Object>();
        
        model.put("organism", diagram.getOrganism());
        model.put("chromosome", diagram.getChromosome());
        model.put("start", diagram.getStart());
        model.put("end", diagram.getEnd());
        model.put("locus", diagram.getLocus());
        model.put("basesPerPixel", renderedContextMap.getBasesPerPixel());

        model.put("imageSrc", contextMapURI);
        model.put("imageWidth", renderedContextMap.getWidth());
        model.put("imageHeight", renderedContextMap.getHeight());
        
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
        ContextMapDiagram contextMapDiagram;
        
        if (command.isGeneCommand()) {
            contextMapDiagram = ContextMapDiagram.forGene(basicGeneService, command.getGene(),
                DIAGRAM_WIDTH);
        } else // command.isRegionCommand
        {
            contextMapDiagram = ContextMapDiagram.forRegion(basicGeneService, command.getOrganism(),
                command.getChromosome(), command.getStart(), command.getEnd());
        }
        RenderedContextMap renderedContextMap = new RenderedContextMap(contextMapDiagram);
        
        ContextMapDiagram chromosomeThumbnail = ContextMapDiagram.forRegion(basicGeneService, command.getOrganism(), command.getChromosome(), 0, command.getChromosomeLength());
        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeThumbnail).asThumbnail(command.getDisplayWidth());
        
        return new ModelAndView(view, populateModel(renderedContextMap, renderedChromosomeThumbnail));
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
