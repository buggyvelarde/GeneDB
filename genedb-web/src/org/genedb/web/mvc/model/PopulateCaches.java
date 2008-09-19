package org.genedb.web.mvc.model;

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.web.gui.ContextMapDiagram;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.RenderedContextMap;
import org.genedb.web.mvc.controller.ModelBuilder;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;

import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;


@Repository
@Transactional
public class PopulateCaches implements PopulateCachesI {

    private SessionFactory sessionFactory;
    private BlockingCache dtoCache;
    private BlockingCache contextMapCache;
    private ModelBuilder modelBuilder;
    private DiagramCache diagramCache;
    private LuceneIndexFactory luceneIndexFactory;
    private BasicGeneService basicGeneService;

    private static final int TILE_WIDTH = 5000;
    private int THUMBNAIL_WIDTH = 200;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"classpath:applicationContext.xml"});
        ctx.refresh();
        PopulateCachesI pc = (PopulateCachesI) ctx.getBean("populateCaches", PopulateCachesI.class);
        pc.fullCachePopulate();
    }

    @Transactional
    public void fullCachePopulate() {

        LuceneIndex luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
        basicGeneService = new BasicGeneServiceImpl(luceneIndex);

        long start = new Date().getTime();

        List<String> topLevelFeatures = (List<String>) sessionFactory.getCurrentSession().createQuery(
        "select f.uniqueName from Feature f, FeatureProp fp where fp.feature=f and fp.cvTerm.name='top_level_seq'")// and f.organism.commonName = 'Pfalciparum'")
        //.setString("name", "%chromosome%").list();
        .list();

        int count = 0;
        for (String featureName : topLevelFeatures) {
            Feature feature = (Feature) sessionFactory.getCurrentSession().createQuery(
            "from Feature f where f.uniqueName = :uniqueName")
            .setString("uniqueName", featureName).uniqueResult();

//            if (!"Pf3D7_01".equals(featureName)) {
//                continue;
//            }

            populateContextMapCache(feature, basicGeneService);

            List<Feature> features = (List<Feature>) sessionFactory.getCurrentSession().createQuery(
            "select f from Feature f, FeatureLoc fl where fl.sourceFeature.uniqueName=:feature and fl.feature=f")
            .setString("feature", featureName).list();


            //List<Feature> f = feature.getFeatureLocsForSrcFeatureId();
            System.err.print(feature.getUniqueName() + " : size "+ features.size() + " : ");
            for (Feature f : features) {
                //Feature f = fl.getFeature();
                //System.err.print("The type of '"+f.getClass().getName()+"'   ");
                if (f instanceof AbstractGene) {
                    System.err.println("processing '"+f.getUniqueName()+"' as is a gene");
                    populateDtoCache((AbstractGene) f);
                }// else {
                //    System.err.println(f.getUniqueName());
                //}
            }

            dtoCache.flush();

            sessionFactory.getCurrentSession().clear();
            count++;
            System.err.println(String.format("Count '%d' of '%d' : Total run time '%d's", count, topLevelFeatures.size(), (new Date().getTime() - start)/1000));
        }
    }



    private void populateContextMapCache(Feature feature, BasicGeneService basicGeneService) {

        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forChromosome(basicGeneService,
            feature.getOrganism().getCommonName(), feature.getUniqueName(), feature.getSeqLen());

        RenderedContextMap renderedContextMap = new RenderedContextMap(chromosomeDiagram);
        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeDiagram).asThumbnail(THUMBNAIL_WIDTH);

        String renderDirectoryPath = "/tmp";
        List<RenderedContextMap.Tile> tiles = renderedContextMap.renderTilesTo(renderDirectoryPath, TILE_WIDTH);

        String text;
        try {
            text = populateModel(renderedChromosomeThumbnail, renderedContextMap, tiles);
            contextMapCache.put(new Element(feature.getUniqueName(), text));
            System.err.println("Stored contextMap for '"+feature.getUniqueName()+"'");
        } catch (IOException exp) {
            System.err.println(exp);
        }
    }

    private String populateModel(RenderedContextMap chromosomeThumbnail, RenderedContextMap contextMap,
            List<RenderedContextMap.Tile> tiles) throws IOException {
        String chromosomeThumbnailURI = diagramCache.fileForDiagram(chromosomeThumbnail);

        ContextMapDiagram diagram = contextMap.getDiagram();

        Map<String,Object> model = new HashMap<String,Object>();

        model.put("organism", diagram.getOrganism());
        model.put("chromosome", diagram.getChromosome());
        model.put("numberOfPositiveTracks", diagram.numberOfPositiveTracks());
        model.put("geneTrackHeight", contextMap.getTrackHeight());
        model.put("scaleTrackHeight", contextMap.getScaleTrackHeight());
        model.put("exonRectHeight", contextMap.getExonRectHeight());
        model.put("tileHeight", contextMap.getHeight());
        model.put("basesPerPixel", contextMap.getBasesPerPixel());

        model.put("products", contextMap.getProducts());
        model.put("features", contextMap.getRenderedFeatures());

        model.put("start", diagram.getStart());
        model.put("end", diagram.getEnd());

        model.put("tilePrefix", "/" + contextMap.getRelativeRenderDirectory());
        model.put("tiles", tiles);

        Map<String,Object> chromosomeThumbnailModel = new HashMap<String,Object>();
        chromosomeThumbnailModel.put("src", chromosomeThumbnailURI);
        chromosomeThumbnailModel.put("width", chromosomeThumbnail.getWidth());
        model.put("chromosomeThumbnail", chromosomeThumbnailModel);

//        if( skipBindingResult && jsonConfig.getJsonPropertyFilter() == null ){
//            this.jsonConfig.setJsonPropertyFilter( new BindingResultPropertyFilter() );
//         }
         JSON json =  JSONSerializer.toJSON(model);
         //if( forceTopLevelArray ){
         //  json = new JSONArray().element(json);
        //}
         String text = json.toString(0);
         return text;
    }

    private void populateDtoCache(AbstractGene gene) {
        System.err.println("Storing gene '"+gene.getUniqueName()+"'");
        for (Transcript transcript : gene.getTranscripts()) {
            TranscriptDTO dto = modelBuilder.prepareTranscript(transcript);
            dtoCache.put(new Element(transcript.getUniqueName(), dto));
        }
    }

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public void setDiagramCache(DiagramCache diagramCache) {
        this.diagramCache = diagramCache;
    }

    public void setDtoCache(BlockingCache dtoCache) {
        this.dtoCache = dtoCache;
    }

    public void setContextMapCache(BlockingCache contextMapCache) {
        this.contextMapCache = contextMapCache;
    }

    public void setLuceneIndexFactory(LuceneIndexFactory luceneIndexFactory) {
        this.luceneIndexFactory = luceneIndexFactory;
    }

}
