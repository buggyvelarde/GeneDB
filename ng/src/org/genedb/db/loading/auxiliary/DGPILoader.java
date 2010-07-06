package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.gmod.schema.feature.Polypeptide;

import org.hibernate.Session;


public class DGPILoader extends Loader {
    private static final Logger logger = Logger.getLogger(DGPILoader.class);

    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        //Transaction transaction = session.getTransaction();

        DGPIFile file = new DGPIFile(inputStream);
        int n=1;
        for (String key: file.keys()) {
             logger.info(String.format("[%d/%d] Loading DGPI results for key '%s'", n++, file.keys().size(), key));
             
             Polypeptide polypeptide = getPolypeptideByMangledName(key);
            if (polypeptide == null) {
                logger.error(String.format("Could not find polypeptide '%s'", key));
                continue;
            }

            //transaction.begin(); 
            loadResult(polypeptide, file.resultForKey(key));
            //transaction.commit();
            /*
             * If the session isn't cleared out every so often, it
             * starts to get pretty slow after a while if we're loading
             * a large file. It's important that this come immediately
             * after a flush. (Commit will trigger a flush unless you've
             * set FlushMode.MANUAL, which we assume you haven't.)
             */
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }

        }
    }

    private void loadResult(Polypeptide polypeptide, DGPIResult result) {
        logger.debug(String.format("Processing result for '%s'", polypeptide.getUniqueName()));
        if (result.isAnchored()) {
            addAnchoredProperty(polypeptide);
        }
        if (result.getBestCleavageSite() >= 0)
            addCleavageSite(polypeptide, result.getBestCleavageSite(), result.getCleavageSiteScore());
    }

    private void addCleavageSite(Polypeptide polypeptide, int bestCleavageSite, String cleavageSiteScore) {
        logger.debug(String.format("Adding cleavage site at %d (score=%s)", bestCleavageSite, cleavageSiteScore));
        sequenceDao.persist(sequenceDao.createGPIAnchorCleavageSite(polypeptide, bestCleavageSite, cleavageSiteScore));
    }

    private void addAnchoredProperty(Polypeptide polypeptide) {
        logger.debug("Setting the 'DGPI anchored' property");
        sequenceDao.persist(sequenceDao.createGPIAnchoredProperty(polypeptide));
    }
}

class DGPIFile {
    private Map<String, DGPIResult> resultsByKey = new HashMap<String, DGPIResult>();

    public DGPIFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line, key = null;
        List<String> resultLines = new ArrayList<String>();
        while (null != (line = reader.readLine())) {
           
            if (line.startsWith(">")) {
                
                if (key != null) {
                    DGPIResult result = DGPIResult.parseLines(resultLines);
                    if (result != null) {
                        resultsByKey.put((key.substring(1)).trim(), result);
                    }
                }

                key = line;
                resultLines = new ArrayList<String>();
            }
            else {
                resultLines.add(line);
            }
        }
        // Don't forget the last one!
        DGPIResult result = DGPIResult.parseLines(resultLines);
        if (result != null) {
            resultsByKey.put((key.substring(1)).trim(), result);
        }
    }

    public Collection<String> keys() {
        return resultsByKey.keySet();
    }
    public DGPIResult resultForKey(String key) {
        return resultsByKey.get(key);
    }
}

class DGPIResult {
    private boolean anchored = false;
    private int bestCleavageSite = -1;
    private String cleavageSiteScore;

    private static final Pattern CLEAVAGE_SITE_PATTERN
        = Pattern.compile("\\s*There's a potential cleavage site at (\\d+) \\(score=(\\d+\\.\\d+)\\).*");

    private static final Pattern BEST_CLEAVAGE_SITE_PATTERN
        = Pattern.compile("\\s*The best cleavage site is (\\d+)");

    private static final Pattern IS_GPI_ANCHORED_PATTERN
        = Pattern.compile("\\s*This protein is GPI-anchored.*");


    private enum State {NONE, CLEAVAGE_SITE, CONCLUSION};
    public static DGPIResult parseLines (List<String> lines) {
        DGPIResult ret = new DGPIResult();
        Map<String,String> cleavageSiteScoreByLocation = new HashMap<String,String>();
        State state = State.NONE;
        for (String line: lines) {
            if (line.startsWith("Cleavage site")) {
                assert state == State.NONE;
                state = State.CLEAVAGE_SITE;
            }
            else if (line.startsWith("Conclusion")) {
                assert state == State.CLEAVAGE_SITE;
                state = State.CONCLUSION;
            }
            else {
                switch (state) {
                case CLEAVAGE_SITE:
                    Matcher cleavageSiteMatcher = CLEAVAGE_SITE_PATTERN.matcher(line);
                    if (cleavageSiteMatcher.matches())
                        cleavageSiteScoreByLocation.put(cleavageSiteMatcher.group(1), cleavageSiteMatcher.group(2));

                    Matcher bestCleavageSiteMatcher = BEST_CLEAVAGE_SITE_PATTERN.matcher(line);
                    if (bestCleavageSiteMatcher.matches()) {
                        String bestCleavageSiteString = bestCleavageSiteMatcher.group(1);
                        ret.bestCleavageSite = Integer.parseInt(bestCleavageSiteString);
                        if (!cleavageSiteScoreByLocation.containsKey(bestCleavageSiteString))
                            throw new RuntimeException("Failed to parse DGPI result");
                        ret.cleavageSiteScore = cleavageSiteScoreByLocation.get(bestCleavageSiteString);
                    }
                    break;
                case CONCLUSION:
                    Matcher isAnchoredMatcher = IS_GPI_ANCHORED_PATTERN.matcher(line);
                    if (isAnchoredMatcher.matches())
                        ret.anchored = true;
                    break;
                case NONE:
                    /* Nothing need be done */
                    break;
                }
            }
        }

        if (ret.anchored || ret.bestCleavageSite > -1)
            return ret;
        else
            return null;
    }
    public boolean isAnchored() {
        return anchored;
    }
    public int getBestCleavageSite() {
        return bestCleavageSite;
    }
    public String getCleavageSiteScore() {
        return cleavageSiteScore;
    }
    @Override
    public String toString(){
        return "Cleavage site at " + getBestCleavageSite() + " with a score of " + getCleavageSiteScore() + " GPI anchored: " + isAnchored();
    
    }
}