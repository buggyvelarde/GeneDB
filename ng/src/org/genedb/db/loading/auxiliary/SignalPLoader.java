package org.genedb.db.loading.auxiliary;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.Analysis;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignalPLoader extends Loader {
    private static final Logger logger = Logger.getLogger(SignalPLoader.class);

    String analysisProgramVersion;
    private Analysis analysis;

    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "signalp-version");
        return options;
    }

    @Override
    protected boolean processOption(String optionName, String optionValue) {

        if (optionName.equals("signalp-version")) {
            analysisProgramVersion = optionValue;
            return true;
        }
        return false;
    }


    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        loadTerms();

        if (analysisProgramVersion == null) {
            throw new IllegalArgumentException("Property load.analysis.programVersion is required");
        }

        // Add analysis
        analysis = new Analysis();
        analysis.setProgram("signalp");
        analysis.setProgramVersion(analysisProgramVersion);
        sequenceDao.persist(analysis);

        SignalPFile file = new SignalPFile(inputStream);

        int n=1;
        for (SignalPHit hit: file.hits()) {
            logger.info(String.format("[%d/%d] Processing prediction for '%s'", n++, file.hits().size(), hit.getKey()));

            loadHit(hit);

            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }
    }

    private CvTerm predictionTerm, peptideProbabilityTerm, anchorProbabilityTerm, plasmoAPScoreTerm;
    private void loadTerms() {
        predictionTerm = cvDao.getCvTermByNameAndCvName("SignalP_prediction", "genedb_misc");
        peptideProbabilityTerm = cvDao.getCvTermByNameAndCvName("signal_peptide_probability", "genedb_misc");
        anchorProbabilityTerm = cvDao.getCvTermByNameAndCvName("signal_anchor_probability", "genedb_misc");
        plasmoAPScoreTerm = cvDao.getCvTermByNameAndCvName("PlasmoAP_score", "genedb_misc");
    }

    private void loadHit(SignalPHit hit) {
        Polypeptide polypeptide = getPolypeptideByMangledName(hit.getKey());
        logger.debug(String.format("Processing hit of type '%s'", hit.getType()));

        if (polypeptide == null) {
            logger.error(String.format("Could not find polypeptide for key '%s'", hit.getKey()));
            return;
        }

        sequenceDao.persist(new FeatureProp(polypeptide, predictionTerm, hit.getType(), 0));
        sequenceDao.persist(new FeatureProp(polypeptide, peptideProbabilityTerm, hit.getPeptideProbability(), 0));
        if (hit.getAnchorProbability() != null) {
            sequenceDao.persist(new FeatureProp(polypeptide, anchorProbabilityTerm, hit.getAnchorProbability(), 0));
        }

        if (hit.getType().equals("Signal peptide")) {

            SignalPeptide signalPeptide = sequenceDao.createSignalPeptide(polypeptide, hit.getCleavageSiteAfter(),
                    hit.getCleavageSiteProbability(), analysis);
            sequenceDao.persist(signalPeptide);
        }
        
        /* Add the plasmoAP score (if available) */

        if (hit.getPlasmoAP_score()!=null){
            
            sequenceDao.persist(new FeatureProp(polypeptide, plasmoAPScoreTerm, hit.getPlasmoAP_score(), 0));            
        }
    }
}

class SignalPFile {
    private static final Logger logger = Logger.getLogger(SignalPFile.class);

    private List<SignalPHit> hits = new ArrayList<SignalPHit>();

    public SignalPFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String previousLine = null, line;
        while (null != (line = reader.readLine())) {
            if (line.startsWith("Prediction: ")) {
                if (previousLine == null) {
                    throw new IllegalStateException();
                }
                StringBuilder sb = new StringBuilder(previousLine);
                sb.append('\n');
                sb.append(line);
                sb.append('\n');

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

    public Collection<SignalPHit> hits() {
        return hits;
    }

    private static final Pattern SUMMARY_PATTERN = Pattern.compile(
            ">(.*)\n"+
            "Prediction: (Non-secretory protein|Signal peptide|Signal anchor)\n"+
            "Signal peptide probability: (\\d\\.\\d{3})\n"+
            "(?:Signal anchor probability: (\\d\\.\\d{3})\n)?"+
            "Max cleavage site probability: (\\d\\.\\d{3}) between pos\\. (-1|\\d+) and  ?(\\d+)\n" +
            "(?:PlasmoAP_score:\\s+(\\d+)\n)?" 
            );
    private void parseSummary(CharSequence summary) {
        Matcher matcher = SUMMARY_PATTERN.matcher(summary);
        if (matcher.matches()) {
            String key  = matcher.group(1);
            String type = matcher.group(2);

            if (type.equals("Non-secretory protein")) {
                return;
            }
            String peptideProbability = matcher.group(3);
            String anchorProbability  = matcher.group(4);
            String cleavageSiteProbability = matcher.group(5);
            int cleavageSiteAfter = Integer.parseInt(matcher.group(7));
            /* PlasmoAP score will be available if signalp was run with the -s option */
            String plasmoAP_score = matcher.group(8); 

            hits.add(new SignalPHit(key, type, peptideProbability, anchorProbability, cleavageSiteProbability,  plasmoAP_score,
                    cleavageSiteAfter));
        }
        else {
            logger.error("Failed to parse summary:\n" + summary);
        }
    }
}

class SignalPHit {
    private String key, type, peptideProbability, anchorProbability, cleavageSiteProbability, plasmoAP_score;
    int cleavageSiteAfter;

    public SignalPHit(String key, String type,
            String peptideProbability, String anchorProbability,
            String cleavageSiteProbability, String plasmoAP_score, int cleavageSiteAfter) {
        this.key = key;
        this.type = type;
        this.peptideProbability = peptideProbability;
        this.anchorProbability = anchorProbability;
        this.cleavageSiteProbability = cleavageSiteProbability;
        this.cleavageSiteAfter = cleavageSiteAfter;
        this.plasmoAP_score = plasmoAP_score;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getPeptideProbability() {
        return peptideProbability;
    }

    public String getAnchorProbability() {
        return anchorProbability;
    }

    public String getCleavageSiteProbability() {
        return cleavageSiteProbability;
    }

    public int getCleavageSiteAfter() {
        return cleavageSiteAfter;
    }
    
    public String getPlasmoAP_score(){
        return plasmoAP_score;
    }
}
