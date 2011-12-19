package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.hibernate.Session;

public class LocationLoader extends Loader {

    private static final Logger logger    = Logger.getLogger(LocationLoader.class);
    private String              delimiter = "\t";
    private String              sourceFeatureUniqueName;
    private int                 n         = 0;
    private int                 nGenes    = 0;

    private class GFFFeature {
        Integer fmin;
        Integer fmax;
        Integer phase;
        Short   strand;
        String  id;

        public String toString() {
            return String.format("%s\t%s\t%s\t%s\t%s", id, fmin, fmax, phase, strand);
        }
    }

    @Override
    protected void doLoad(InputStream inputStream, Session session) throws IOException {
        
        Feature sourceFeature = sequenceDao.getFeatureByUniqueName(sourceFeatureUniqueName);
        if (sourceFeature == null) {
            throw new RuntimeException("Could not find sourceFeature " + sourceFeatureUniqueName);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        
        // the GFF may have several lines per ID, pertaining to multiple exons
        LinkedHashMap<String, List<GFFFeature>> features = new LinkedHashMap<String, List<GFFFeature>>();

        while ((line = reader.readLine()) != null) {

            if (line.startsWith("#")) {
                continue;
            }

            if (line.length() > 0 && line.contains(delimiter)) {

                String[] split = line.split(delimiter);

                Integer fmin = Integer.parseInt(split[3]);
                Integer fmax = Integer.parseInt(split[4]);

                Short strand = null;
                if (split[6].equals("-")) {
                    strand = -1;
                } else if (split[6].equals("+")) {
                    strand = 1;
                }

                Integer phase = 0;
                if (!split[7].equals(".")) {
                    phase = Integer.parseInt(split[7]);
                }

                String annotations = split[8];

                String id = null;
                for (String annotation : annotations.split(";")) {
                    String[] kv = annotation.split("=");
                    if (kv[0].equals("ID")) {
                        id = kv[1];
                        break;
                    }
                }

                if (id == null) {
                    throw new RuntimeException("Could not find an ID for this line '" + line + "'");
                }

                GFFFeature gf = new GFFFeature();
                gf.fmin = fmin;
                gf.fmax = fmax;
                gf.strand = strand;
                gf.phase = phase;
                gf.id = id;

                if (!features.containsKey(id)) {
                    features.put(id, new ArrayList<GFFFeature>());
                }

                features.get(id).add(gf);
            }
        }
        
        
        List<String> warnings = new ArrayList<String>();

        for (Entry<String, List<GFFFeature>> entry : features.entrySet()) {
            String id = entry.getKey();
            List<GFFFeature> gffFeatures = entry.getValue();

            Feature feature = sequenceDao.getFeatureByUniqueName(id);

            if (feature == null) {
                logger.debug("This feature " + id + " does not exist... skipping");
                continue;
            }
            
            assert (feature instanceof AbstractGene);

            if (feature.getFeatureLocs() != null && feature.getFeatureLocs().size() > 0) {
                logger.debug("This feature " + id + " has locs... skipping");
                continue;
            }

            assert (feature.getFeatureLocs() == null);

            final int expectedExonCount = gffFeatures.size();
            int exonIndex = 0;

            int minFmin = Integer.MAX_VALUE;
            int maxFmax = 0;

            for (FeatureRelationship rel : feature.getFeatureRelationshipsForObjectId()) {

                Feature mRNA = rel.getSubjectFeature();

                GFFFeature gf = gffFeatures.get(exonIndex);

                assert (gf.id.equals(id));

                if (gf.fmin < minFmin) {
                    minFmin = gf.fmin;
                }
                if (gf.fmax > maxFmax) {
                    maxFmax = gf.fmax;
                }

                loc(session, sourceFeature, gf, mRNA);

                if (mRNA instanceof Transcript) {

                    Transcript transcript = (Transcript) mRNA;

                    Polypeptide p = transcript.getPolypeptide();
                    loc(session, sourceFeature, gf, p);

                    /*
                     * can't use getExons() here: 2011-12-19 15:44:16,295 ERROR
                     * [org.gmod.schema.mapped.Feature] -
                     * <getRankZeroFeatureLoc: Feature 'Tb11.v5.0111.1:exon:1'
                     * has no FeatureLocs> Exception in thread "main"
                     * java.lang.NullPointerException at
                     * org.gmod.schema.feature.Region.loadLoc(Region.java:56) at
                     * org.gmod.schema.feature.Region.compareTo(Region.java:70)
                     * at
                     * org.gmod.schema.feature.Region.compareTo(Region.java:1)
                     * at java.util.TreeMap.put(TreeMap.java:545) at
                     * java.util.TreeSet.add(TreeSet.java:238) at
                     * org.gmod.schema
                     * .feature.Transcript.getComponents(Transcript.java:209) at
                     * org
                     * .gmod.schema.feature.Transcript.getExons(Transcript.java
                     * :218) at
                     * org.genedb.db.loading.auxiliary.LocationLoader.doLoad
                     * (LocationLoader.java:169) at
                     * org.genedb.db.loading.auxiliary
                     * .Loader.load(Loader.java:79) at
                     * org.genedb.db.loading.auxiliary.Load.load(Load.java:141)
                     * at
                     * org.genedb.db.loading.auxiliary.Load.main(Load.java:94)
                     * 
                     * for (AbstractExon exon : transcript.getExons()) {
                     */
                    for (FeatureRelationship relation : transcript.getFeatureRelationshipsForObjectId()) {
                        Feature exon = relation.getSubjectFeature();

                        if (exon instanceof AbstractExon) {

                            // get the next exon location
                            gf = gffFeatures.get(exonIndex);

                            loc(session, sourceFeature, gf, exon);

                            exonIndex++;
                            // logger.info(exonIndex);
                        }

                    }

                }

            }

            // the exonIndex gets incremented above AFTER each exon is found, so
            // you don't need to add 1 to it here
            if (expectedExonCount != exonIndex) {
                warnings.add(String.format("The number of GFF lines for %s is %s, but should should equal the number of exons %s. Manual intervention needed.", feature.getUniqueName(), expectedExonCount, exonIndex));
            }
            
            GFFFeature gf = new GFFFeature();
            gf.fmin = minFmin;
            gf.fmax = maxFmax;
            gf.strand = gffFeatures.get(0).strand;
            gf.phase = gffFeatures.get(0).phase;
            gf.id = id;
            
            loc(session, sourceFeature, gf, feature);
            
            nGenes++;
            
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.flush();
                session.clear();
            }
            n++;
            
        }
        
        for (String warning : warnings) {
            logger.warn(warning);
        }
        
        logger.info("Processed " + nGenes + " genes, and " + n + " features");

        session.flush();
        session.clear();

    }

    private void loc(Session session, Feature sourceFeature, GFFFeature gf, Feature feature) {
        List<FeatureLoc> locs = feature.getFeatureLocs();

        if (locs != null && locs.size() > 0) {
            logger.warn(feature.getUniqueName() + " already had locs, skipping...");
            return;
        }

        logger.info(String.format("%s\t%s", feature.getUniqueName(), gf));

        FeatureLoc loc = new FeatureLoc(sourceFeature, feature, gf.fmin, false, gf.fmax, false, gf.strand, gf.phase, 0, 0);
        session.persist(loc);
        
    }

    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "sourceFeature");
        return options;
    }

    @Override
    protected boolean processOption(String optionName, String optionValue) {

        logger.info(String.format("Setting option: '%s' :: '%s'", optionName, optionValue));

        if (optionName.equals("sourceFeature")) {
            sourceFeatureUniqueName = optionValue;
            return true;
        }

        return false;
    }

}
