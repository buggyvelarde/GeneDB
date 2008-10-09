package org.genedb.web.mvc.model;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.util.MD5Util;
import org.genedb.web.gui.ContextMapDiagram;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.RenderedContextMap;
import org.genedb.web.mvc.controller.ModelBuilder;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentLockedException;




@Repository
@Transactional
public class PopulateCaches {


    private BerkeleyMapFactory bmf;

    private StoredMap<String, TranscriptDTO> dtoMap;
    private StoredMap<String, String> contextMapMap;

    private SessionFactory sessionFactory;
    //private BlockingCache dtoCache;
    //private BlockingCache contextMapCache;
    private ModelBuilder modelBuilder;
    private DiagramCache diagramCache;
    private LuceneIndexFactory luceneIndexFactory;
    private BasicGeneService basicGeneService;

    private static final int TILE_WIDTH = 5000;

    private int THUMBNAIL_WIDTH = 600;
    private PopulateCachesArgs config;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param args
     * @throws DatabaseException
     * @throws EnvironmentLockedException
     */
    public static void main(String[] args) {


        Cli<PopulateCachesArgs> cli = CliFactory.createCli(PopulateCachesArgs.class);
        PopulateCachesArgs pca = null;
        try {
          pca = cli.parseArguments(args);
        }
        catch(ArgumentValidationException exp) {
            System.err.println("Unable to run:");
            System.err.println(cli.getHelpMessage());
            return;
        }

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"classpath:applicationContext.xml"});
        ctx.refresh();
        PopulateCaches pc = (PopulateCaches) ctx.getBean("populateCaches", PopulateCaches.class);
        pc.setConfig(pca);
        pc.fullCachePopulate();

    }

    @Transactional
    public void fullCachePopulate() {


        dtoMap = bmf.getDtoMap(); // TODO More nicely
        contextMapMap = bmf.getContextMapMap();

        LuceneIndex luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
        basicGeneService = new BasicGeneServiceImpl(luceneIndex);

        long start = new Date().getTime();

        List<String> topLevelFeatures = getTopLevelFeatures();

        int count = 0;
        for (String featureName : topLevelFeatures) {

            if (config.isDebugCount() && count >= config.getDebugCount()) {
                break;
            }

            Feature feature = (Feature) sessionFactory.getCurrentSession().createQuery(
            "from Feature f where f.uniqueName = :uniqueName")
            .setString("uniqueName", featureName)
            .uniqueResult();

            if (!config.isNoContextMap()) {
                populateContextMapCache(feature, basicGeneService);
            }

            @SuppressWarnings("unchecked") List<Feature> features = (List<Feature>) sessionFactory.getCurrentSession().createQuery(
            "select f from Feature f, FeatureLoc fl where fl.sourceFeature.uniqueName=:feature and fl.feature=f")
            .setString("feature", featureName).list();


            //List<Feature> f = feature.getFeatureLocsForSrcFeatureId();
            System.err.print(feature.getUniqueName() + " : size "+ features.size() + " : ");
            for (Feature f : features) {
                //Feature f = fl.getFeature();
                //System.err.print("The type of '"+f.getClass().getName()+"'   ");
                if (f instanceof AbstractGene) {
                    //System.err.println("processing '"+f.getUniqueName()+"' as is a gene");
                    populateDtoCache((AbstractGene) f);
                }// else {
                //    System.err.println(f.getUniqueName());
                //}
            }

            //dtoCache.flush();

            sessionFactory.getCurrentSession().clear();
            count++;
            System.err.println(String.format("Count '%d' of '%d' : Total run time '%d's", count, topLevelFeatures.size(), (new Date().getTime() - start)/1000));
        }

        //dtoCache.dispose();
        //contextMapCache.flush();
        //contextMapCache.dispose();

    }

    @SuppressWarnings("unchecked")
    private List<String> getTopLevelFeatures() {

        Query q = sessionFactory.getCurrentSession().createQuery(
        "select f.uniqueName from Feature f, FeatureProp fp where fp.feature=f and fp.cvTerm.name='top_level_seq'");

        if (config.isOrganism()) {
            q = sessionFactory.getCurrentSession().createQuery(
            "select f.uniqueName from Feature f, FeatureProp fp where fp.feature=f and fp.cvTerm.name='top_level_seq' and f.organism.commonName = :orgName");
            q.setString("orgName", config.getOrganism());
        }

        return (List<String>) q.list();
    }



    private void populateContextMapCache(Feature feature, BasicGeneService basicGeneService) {

        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forChromosome(basicGeneService,
            feature.getOrganism().getCommonName(), feature.getUniqueName(), feature.getSeqLen());

        RenderedContextMap renderedContextMap = new RenderedContextMap(chromosomeDiagram);
        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeDiagram).asThumbnail(THUMBNAIL_WIDTH);

        File renderDirectory = new File(diagramCache.getContextMapRootDir() + "/" + feature.getOrganism().getCommonName() + "/" + MD5Util.getPathBasedOnMD5(feature.getUniqueName(), '/'));
        renderDirectory.mkdirs();
        List<RenderedContextMap.Tile> tiles = renderedContextMap.renderTilesTo(renderDirectory, TILE_WIDTH);

        String text;
        try {
            text = populateModel(renderedChromosomeThumbnail, renderedContextMap, renderDirectory, tiles);

            contextMapMap.put(feature.getUniqueName(), text);
            //contextMapCache.put(new Element(feature.getUniqueName(), text));
            System.err.println("Stored contextMap for '"+feature.getUniqueName()+"' as '"+text+"'");
        } catch (IOException exp) {
            System.err.println(exp);
        }
    }

    private String populateModel(RenderedContextMap chromosomeThumbnail, RenderedContextMap contextMap, File renderDirectory,
            List<RenderedContextMap.Tile> tiles) throws IOException {
        String chromosomeThumbnailURI = diagramCache.fileForContextMap(chromosomeThumbnail);

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

        String path = renderDirectory.getAbsolutePath().substring(4); // FIXME - BUG

        model.put("tilePrefix", diagramCache.getBaseUri()+path);
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
        //System.err.println("Storing gene '"+gene.getUniqueName()+"'");
        for (Transcript transcript : gene.getTranscripts()) {
            TranscriptDTO dto = modelBuilder.prepareTranscript(transcript);

            dtoMap.put(transcript.getUniqueName(), dto);
        }
    }

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public void setDiagramCache(DiagramCache diagramCache) {
        this.diagramCache = diagramCache;
    }

    public void setLuceneIndexFactory(LuceneIndexFactory luceneIndexFactory) {
        this.luceneIndexFactory = luceneIndexFactory;
    }

    public void setConfig(PopulateCachesArgs pca) {
        this.config = pca;
    }

    public void setDtoMap(StoredMap<String, TranscriptDTO> dtoMap) {
        this.dtoMap = dtoMap;
    }

    public void setContextMapMap(StoredMap<String, String> contextMapMap) {
        this.contextMapMap = contextMapMap;
    }


    interface PopulateCachesArgs {

        @Option(shortName="o", description="Only populate cache for this organism")
        String getOrganism();

        boolean isOrganism();

        @Option
        String getGlobalFileRoot();

        boolean isGlobalFileRoot();

        @Option
        int getDebugCount();

        boolean isDebugCount();

        @Option(longName="ncm", description="Don't generate context maps")
        boolean isNoContextMap();

    }


    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

}
