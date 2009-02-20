package org.genedb.web.mvc.model;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.web.gui.ContextMapDiagram;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.RenderedContextMap;
import org.genedb.web.gui.RenderedDiagramFactory;
import org.genedb.web.mvc.controller.ModelBuilder;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
    private RenderedDiagramFactory renderedDiagramFactory;

    private static final int TILE_WIDTH = 5000;
    private static final int THUMBNAIL_WIDTH = 600;
    private static final int MIN_CONTEXT_LENGTH_BASES = 5000;

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
            System.exit(64);
        }

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"classpath:applicationContext.xml", "classpath:populateCaches.xml"});
        ctx.refresh();
        PopulateCaches pc = ctx.getBean("populateCaches", PopulateCaches.class);
        pc.setConfig(pca);
        pc.fullCachePopulate();
    }

    @Transactional
    public void fullCachePopulate() {
        dtoMap = bmf.getDtoMap(); // TODO More nicely
        contextMapMap = bmf.getContextMapMap();

        LuceneIndex luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
        basicGeneService = new BasicGeneServiceImpl(luceneIndex);

        String geneUniqueName = config.getGeneUniqueName();
        if (geneUniqueName == null) {
            populateCacheForTopLevelFeatures();
        } else {
            populateCacheForGene(geneUniqueName);
        }
    }

    /**
     * @param session
     */
    private void populateCacheForTopLevelFeatures() {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        long start = System.currentTimeMillis();

        Iterator<Feature> topLevelFeatures = getTopLevelFeatures();

        int count = 0;
        while (topLevelFeatures.hasNext()) {
            Feature feature = topLevelFeatures.next();

            if (config.isDebugCount() && count >= config.getDebugCount()) {
                break;
            }

            if (!config.isNoContextMap() && feature.getSeqLen() > MIN_CONTEXT_LENGTH_BASES) {
                populateContextMapCache(feature, basicGeneService);
            }

            @SuppressWarnings("unchecked")
            List<Feature> features = session.createQuery(
                "select fl.feature from FeatureLoc fl" +
                " where fl.sourceFeature = :feature")
            .setParameter("feature", feature).list();

            for (Feature f : features) {
                if (f instanceof AbstractGene) {
                    populateDtoCache((AbstractGene) f);
                }
            }

            session.clear();
            count++;
            logger.info(String.format("Count %d of %s : Total run time %.02fs",
                count, "unknown", (double)(System.currentTimeMillis() - start)/1000));
        }
    }

    private void populateCacheForGene(String geneUniqueName) {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        AbstractGene gene = (AbstractGene) session.createQuery(
            "select g from AbstractGene g" +
            " where g.uniqueName = :geneUniqueName")
        .setParameter("geneUniqueName", geneUniqueName)
        .uniqueResult();

        if (gene == null) {
            logger.error("Could not find gene with uniqueName '"
                + geneUniqueName + "'");
        } else {
            populateDtoCache(gene);
        }
    }

    private Iterator<Feature> getTopLevelFeatures() {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        Query q;

        if (config.isOrganism()) {
            q = session.createQuery(
                "select fp.feature" +
                " from FeatureProp fp" +
                " where fp.cvTerm.name = 'top_level_seq'" +
                " and fp.cvTerm.cv.name = 'genedb_misc'" +
                " and fp.feature.organism.commonName = :orgName")
            .setString("orgName", config.getOrganism());
        } else {
            q = session.createQuery(
                "select fp.feature" +
                " from FeatureProp fp" +
                " where fp.cvTerm.name='top_level_seq'" +
                " and fp.cvTerm.cv.name = 'genedb_misc'");
        }

        @SuppressWarnings("unchecked")
        Iterator<Feature> iterator = q.iterate();
        return iterator;
    }


    private void populateContextMapCache(Feature feature, BasicGeneService basicGeneService) {

        ContextMapDiagram chromosomeDiagram = ContextMapDiagram.forChromosome(basicGeneService,
            feature.getOrganism().getCommonName(), feature.getUniqueName(), feature.getSeqLen());

        RenderedContextMap renderedContextMap = (RenderedContextMap) renderedDiagramFactory.getRenderedDiagram(chromosomeDiagram);
        RenderedContextMap renderedChromosomeThumbnail = (RenderedContextMap) renderedDiagramFactory.getRenderedDiagram(chromosomeDiagram).asThumbnail(THUMBNAIL_WIDTH);

        List<RenderedContextMap.Tile> tiles = renderedContextMap.renderTiles(TILE_WIDTH);

        try {
            String metadata = contextMapMetadata(renderedChromosomeThumbnail, renderedContextMap, tiles);
            contextMapMap.put(feature.getUniqueName(), metadata);
            logger.info("Stored contextMap for '"+feature.getUniqueName()+"' as '"+metadata+"'");
        } catch (IOException exp) {
            logger.error(exp);
        }
    }

    private String contextMapMetadata(RenderedContextMap chromosomeThumbnail, RenderedContextMap contextMap,
            List<RenderedContextMap.Tile> tiles) throws IOException {
        String chromosomeThumbnailKey = diagramCache.fileForContextMap(chromosomeThumbnail);

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

        model.put("tilePrefix", "/Image?key=");
        model.put("tiles", tiles);

        Map<String,Object> chromosomeThumbnailModel = new HashMap<String,Object>();
        chromosomeThumbnailModel.put("src", chromosomeThumbnailKey);
        chromosomeThumbnailModel.put("width", chromosomeThumbnail.getWidth());
        model.put("chromosomeThumbnail", chromosomeThumbnailModel);

        JSON json =  JSONSerializer.toJSON(model);
        String text = json.toString(0);
        return text;
    }

    private void populateDtoCache(AbstractGene gene) {
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

        @Option(longName="gene")
        String getGeneUniqueName();
    }


    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

    public void setRenderedDiagramFactory(RenderedDiagramFactory renderedDiagramFactory) {
        this.renderedDiagramFactory = renderedDiagramFactory;
    }
}
