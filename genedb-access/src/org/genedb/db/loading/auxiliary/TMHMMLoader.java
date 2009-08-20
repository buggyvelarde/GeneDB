package org.genedb.db.loading.auxiliary;

import org.gmod.schema.feature.MembraneStructure;
import org.gmod.schema.feature.Polypeptide;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TMHMMLoader extends Loader {

    private static Logger logger = Logger.getLogger(TMHMMLoader.class);

    String analysisProgramVersion = "unknown";
    Boolean notFoundNotFatal = false;

    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "tmhmm-version", "not-found-not-fatal");
        return options;
    }
    @Override
    protected boolean processOption(String optionName, String optionValue) {

    	if (optionName.equals("tmhmm-version")) {
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
       	Analysis analysis = new Analysis();
    	analysis.setProgram("tmhmm");
    	analysis.setProgramVersion(analysisProgramVersion);
    	sequenceDao.persist(analysis);

        TMHMMFile file = new TMHMMFile(inputStream);
        int n=1;
        for (String key: file.keys()) {

            logger.info(String.format("[%d/%d] Loading helices for key '%s'", n++, file.keys().size(), key));

            Polypeptide polypeptide = getPolypeptideByMangledName(key);
            if (polypeptide == null) {

                if (notFoundNotFatal) {
                    String errorMessage = String.format("Failed to find polypeptide '%s'", key);
                    logger.error(errorMessage);
                }
                else {
                    throw new RuntimeException(String.format("Failed to find polypeptide '%s'", key));
                }
                continue;
            }


            loadMembraneStructure(polypeptide, file.regionsForKey(key), analysis);
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

    private void loadMembraneStructure(Polypeptide polypeptide, Iterable<TMHMMRegion> regions, Analysis analysis) {
        logger.debug(String.format("Creating membrane structure region for '%s'", polypeptide.getUniqueName()));
        MembraneStructure membraneStructure = sequenceDao.createMembraneStructure(polypeptide);
    	// Add analysisfeature
    	if (analysis != null) {
    	    membraneStructure.createAnalysisFeature(analysis);
    	}
        sequenceDao.persist(membraneStructure);

        for (TMHMMRegion region: regions) {
            loadRegion(membraneStructure, region);
        }
    }

    private void loadRegion(MembraneStructure membraneStructure, TMHMMRegion region) {
        logger.debug(String.format("Adding membrane structure subregion (%s) for '%s' at %d-%d",
            region.getType(), region.getKey(), region.getFmin(), region.getFmax()));

        switch(region.getType()) {
        case INSIDE:
            sequenceDao.persist(sequenceDao.createCytoplasmicRegion(membraneStructure,
                region.getFmin(), region.getFmax()));
            break;
        case TMHELIX:
            sequenceDao.persist(sequenceDao.createTransmembraneRegion(membraneStructure,
                region.getFmin(), region.getFmax()));
            break;
        case OUTSIDE:
            sequenceDao.persist(sequenceDao.createNonCytoplasmicRegion(membraneStructure,
                region.getFmin(), region.getFmax()));
            break;
        }
    }
}

class TMHMMRegion {
    public enum Type {
        INSIDE, TMHELIX, OUTSIDE;
        public static Type decode(String typeString) {
            if (typeString.equals("inside")) {
                return INSIDE;
            }
            else if (typeString.equals("TMhelix")) {
                return TMHELIX;
            }
            else if (typeString.equals("outside")) {
                return OUTSIDE;
            }
            else {
                throw new IllegalArgumentException(String.format("Unrecognised type '%s'", typeString));
            }
        }
    };
    private String key;
    private Type type;
    private int fmin, fmax;
    public TMHMMRegion(String key, int fmin, int fmax, Type type) {
        this.key = key;
        this.fmin = fmin;
        this.fmax = fmax;
        this.type = type;
    }
    public String getKey() {
        return key;
    }
    public int getFmin() {
        return fmin;
    }
    public int getFmax() {
        return fmax;
    }
    public Type getType() {
        return type;
    }
    public boolean isTMHelix() {
        return type == Type.TMHELIX;
    }
}

class TMHMMFile {
    private Set<String> keysWithHelices = new HashSet<String> ();
    private Map<String,Collection<TMHMMRegion>> regionsByKey = new HashMap<String,Collection<TMHMMRegion>>();
    public TMHMMFile(InputStream inputStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while (null != (line = reader.readLine())) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\t");
            String key = fields[0];
            TMHMMRegion.Type type = TMHMMRegion.Type.decode(fields[2]);
            String startString = fields[3];
            String stopString = fields[4];

            TMHMMRegion region = new TMHMMRegion(key, Integer.parseInt(startString) - 1, Integer.parseInt(stopString), type);

            if (region.isTMHelix()) {
                keysWithHelices.add(key);
            }

            if (!regionsByKey.containsKey(key)) {
                regionsByKey.put(key, new ArrayList<TMHMMRegion>());
            }
            regionsByKey.get(key).add(region);
        }
    }
    public Set<String> keys() {
        return keysWithHelices;
    }
    public Collection<TMHMMRegion> regionsForKey(String key) {
        return regionsByKey.get(key);
    }
}