package org.genedb.db.loading;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Load orthologue data from a simple tab-separated file.
 * Each line of the file has five fields:
 * <ol>
 *   <li> Source organism,
 *   <li> source feature,
 *   <li> target organism,
 *   <li> target feature,
 *   <li> cluster name (may be missing or empty for unclustered orthologues)
 * </ol>
 * The source and target feature fields should contain the uniqueName of a transcript.
 *
 * @author rh11
 *
 */
public class LoadOrthologues extends FileProcessor {
    private static final Logger logger = Logger.getLogger(LoadOrthologues.class);
    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.ortho");

        new LoadOrthologues().processFileOrDirectory(inputDirectory, fileNamePattern);
    }

    private OrthologuesLoader loader;
    private LoadOrthologues() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = (OrthologuesLoader) applicationContext.getBean("orthologuesLoader", OrthologuesLoader.class);
    }

    @Override
    protected void processFile(File inputFile) throws IOException, ParsingException {
        OrthologueFile orthologueFile = new OrthologueFile(inputFile);
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
        private String cluster;

        private Line(int lineNumber, String line) throws ParsingException {
            this.lineNumber = lineNumber;

            String[] fields = line.split("\\t");
            if (fields.length != 4 && fields.length != 5) {
                throw new SyntaxError(file.toString(), lineNumber,
                    String.format("Wrong number of fields (%d)", fields.length));
            }

            sourceOrganism = fields[0];
            sourceFeature  = fields[1];
            targetOrganism = fields[2];
            targetFeature  = fields[3];

            if (fields.length == 5 && fields[4].length() > 0) {
                cluster = fields[4];
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

        String getCluster() {
            return cluster;
        }
    }
    private File file;
    private List<Line> lines = new ArrayList<Line>();

    public OrthologueFile(File file) throws IOException, ParsingException {
        this.file = file;
        BufferedReader br = new BufferedReader(new FileReader(file));
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

    public Iterable<Line> lines() {
        return lines;
    }
}

@Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
class OrthologuesLoader {
    private static final Logger logger = Logger.getLogger(OrthologuesLoader.class);

    private SequenceDao sequenceDao;

    public void load(OrthologueFile orthologueFile) throws DataError {
        /*
         * Thanks to the @Transactional annotation on this class,
         * Spring will automatically initiate a transaction when
         * we're called, which will be committed on successful
         * return or rolled back if we throw an exception.
         */

        for (OrthologueFile.Line line: orthologueFile.lines()) {
            logger.trace(String.format("%s:%s -> %s:%s (cluster %s)",
                line.getSourceOrganism(), line.getSourceFeature(),
                line.getTargetOrganism(), line.getTargetFeature(),
                line.getCluster()));

            Transcript source = getTranscript(line.getSourceOrganism(), line.getSourceFeature(),
                orthologueFile.file(), line.lineNumber);
            Transcript target = getTranscript(line.getTargetOrganism(), line.getTargetFeature(),
                orthologueFile.file(), line.lineNumber);
            String cluster = line.getCluster();
            if (cluster == null) {
                addOrthologue(source, target, orthologueFile.file(), line.lineNumber);
            } else {
                throw new RuntimeException("Clusters not yet supported"); // TODO
            }
        }
    }

    private Transcript getTranscript(String organism, String uniqueName, File file, int lineNumber) throws DataError {
        Transcript transcript = sequenceDao.getFeatureByUniqueName(uniqueName, Transcript.class);
        if (transcript == null) {
            throw new DataError(file, lineNumber, String.format("Could not find transcript feature '%s'", uniqueName));
        }
        if (!transcript.getOrganism().getCommonName().equals(organism)) {
            throw new DataError(file, lineNumber,
                String.format("The feature '%s' does not belong to organism '%s'",
                    uniqueName, organism));
        }
        return transcript;
    }

    private void addOrthologue(Transcript source, Transcript target, File file, int lineNumber) {
        if (!(source instanceof ProductiveTranscript)) {
            logger.error(String.format("%s line %d: The source transcript (%s) is non-coding. " +
                    "We can't currently store orthologues for non-coding transcripts",
                file, lineNumber, source.getUniqueName()));
            return;
        }
        Polypeptide sourcePolypeptide = ((ProductiveTranscript) source).getProtein();
        if (sourcePolypeptide == null) {
            logger.error(String.format("%s line %d: The source transcript (%s) has no associated polypeptide. Ignoring entry",
                file, lineNumber, source.getUniqueName()));
            return;
        }

        if (!(target instanceof ProductiveTranscript)) {
            logger.error(String.format("%s line %d: The target transcript (%s) is non-coding. " +
                    "We can't currently store orthologues for non-coding transcripts",
                file, lineNumber, target.getUniqueName()));
            return;
        }
        Polypeptide targetPolypeptide = ((ProductiveTranscript) target).getProtein();
        if (targetPolypeptide == null) {
            logger.error(String.format("%s line %d: The target transcript (%s) has no associated polypeptide. Ignoring entry",
                file, lineNumber, target.getUniqueName()));
            return;
        }

        targetPolypeptide.addOrthologue(sourcePolypeptide);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}

