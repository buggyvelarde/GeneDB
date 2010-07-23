package org.genedb.db.loading;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.ProteinMatch;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load orthologue data from a simple tab-separated file.
 * Each line of the file has four or five fields:
 * <ol>
 *   <li> Source organism,
 *   <li> source feature,
 *   <li> target organism or <code>cluster</code>,
 *   <li> target feature or cluster name,
 *   <li> the percentage identity (optional)
 * </ol>
 * The source feature field should contain the uniqueName of a transcript
 * or polypeptide.
 * For a simple orthologue, the third and fourth fields contain the name of the
 * target organism and target feature (transcript or polypeptide);
 * for orthologue clusters, the third field contains the word "cluster"
 * and the fourth field the name of the cluster.
 * <p>
 * Details of the analysis should be specified as properties:
 * <dl>
 * <dt><code>load.dataset</code></dt><dd>The name of the orthologue dataset being loaded, e.g. "Plasmodium" (Required)</dd>
 * <dt><code>load.analysis.program</code></dt><dd>The program used to perform the analysis (Required for automatic predictions)</dd>
 * <dt><code>load.analysis.programVersion</code></dt><dd>The version of the program that was used (Defaults to "unknown")</dd>
 * <dt><code>load.analysis.algorithm</code></dt><dd>The name of the algorithm (Optional)</dd>
 * </dl>
 * If no analysis program is specified, load.orthologues.manual must be specified to indictate that the file contains manually curated orthologues.
 * <p>
 * There are also a couple of optional properties that control the behaviour of the loader:
 * <dl>
 * <dt><code>load.orthologues.notFoundNotFatal</code></dt><dd>If this property is set, it is not a fatal error if a gene mentioned
 *           in the input file does not exist. This can be useful when gene models have been deleted or renamed since the orthologue
 *           data were generated.</dd>
 * <dt><code>load.orthologues.geneNames</
 * code></dt><dd>Treat the names in the input file as gene names, rather than polypeptide
 *           or transcript names.</dd>
 * <dt><code>load.analysis.paralogues</code></dt><dd>Load as paralogues not orthologues. NB: Paralogues will overwrite orthologues with the same dataset name.</dd>
 * <dt><code>load.orthologues.manual</code></dt><dd>Load as manually curated orthologues. load.analysisProgram is no longer required.</dd>
 * </dl>
 *
 * @author rh11
 *
 */
public class LoadOrthologues extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadOrthologues.class);
    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException, SQLException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.ortho");

        String analysisProgram = getPropertyWithDefault("load.analysis.program", null);
        String analysisProgramVersion = getPropertyWithDefault("load.analysis.programVersion", null);
        String analysisAlgorithm = getPropertyWithDefault("load.analysis.algorithm", null);

        String datasetName = getPropertyWithDefault("load.dataset", null);

        boolean geneNames = hasProperty("load.orthologues.geneNames");
        boolean notFoundNotFatal = hasProperty("load.orthologues.notFoundNotFatal");
        boolean loadAsParalogues = hasProperty("load.orthologues.paralogues");
        boolean manualOrthologues = hasProperty("load.orthologues.manual");
        
        if (analysisProgram == null) {
            if (analysisProgramVersion != null) {
                throw new IllegalArgumentException("load.analysis.programVersion is specified, but load.analysis.program is not");
            }
            if (analysisAlgorithm != null) {
                throw new IllegalArgumentException("load.analysis.algorithm is specified, but load.analysis.program is not");
            }
            if (manualOrthologues == false) {
            	 throw new IllegalArgumentException("You must either specify load.analysis.program (for automatic predictions) or load.orthologues.manual (for manual orthologues)");
            }
        } else {
            analysisProgramVersion = "unknown";
        }

        LoadOrthologues loadOrthologues = new LoadOrthologues(datasetName);
        loadOrthologues.setAnalysisProperties(analysisProgram, analysisProgramVersion, analysisAlgorithm);
        loadOrthologues.setGeneNames(geneNames);
        loadOrthologues.setNotFoundNotFatal(notFoundNotFatal);
        loadOrthologues.setLoadAsParalogues(loadAsParalogues);
        loadOrthologues.processFileOrDirectory(inputDirectory, fileNamePattern);
    }

	private final OrthologuesLoader loader;
    private LoadOrthologues(String datasetName) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = applicationContext.getBean("orthologuesLoader", OrthologuesLoader.class);
        loader.setDatasetName(datasetName);
    }

    private void setAnalysisProperties(String analysisProgram, String analysisProgramVersion, String analysisAlgorithm) {
        loader.setAnalysisProperties(analysisProgram, analysisProgramVersion, analysisAlgorithm);
    }

    private void setGeneNames(boolean geneNames) {
        loader.setGeneNames(geneNames);
    }

    private void setNotFoundNotFatal(boolean notFoundNotFatal) {
        loader.setNotFoundNotFatal(notFoundNotFatal);
    }

	private void setLoadAsParalogues(boolean loadAsParalogues) {
		loader.setLoadAsParalogues(loadAsParalogues);
	}
	
    @Override
    protected void processFile(File inputFile, Reader reader) throws IOException, ParsingException {
        OrthologueFile orthologueFile = new OrthologueFile(inputFile, reader);
        loader.load(orthologueFile);
    }
}

class OrthologueFile {
    class Line {
        int lineNumber;
        private String sourceOrganism;
        private String sourceFeature;
        private String targetOrganism;
        private String targetFeature;
        private Double identity = null;

        private Line(int lineNumber, String line) throws ParsingException {
            this.lineNumber = lineNumber;

            String[] fields = line.split("\\t");
            if (fields.length != 4 && fields.length != 5) {
                throw new SyntaxError(file, lineNumber,
                    String.format("Wrong number of fields (%d)", fields.length));
            }

            sourceOrganism = fields[0];
            sourceFeature  = fields[1];
            targetOrganism = fields[2];
            targetFeature  = fields[3];

            if (fields.length > 4 && fields[4].length() > 0) {
                try {
                    identity = Double.parseDouble(fields[4]);
                } catch (NumberFormatException e) {
                    throw new SyntaxError(file, lineNumber,
                        String.format("Could not parse identity field '%s'", fields[4]));
                }
                if (identity < 0 || identity > 100) {
                    throw new DataError(file, lineNumber,
                        String.format("Value of identity field '%s' is out of range", fields[4]));
                }
            }
        }

        String getSourceOrganism() {
            return sourceOrganism;
        }

        String getSourceFeature() {
            return sourceFeature;
        }

        String getTargetOrganism() {
            return targetOrganism;
        }

        String getTargetFeature() {
            return targetFeature;
        }

        Double getIdentity() {
            return identity;
        }
    }
    private File file;
    private List<Line> lines = new ArrayList<Line>();

    public OrthologueFile(File file, Reader reader) throws IOException, ParsingException {
        this.file = file;
        BufferedReader br = new BufferedReader(reader);
        String line;
        int lineNumber = 0;
        while (null != (line = br.readLine())) {
            lineNumber ++;
            lines.add(new Line(lineNumber, line));
        }
    }

    public File file() {
        return file;
    }

    public List<Line> lines() {
        return lines;
    }

    public int numberOfLines() {
        return lines.size();
    }
}

@Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
class OrthologuesLoader {
    private static final Logger logger = Logger.getLogger(OrthologuesLoader.class);

    private static final int BATCH_SIZE = 50;

    private SequenceDao sequenceDao;
    private OrganismDao organismDao;
    private SessionFactory sessionFactory;

    private Organism dummyOrganism;
    private Analysis analysis = null;
    private boolean geneNames = false;
    private boolean notFoundNotFatal = false;
	private boolean loadAsParalogues = false;
	
    private String datasetName;

    public void setAnalysisProperties(String analysisProgram, String analysisProgramVersion, String analysisAlgorithm) {
        if (analysisProgram == null) {
            return;
        }

        analysis = new Analysis();
        analysis.setProgram(analysisProgram);
        analysis.setProgramVersion(analysisProgramVersion);
        analysis.setAlgorithm(analysisAlgorithm);
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * If the geneNames property is set, then the identifiers in the file
     * are assumed to refer to genes rather than transcripts. This only
     * makes sense for bacteria, where the gene name uniquely identifies
     * a transcript.
     *
     * @param geneNames
     */
    void setGeneNames(boolean geneNames) {
        this.geneNames = geneNames;
    }

    void setNotFoundNotFatal(boolean notFoundNotFatal) {
        this.notFoundNotFatal = notFoundNotFatal;
    }
    
    void setLoadAsParalogues(boolean loadAsParalogues) {
    	this.loadAsParalogues  = loadAsParalogues;	
	}
    
    public void load(OrthologueFile orthologueFile) throws DataError {

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        dummyOrganism = organismDao.getOrganismByCommonName("dummy");

        if (analysis != null) {
            persistAnalysis();
        }

        Map<String,Collection<Integer>> clustersByName = new HashMap<String,Collection<Integer>>();

        int numberOfLines = orthologueFile.numberOfLines();
        for (OrthologueFile.Line line: orthologueFile.lines()) {
            logger.trace(String.format("[%d/%d] %s:%s -> %s:%s",
                line.lineNumber, numberOfLines,
                line.getSourceOrganism(), line.getSourceFeature(),
                line.getTargetOrganism(), line.getTargetFeature()));

            processLine(orthologueFile.file(), line, clustersByName);

            if (line.lineNumber % BATCH_SIZE == 0) {
                /* If we don't clear the session regularly,
                 * it becomes impossibly slow after a while.
                 */
                logger.trace("Flushing and clearing session");
                session.flush();
                session.clear();
            }
        }

        loadClusters(clustersByName);
    }

    private void persistAnalysis() {
        SessionFactoryUtils.getSession(sessionFactory, false).persist(analysis);
    }

    private void processLine(File file, OrthologueFile.Line line,
            Map<String, Collection<Integer>> clustersByName) throws DataError
    {
        Polypeptide source = getPolypeptide(line.getSourceOrganism(), line.getSourceFeature(),
            file, line.lineNumber);
        if (source == null) {
            // A return value of null indicates a non-fatal error
            return;
        }

        String targetOrganismName = line.getTargetOrganism();
        if (targetOrganismName.equalsIgnoreCase("cluster")) {
            String cluster = line.getTargetFeature();
            if (!clustersByName.containsKey(cluster)) {
                clustersByName.put(cluster, new ArrayList<Integer>());
            }
            clustersByName.get(cluster).add(source.getFeatureId());
        } else {
            // Unclustered
            Polypeptide target = getPolypeptide(line.getTargetOrganism(), line.getTargetFeature(),
                file, line.lineNumber);
	    if (target != null) {
	    	addOrthologue(source, target, line.getIdentity());
	    }
        }
    }

    private void loadClusters(Map<String, Collection<Integer>> clustersByName) {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        int n = 0;

        for (Map.Entry<String, Collection<Integer>> entry: clustersByName.entrySet()) {
            String clusterName = entry.getKey();
            Collection<Integer> polypeptideIds = entry.getValue();

            loadCluster(clusterName, polypeptideIds, null, this.loadAsParalogues);

            if (++n % BATCH_SIZE == 0) {
                /* If we don't clear the session regularly,
                 * it becomes impossibly slow after a while.
                 */
                logger.trace("Flushing and clearing session");
                session.flush();
                session.clear();
            }
        }
    }

    private Polypeptide getPolypeptide(String organism, String uniqueName, File file, int lineNumber) throws DataError {
        Transcript transcript;

        if (geneNames) {
            AbstractGene gene = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(uniqueName, organism, AbstractGene.class);
            if (gene == null) {
                String errorMessage = String.format("Could not find gene '%s' in organism '%s'", uniqueName, organism);
                if (notFoundNotFatal) {
                    logger.error(errorMessage);
                    return null;
                }
                throw new DataError(errorMessage);
            }
            Collection<Transcript> transcripts = gene.getTranscripts();
            if (transcripts.isEmpty()) {
                logger.error(String.format("The gene '%s' does not have any transcripts", uniqueName));
                return null;
            }
            if (transcripts.size() > 1) {
                logger.error(String.format("The gene '%s' has %d transcripts", uniqueName, transcripts.size()));
                return null;
            }
            transcript = transcripts.iterator().next();
        } else {
            transcript = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(uniqueName, organism, Transcript.class);
            if (transcript == null) {
                Polypeptide polypeptide = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(uniqueName, organism, Polypeptide.class);
                if (polypeptide != null) {
                    return polypeptide;
                }

                String errorMessage = String.format("Could not find transcript feature '%s' in organism '%s'", uniqueName, organism);
                if (notFoundNotFatal) {
                    logger.error(errorMessage);
                    return null;
                }
                throw new DataError(file, lineNumber, errorMessage);
            }
        }

        if (!(transcript instanceof ProductiveTranscript)) {
            logger.error(String.format("%s line %d: The transcript (%s) is non-coding. " +
                    "We can't currently store orthologues for non-coding transcripts",
                file, lineNumber, transcript.getUniqueName()));
            return null;
        }
        Polypeptide polypeptide = ((ProductiveTranscript) transcript).getProtein();
        if (polypeptide == null) {
            logger.error(String.format("%s line %d: The transcript (%s) has no associated polypeptide. Ignoring entry",
                file, lineNumber, transcript.getUniqueName()));
            return null;
        }

        return polypeptide;
    }

    private void addOrthologue(Polypeptide sourcePolypeptide, Polypeptide targetPolypeptide, Double identity) {
        if (this.analysis != null) {
            if (datasetName == null) {
                throw new NullPointerException("The datasetName is null - did you call setDatasetName?");
            }
            // Since this is an algorithmically-derived orthologue, we add it as a cluster
            String clusterName = String.format("%s: %s -> %s", datasetName,
                sourcePolypeptide.getUniqueName(), targetPolypeptide.getUniqueName());
            List<Integer> polypeptideIds = new ArrayList<Integer>(2);
            Collections.addAll(polypeptideIds, sourcePolypeptide.getFeatureId(), targetPolypeptide.getFeatureId());
            loadCluster(clusterName, polypeptideIds, identity, this.loadAsParalogues);
        } else {
            // Manually-curated orthologue. Add a simple orthologous_to/paralogous_to relationship in both directions.
            Session session = SessionFactoryUtils.getSession(sessionFactory, false);
            if (this.loadAsParalogues == true) {
            	session.persist(sourcePolypeptide.addParalogue(targetPolypeptide));
            	session.persist(targetPolypeptide.addParalogue(sourcePolypeptide));
            } else {
            	session.persist(sourcePolypeptide.addOrthologue(targetPolypeptide));
            	session.persist(targetPolypeptide.addOrthologue(sourcePolypeptide));           	            
            }
        }
    }

    private void loadCluster(String clusterName, Collection<Integer> polypeptideIds, Double identity, Boolean loadAsParalogues) {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        if (datasetName == null) {
            throw new NullPointerException("The datasetName is null - did you call setDatasetName?");
        }

        String clusterUniqueName = String.format("%s:%s", datasetName, clusterName);

        logger.trace(String.format("Loading orthologue cluster '%s' as '%s'", clusterName, clusterUniqueName));
        ProteinMatch clusterFeature = new ProteinMatch(dummyOrganism, clusterUniqueName, true, false);
        if (analysis != null) {
        	AnalysisFeature analysisFeature = clusterFeature.createAnalysisFeature(analysis);
        	analysisFeature.setIdentity(identity);
        }
        
        session.persist(clusterFeature);

        for (int polypeptideId: polypeptideIds) {
            logger.trace("Loading polypeptide " + polypeptideId);
            Polypeptide p = (Polypeptide) session.load(Polypeptide.class, polypeptideId);

            FeatureRelationship fr;
            if (loadAsParalogues == true) {
            	logger.trace("Adding paralogue to cluster");
            	fr = clusterFeature.addParalogue(p);
            }
            else {
            	logger.trace("Adding orthologue to cluster");
            	fr = clusterFeature.addOrthologue(p);        
            }
            
            logger.trace("Persisting FeatureRelationship");
            session.persist(fr);
        }
    }

    /* Spring setters */
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }
}

