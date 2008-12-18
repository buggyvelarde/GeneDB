package org.genedb.web.mvc.controller;

import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.web.mvc.model.BerkeleyMapFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/ContextMap")
public class ContextMapController {
    /*
     * More than 30,000 pixels per tile, and almost all browsers
     * fail. More than 5,000 or so, and Linux Firefox crashes.
     */
    //private static final int TILE_WIDTH = 5000; // in pixels

    private Logger logger = Logger.getLogger(this.getClass());

    //private DiagramCache fileDiagramCache;
    private LuceneIndexFactory luceneIndexFactory; // Injected by Spring
    private View view; // Defined in genedb-servlet.xml
    private int cacheHit;
    private int cacheMiss;

//    private BlockingCache contextMapCache;
    private BerkeleyMapFactory bmf;

    private BasicGeneService basicGeneService;


    @PostConstruct
    private void init() {
        LuceneIndex luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
        basicGeneService = new BasicGeneServiceImpl(luceneIndex);
    }

    @RequestMapping(method=RequestMethod.GET)
    public void writeMapInfo(HttpServletRequest request, Writer out,
            HttpServletResponse response, String chromosome) throws IOException {

        String text = bmf.getContextMapMap().get(chromosome);
        if (text != null) {
            logger.trace("Cache hit for context map '"+chromosome+"' of '"+text+"'");
            cacheHit++;
        } else {
            logger.error(String.format("The context maps for '%s' aren't cached and need to be generated", chromosome));
            //text = contextMapCache.getKeys().toString();
            cacheMiss++;
        }

        out.write(text);
    }

    public LuceneIndexFactory getLuceneDao() {
        return luceneIndexFactory;
    }

    public void setLuceneIndexFactory(LuceneIndexFactory luceneIndexFactory) {
        this.luceneIndexFactory = luceneIndexFactory;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getCacheHit() {
        return cacheHit;
    }

    public int getCacheMiss() {
        return cacheMiss;
    }



//    public static class Command {
//        private String organism;
//        private String chromosome;
//        private int chromosomeLength;
//        private int thumbnailDisplayWidth;
//
//        public boolean hasRequiredData() {
//            return organism != null
//                && chromosome != null
//                && chromosomeLength > 0
//                && thumbnailDisplayWidth > 0;
//        }
//
//        // Getters and setters
//        public String getOrganism() {
//            return organism;
//        }
//
//        public void setOrganism(String organism) {
//            this.organism = organism;
//        }
//
//        public String getChromosome() {
//            return chromosome;
//        }
//
//        public void setChromosome(String chromosome) {
//            this.chromosome = chromosome;
//        }
//
//        public int getChromosomeLength() {
//            return chromosomeLength;
//        }
//
//        public void setChromosomeLength(int chromosomeLength) {
//            this.chromosomeLength = chromosomeLength;
//        }
//
//        public int getThumbnailDisplayWidth() {
//            return thumbnailDisplayWidth;
//        }
//
//        public void setThumbnailDisplayWidth(int displayWidth) {
//            this.thumbnailDisplayWidth = displayWidth;
//        }
//    }

    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

}
