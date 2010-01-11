package org.genedb.db.loading.auxiliary;

import org.gmod.schema.feature.HelixTurnHelix;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.mapped.Analysis;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to load helix-turn-helix features. The results are expected in a certain format specified by the pattern object below.
 * TODO: Add the capability to handle version number when version is known
 *
 * @author nds
 *
 */


public class HTHLoader extends Loader {
    private static final Logger logger = Logger.getLogger(HTHLoader.class);
    private int number = 0;
    private String analysisProgramVersion = "unknown"; //Cannot be null in database; get right version number when known; update code to handle commandline args
    private Analysis analysis;
    boolean notFoundNotFatal = false;

    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "hth-version", "not-found-not-fatal");
        return options;
    }

    @Override
    protected boolean processOption(String optionName, String optionValue) {
       if (optionName.equals("hth-version")) {
            analysisProgramVersion = optionValue;
            return true;
        }
       else if (optionName.equals("not-found-not-fatal")) {
           if (!optionValue.equals("true") && !optionValue.equals("false")) {
               return false;
           }
           notFoundNotFatal = Boolean.valueOf(optionValue);
           return true;
       }
        return false;
    }


    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        // Add analysis
      	analysis = new Analysis();
      	analysis.setProgram("helixturnhelix");
      	analysis.setProgramVersion(analysisProgramVersion);
      	sequenceDao.persist(analysis);
      	HTHFile file = new HTHFile(inputStream);

        int n=1;
        for (HTHHit hit: file.hits()) {
            logger.info(String.format("[%d/%d] Processing helix-turn-helix for '%s'", n++, file.hits().size(), hit.getName()));
            loadHit(hit);
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }

    }


    private void loadHit(HTHHit hit) {
        Polypeptide polypeptide = getPolypeptideByMangledName(hit.getName());
        logger.debug(String.format("Processing feature of name '%s'", hit.getName()));

        if (polypeptide == null) {

            if (notFoundNotFatal) {
                logger.error(String.format("Could not find polypeptide for key '%s'", hit.getName()));
                return;
            }
            else {
                throw new RuntimeException(String.format("Could not find polypeptide for key '%s'", hit.getName()));
            }
        }else {
            number++;
        }

        //All hits should be of type helix-turn-helix at this stage.
        //The createHelixTurnHelix method takes all the essential information from a hit and creates the corresponding feature & featureloc
        HelixTurnHelix helixTurnHelix = sequenceDao.createHelixTurnHelix(polypeptide, hit.getStart(), hit.getEnd(),  hit.getScore(), hit.getMaxScoreAt(), hit.getStdDeviations(), analysis);
        sequenceDao.persist(helixTurnHelix);

    }

    @Transactional
    public void clear(final String organismCommonName, final String analysisProgram) throws HibernateException, SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        session.doWork(new Work() {
               public void execute(Connection connection) throws SQLException {
                    new ClearHTH(connection, organismCommonName, analysisProgram).clear();
               }
        });
    }



}

/* Class corresponding to HTH file */

class HTHFile {
    private static final Logger logger = Logger.getLogger(HTHFile.class);

    private List<HTHHit> hits = new ArrayList<HTHHit>();

    public HTHFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String previousLine = null, line;
        while (null != (line = reader.readLine())) { //While not end of file
            if (line.startsWith("Feature: ")) {
                if (previousLine == null) {
                    throw new IllegalStateException();
                }
                StringBuilder sb = new StringBuilder(previousLine);
                while (0 < (line = reader.readLine()).length()) {
                    sb.append(line);
                    sb.append('\n');
                }
                logger.trace(sb);
                parseSummary(sb);
            }

            previousLine = line;
        }
    }

    public Collection<HTHHit> hits() {
        return hits;
    }

    private static final Pattern SUMMARY_PATTERN = Pattern.compile(
        "Name: (\\S+)\n"+
        "Start: (\\d+)\n" +
        "End: (\\d+)\n" +
        "Length: (\\d+)\n" +
        "Score: (\\d+\\.\\d+)\n" +
        "Strand: (\\S)\n" +
        "Maximum_score_at: (\\d+)\n" +
        "Standard_deviations: (\\d+\\.\\d+)\n"

    );


    private void parseSummary(CharSequence summary) {

        Matcher matcher = SUMMARY_PATTERN.matcher(summary);
        if (matcher.matches()) {
            String name  = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            int end = Integer.parseInt(matcher.group(3));
            int length = Integer.parseInt(matcher.group(4));
            String score = matcher.group(5);
            String strand = matcher.group(6);
            int maxScoreAt = Integer.parseInt(matcher.group(7));
            String stdDeviations = new Double(matcher.group(8)).toString();

            hits.add(new HTHHit(name, start, end, length, score, strand, maxScoreAt, stdDeviations));

        }
        else {
            logger.error("Failed to parse summary:\n" + summary);
        }

    }
}

/* Each 'hit' corresponds to a paragraph beginning with the word 'Feature' in the .hth file */
class HTHHit {

    private String name, strand,score, stdDeviations;
    private int start, end, length, maxScoreAt;

    public HTHHit(String name, int start, int end, int length, String score, String strand, int maxScoreAt, String stdDeviations) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.length = length;
        this.score = score;
        this.strand = strand;
        this.maxScoreAt = maxScoreAt;
        this.stdDeviations = stdDeviations;
    }

    public String getName() {
        return name;
    }

    public String getStrand() {
        return strand;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return length;
    }

    public int getMaxScoreAt() {
        return maxScoreAt;
    }

    public String getStdDeviations() {
        return stdDeviations;
    }

    public String getScore() {
        return score;
    }





}
