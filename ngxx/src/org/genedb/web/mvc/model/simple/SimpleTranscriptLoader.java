package org.genedb.web.mvc.model.simple;

import org.genedb.web.mvc.model.load.TimerHelper;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 *
 * @author lo2@sangerinstitute
 *
 */
@Transactional
public class SimpleTranscriptLoader {

    public static void main(String args[]) throws Exception {

        setUpLogging();

        ConfigurableApplicationContext ctx =
                new ClassPathXmlApplicationContext(new String[] { "classpath:SimpleTranscriptLoader-context.xml" });
        SimpleTranscriptLoader transcriptLoader = ctx.getBean("simpleTranscriptLoader", SimpleTranscriptLoader.class);
        String[] orgs = { "Tbruceibrucei927", "Pfalciparum" };
        for (String org : orgs) {
            transcriptLoader.load(org, Integer.MAX_VALUE);
        }
    }

    /**
     * Set up logging
     */
    private static void setUpLogging() {
        // String log4jprops = "/log4j.TranscriptLoader.properties";
        // URL url = TranscriptLoader.class.getResource(log4jprops);
        // System.out.printf("Configuring Log4J from '%s'\n", url);
        // PropertyConfigurator.configure(url);
    }

    /**
     * Load transcripts of all organisms
     *
     * @param limit
     */
    // public int loadAll(int limit)throws Exception{
    // int loadCount = 0;
    // List<OrganismMapper> organisms = template.query(
    // OrganismMapper.GET_ALL_ORGANISMS_SQL, new OrganismMapper());
    // for(OrganismMapper organismMapper: organisms){
    // logger.info("Loading Organism: " + organismMapper.getCommonName());
    // loadCount = loadCount + load(organismMapper.getCommonName(), limit);
    // }
    // return loadCount;
    // }

    Logger logger = Logger.getLogger(SimpleTranscriptLoader.class);

    protected SimpleJdbcTemplate template;

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Find the genes for the given organism
     *
     * @param organismMapper
     * @param offset
     * @param limit
     * @return
     */
    private List<SimpleGene> findGenes(int organismId, int offset, int limit) {
        logger.info(String.format("Offset is %s and Limit is %s", offset, limit));

        // Create the mapper and get the genes
        List<SimpleGene> genes =
                template.query(SimpleGeneMapper.GET_GENES_SQL_WITH_LIMIT_AND_OFFSET_PARAMS, new SimpleGeneMapper(),
                        organismId, limit, offset);
        logger.info("Genes size: " + genes.size());

        for (SimpleGene gene : genes) {
            String parent =
                    template.queryForObject("select uniquename from feature where feature_id="
                            + gene.getSourceFeatureId(), String.class);
            gene.setTopLevelFeatureUniqueName(parent);
        }

        return genes;
    }

    /**
     * Find the transcript from the Gene
     *
     * @param geneMapper
     * @return
     */
    private List<SimpleTranscript> findTranscripts(SimpleGene gene) {
        // get the transcripts
        Date transcriptGetStartTime = new Date();
        List<SimpleTranscript> transcripts =
                template.query(SimpleTranscriptMapper.SQL_WITH_GENE_ID_PARAM, new SimpleTranscriptMapper(), gene
                        .getFeatureId());
        logger.info("Transcripts size: " + transcripts.size());
        TimerHelper.printTimeLapse(logger, transcriptGetStartTime, "transcriptGetStartTime");
        return transcripts;
    }

    private SimplePolypeptide initTranscriptProteinArguments(SimpleTranscript transcript) throws Exception {
        // Init the derived polypeptides details

        if ( !isProductiveTranscript(transcript)) {
            return null;
        }

        try {
            SimplePolypeptide ret =
                    template.queryForObject(SimplePolypeptideMapper.SQL, new SimplePolypeptideMapper(), transcript
                            .getFeatureId());
            try {
                String product =template.queryForObject(
                        "select c.name from cvterm c, feature_cvterm fc, cv cv where fc.feature_id="
                        + ret.getFeatureId()
                        + " and fc.cvterm_id=c.cvterm_id and c.cv_id=cv.cv_id and cv.name='genedb_products' limit 1",
                        String.class);
                ret.setProduct(product);
            }
            catch (EmptyResultDataAccessException exp) {
                System.err.println(String.format("Got no product for '%s' ('%d')", ret.getUniqueName(), ret.getFeatureId()));
                // No product - just carry on
            }
            return ret;
        }
        catch (Exception exp) {
            throw new RuntimeException("Failed to get result for '" + transcript.getFeatureId() + "'", exp);
        }
    }

    /**
     * Insert
     *
     * @param args
     * @return
     * @throws Exception
     */
    private int insertDenormalisedTranscript(SimpleGene gene, SimpleTranscript transcript, SimplePolypeptide protein,
            String commonName) throws Exception {
        logger.debug("Enter insertDenormalisedTranscript");
        Date startTime = new Date();

        int update = 0;

        int proteinId = 0;
        String proteinName = null;
        String product = null;
        if (protein != null) {
            proteinId = protein.getFeatureId();
            proteinName = quote(protein.getUniqueName());
            product = protein.getProduct();
            if (product != null) {
                if (product.length() > 120) {
                    product = product.substring(0, 120) + "...";
                }
                product = quote(product);
            }
        }

        String args =
                StringUtils.arrayToCommaDelimitedString(new Object[] { transcript.getFeatureId(),
                        quote(transcript.getCvtName()), quote(transcript.getUniqueName()), product,
                        gene.getFeatureId(), quote(gene.getUniqueName()), proteinId, proteinName,
                        quote(gene.getTopLevelFeatureUniqueName()), gene.getFmin(), quote(commonName) });
        // System.err.println(args);
        try {
            logger.info("Loading Transcript: " + transcript.getFeatureId());
            update =
                    template.update("insert into transcript_names(" + "transcript_id," + "transcript_cvterm_name,"
                            + "transcript_uniquename," + "product," + "gene_id," + "gene_uniquename," + "protein_id,"
                            + "protein_uniquename," + "top_level_feature_uniquename," + "fmin,"
                            + "organism_common_name" + ")" + " values(" + args + ")");
        }
        catch (Exception exp) {
            logger.error(args, exp);
            throw exp;
        }
        TimerHelper.printTimeLapse(logger, startTime, "insertDenormalisedTranscript");
        logger.debug("transcript loaded......");
        logger.debug("\n");
        return update;
    }

    private boolean isProductiveTranscript(SimpleTranscript transcript) {
        logger.debug("Enter isProductiveTranscript");
        if ("mRNA".equals(transcript.getCvtName()) || "pseudogenic_transcript".equals(transcript.getCvtName())) {
            return true;
        }
        return false;
    }

    /**
     * Choose organism to load
     *
     * @param organismName
     * @param limit
     * @param offset
     * @return rows loaded
     */
    private int load(String organismName, int limit) throws Exception {
        logger.debug(String.format("Enter load(%s)", organismName));
        Date startTime = new Date();

        // Get the organism
        int organismId =
                template.queryForInt("select organism_id from organism where common_name='" + organismName + "'");

        int loadCount = 0;
        int offset = 1;

        List<SimpleGene> genes = null;
        try {
            do {
                // Get the genes for this organism
                genes = findGenes(organismId, offset, limit);

                for (SimpleGene gene : genes) {
                    Date geneProcessingStartTime = new Date();

                    // get the transcripts
                    List<SimpleTranscript> transcripts = findTranscripts(gene);

                    // process transcript
                    loadCount += processTranscripts(gene, transcripts, organismName);

                    TimerHelper.printTimeLapse(logger, geneProcessingStartTime, "geneProcessingStartTime");
                }

                // increase the offset
                offset = offset + limit;

            } while (genes != null && limit <= genes.size());
        }
        catch (Exception e) {
            logger.info("Error: ", e);
            throw e;
        }
        finally {
            logger.info("Load Count: " + loadCount);
        }

        TimerHelper.printTimeLapse(logger, startTime, String.format("Exit load(%s)", organismName));
        return loadCount;
    }

    /**
     * Process each transcript derived from the gene
     *
     * @param organismMapper
     * @param topLevelFeatureMapper
     * @param geneMapper
     * @param transcriptMappers
     * @param isUpdate
     *            or insert (for this method call)
     * @return
     * @throws Exception
     */
    private int processTranscripts(SimpleGene gene, List<SimpleTranscript> simpleTranscripts, String commonName)
            throws Exception {
        int loadCount = 0;
        for (SimpleTranscript transcript : simpleTranscripts) {
            Date transcriptProcessingStartTime = new Date();

            logger.info("Adding..." + transcript.getFeatureId());

            // Init the derived polypeptides details
            SimplePolypeptide polypeptide = initTranscriptProteinArguments(transcript);

            // Insert into the transcript_cache table
            loadCount += insertDenormalisedTranscript(gene, transcript, polypeptide, commonName);

            logger.info("Added..." + transcript.getFeatureId());
            TimerHelper.printTimeLapse(logger, transcriptProcessingStartTime, "transcriptProcessingTime");
        }
        return loadCount;
    }

    private String quote(String in) {
        String out = in.replace("'", "`");
        return "'" + out + "'";
    }
}
