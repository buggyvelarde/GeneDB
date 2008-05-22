package org.genedb.web.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.web.gui.ContextMapDiagram;
import org.genedb.web.gui.RenderedContextMap;
import org.springframework.web.servlet.ModelAndView;

public class ContextMapController extends PostOrGetFormController {
    private static final int DIAGRAM_WIDTH = 300000; // in bases
    private static final int THUMBNAIL_WIDTH = 500; // in pixels

    private LuceneDao luceneDao; // Injected by Spring
    private String view; // Defined in genedb-servlet.xml

    public static class Command {
        private String gene;
        private String organism, chromosome;
        private int start, end;
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
    }

    @Override
    protected ModelAndView onSubmit(Object rawCommand) throws Exception {
        Command command = (Command) rawCommand;
        Map<String, Object> model = new HashMap<String, Object>();
        
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
        model.put("renderedContextMap", new RenderedContextMap(contextMapDiagram));
        
        ContextMapDiagram chromosomeThumbnail = ContextMapDiagram.forChromosome(basicGeneService, command.getOrganism(), command.getChromosome());
        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeThumbnail);
        renderedChromosomeThumbnail.setMaxWidth(THUMBNAIL_WIDTH);
        renderedChromosomeThumbnail.setScaleTrackHeight(3);
        renderedChromosomeThumbnail.setGeneTrackHeight(2);
        renderedChromosomeThumbnail.setExonRectHeght(2);
        renderedChromosomeThumbnail.setIntronRectHeight(2);
        model.put("chromosomeThumbnailMap", renderedChromosomeThumbnail);

        return new ModelAndView(view, model);
    }

    public LuceneDao getLuceneDao() {
        return luceneDao;
    }

    public void setLuceneDao(LuceneDao luceneDao) {
        this.luceneDao = luceneDao;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
