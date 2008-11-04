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

import org.apache.log4j.Logger;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentLockedException;


@Repository
@Transactional
public class PopulateCaches {
    private static final Logger logger = Logger.getLogger(PopulateCaches.class);

    private BerkeleyMapFactory bmf;

    private StoredMap<String, TranscriptDTO> dtoMap;
    private StoredMap<String, String> contextMapMap;

    private SessionFactory sessionFactory;
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

        long start = System.currentTimeMillis();

        List<Feature> topLevelFeatures = getTopLevelFeatures();

        int count = 0;
        for (Feature feature : topLevelFeatures) {

            if (config.isDebugCount() && count >= config.getDebugCount()) {
                break;
            }

            if (!config.isNoContextMap()) {
                populateContextMapCache(feature, basicGeneService);
            }

            @SuppressWarnings("unchecked") List<Feature> features = sessionFactory.getCurrentSession().createQuery(
            "select f from Feature f, FeatureLoc fl where fl.sourceFeature=:feature and fl.feature=f")
            .setParameter("feature", feature).list();

            System.err.print(feature.getUniqueName() + " : size "+ features.size() + " : ");
            for (Feature f : features) {
                if (f instanceof AbstractGene) {
                    populateDtoCache((AbstractGene) f);
                }
            }

            sessionFactory.getCurrentSession().clear();
            count++;
            logger.info(String.format("Count %d of %d : Total run time %.02fs", count, topLevelFeatures.size(), (double)(System.currentTimeMillis() - start)/1000));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Feature> getTopLevelFeatures() {

        Query q = sessionFactory.getCurrentSession().createQuery(
        "select f from Feature f, FeatureProp fp where fp.feature=f and fp.cvTerm.name='top_level_seq'");

        if (config.isOrganism()) {
            q = sessionFactory.getCurrentSession().createQuery(
            "select f from Feature f, FeatureProp fp where fp.feature=f and fp.cvTerm.name='top_level_seq' and f.organism.commonName = :orgName");
            q.setString("orgName", config.getOrganism());
        }

        return q.list();
    }



    private void populateContextMapCache(Feature feature, BasicGeneService basicGeneService) {

        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forChromosome(basicGeneService,
            feature.getOrganism().getCommonName(), feature.getUniqueName(), feature.getSeqLen());

        RenderedContextMap renderedContextMap = new RenderedContextMap(chromosomeDiagram);
        RenderedContextMap renderedChromosomeThumbnail = new RenderedContextMap(chromosomeDiagram).asThumbnail(THUMBNAIL_WIDTH);

        String relativePath = feature.getOrganism().getCommonName() + "/" + MD5Util.getPathBasedOnMD5(feature.getUniqueName(), '/');
        File renderDirectory = new File(diagramCache.getContextMapRootDir() + "/" + relativePath);
        String renderURI = diagramCache.getBaseUri() + "/" + relativePath;
        renderDirectory.mkdirs();
        logger.trace("Rendering context map files to " + renderDirectory);
        List<RenderedContextMap.Tile> tiles = renderedContextMap.renderTilesTo(renderDirectory, TILE_WIDTH);

        String text;
        try {
            text = populateModel(renderedChromosomeThumbnail, renderedContextMap, renderURI, tiles);

            contextMapMap.put(feature.getUniqueName(), text);
            logger.info("Stored contextMap for '"+feature.getUniqueName()+"' as '"+text+"'");
        } catch (IOException exp) {
            logger.error(exp);
        }
    }

    private String populateModel(RenderedContextMap chromosomeThumbnail, RenderedContextMap contextMap,
            String renderURI, List<RenderedContextMap.Tile> tiles) throws IOException {
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

        model.put("tilePrefix", renderURI);
        model.put("tiles", tiles);

        Map<String,Object> chromosomeThumbnailModel = new HashMap<String,Object>();
        chromosomeThumbnailModel.put("src", chromosomeThumbnailURI);
        chromosomeThumbnailModel.put("width", chromosomeThumbnail.getWidth());
        model.put("chromosomeThumbnail", chromosomeThumbnailModel);

         JSON json =  JSONSerializer.toJSON(model);
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
