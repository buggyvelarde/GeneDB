//package org.genedb.web.mvc.model;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import net.sf.json.JSON;
//import net.sf.json.JSONSerializer;
//
//import org.apache.log4j.Logger;
//import org.genedb.db.domain.services.BasicGeneService;
//import org.genedb.web.gui.ContextMapDiagram;
//import org.genedb.web.gui.DiagramCache;
//import org.genedb.web.gui.ImageCreationException;
//import org.genedb.web.gui.RenderedContextMap;
//import org.genedb.web.gui.RenderedDiagramFactory;
//import org.gmod.schema.feature.AbstractGene;
//import org.gmod.schema.mapped.Feature;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.springframework.orm.hibernate3.SessionFactoryUtils;
//
//import com.sleepycat.collections.StoredMap;
//
///**
// * To provide a set of generic helper methods to be used in multiple places
// * @author sangerinstitute
// *
// */
//public class CacheDBHelper {
//    private static final Logger logger = Logger.getLogger(CacheDBHelper.class);
//
//    private static final int TILE_WIDTH = 5000;
//    private static final int THUMBNAIL_WIDTH = 600;
//
//    public static final int MIN_CONTEXT_LENGTH_BASES = 100;
//
//     /**
//      * Find a Gene, using it's unique name
//      * @param geneUniqueName
//      * @param sessionFactory
//      * @return
//      */
//    public static AbstractGene findGene(String geneUniqueName, SessionFactory sessionFactory) {
//        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
//
//        return (AbstractGene) session.createQuery(
//            "select g from AbstractGene g" +
//            " where g.uniqueName = :geneUniqueName")
//        .setParameter("geneUniqueName", geneUniqueName)
//        .uniqueResult();
//    }
//    
//    /**
//     * 
//     * @param feature
//     * @param basicGeneService
//     * @param renderedDiagramFactory
//     * @param diagramCache
//     * @param contextMapMap
//     */
//    public static void populateContextMapCache(
//            Feature feature, BasicGeneService basicGeneService,
//            RenderedDiagramFactory renderedDiagramFactory,
//            DiagramCache diagramCache, StoredMap<Integer, String> contextMapMap) {
//    	populateContextMapCache(feature, basicGeneService, renderedDiagramFactory, diagramCache, contextMapMap, TILE_WIDTH);
//    }
//    
//    /**
//     * 
//     * @param feature
//     * @param basicGeneService
//     * @param renderedDiagramFactory
//     * @param diagramCache
//     * @param contextMapMap
//     * @param tileWidth
//     */
//    public static void populateContextMapCache(
//            Feature feature, BasicGeneService basicGeneService,
//            RenderedDiagramFactory renderedDiagramFactory,
//            DiagramCache diagramCache, StoredMap<Integer, String> contextMapMap, int tileWidth) {
//
//        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forChromosome(basicGeneService,
//            feature.getOrganism().getCommonName(), feature.getUniqueName(), feature.getFeatureId(), feature.getSeqLen());
//
//        RenderedContextMap renderedContextMap = (RenderedContextMap) renderedDiagramFactory.getRenderedDiagram(chromosomeDiagram);
//        RenderedContextMap renderedChromosomeThumbnail = (RenderedContextMap) renderedDiagramFactory.getRenderedDiagram(chromosomeDiagram).asThumbnail(THUMBNAIL_WIDTH);
//        
//        logger.debug(String.format("About to try and generate context map for '%s'", feature.getUniqueName()));
//        try {
//            List<RenderedContextMap.Tile> tiles = renderedContextMap.renderTiles(tileWidth);
//            String metadata = contextMapMetadata(renderedChromosomeThumbnail, renderedContextMap, tiles, diagramCache);
//            contextMapMap.put(feature.getFeatureId(), metadata);
//            logger.debug("Stored contextMap for "+feature.getFeatureId()+" '"+feature.getUniqueName()+"' as '"+metadata+"'");
//        } catch (IOException exp) {
//            logger.error(exp);
//        }
//        catch (ImageCreationException exp) {
//            logger.error(exp);
//        }
//    }
//
//    /**
//     *
//     * @param chromosomeThumbnail
//     * @param contextMap
//     * @param tiles
//     * @param diagramCache
//     * @return
//     * @throws IOException
//     * @throws ImageCreationException
//     */
//    private static String contextMapMetadata(RenderedContextMap chromosomeThumbnail, RenderedContextMap contextMap,
//            List<RenderedContextMap.Tile> tiles, DiagramCache diagramCache) throws IOException, ImageCreationException {
//        String chromosomeThumbnailKey = diagramCache.fileForContextMap(chromosomeThumbnail);
//
//        ContextMapDiagram diagram = contextMap.getDiagram();
//
//        Map<String,Object> model = new HashMap<String,Object>();
//
//        model.put("organism", diagram.getOrganism());
//        model.put("chromosome", diagram.getChromosome());
//        model.put("numberOfPositiveTracks", diagram.numberOfPositiveTracks());
//        model.put("geneTrackHeight", contextMap.getTrackHeight());
//        model.put("scaleTrackHeight", contextMap.getScaleTrackHeight());
//        model.put("exonRectHeight", contextMap.getExonRectHeight());
//        model.put("tileHeight", contextMap.getHeight());
//        model.put("basesPerPixel", contextMap.getBasesPerPixel());
//
//        model.put("products", contextMap.getProducts());
//        model.put("features", contextMap.getRenderedFeatures());
//
//        model.put("start", diagram.getStart());
//        model.put("end", diagram.getEnd());
//
//        model.put("tilePrefix", "/Image/");
//        model.put("tiles", tiles);
//
//        Map<String,Object> chromosomeThumbnailModel = new HashMap<String,Object>();
//        chromosomeThumbnailModel.put("src", chromosomeThumbnailKey);
//        chromosomeThumbnailModel.put("width", chromosomeThumbnail.getWidth());
//        model.put("chromosomeThumbnail", chromosomeThumbnailModel);
//
//        JSON json =  JSONSerializer.toJSON(model);
//        String text = json.toString(0);
//        return text;
//    }
//}
