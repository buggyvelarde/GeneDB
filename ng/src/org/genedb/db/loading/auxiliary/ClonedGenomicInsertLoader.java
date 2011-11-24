package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gmod.schema.feature.ClonedGenomicInsert;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Organism;
import org.hibernate.Session;

public class ClonedGenomicInsertLoader extends Loader {

    private static final Logger logger    = Logger.getLogger(ClonedGenomicInsertLoader.class);
    private String delimiter = "\t";
    private boolean delete = false;
    private boolean deleteall = false;
    private int n = 0;
    
    private static final String cvterm = "cloned_genomic_insert";
    private static final String cv = "sequence"; 
    
    @Override
    protected void doLoad(InputStream inputStream, Session session) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        
        if (deleteall) {
            logger.info("Deleting all cloned_geenomic_insert features.");
            for (Feature feature : sequenceDao.getFeaturesByCvTermNameAndCvName(cvterm, cv)) {
                logger.info("Deleting " + feature.getUniqueName());
                for (FeatureLoc floc : feature.getFeatureLocs()) {
                    session.delete(floc);
                }
                session.delete(feature);
            }
        }
        
        // to catch any format errors prior to loading, preparse the file
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0 && !line.startsWith("#")) {
                
                String[] split = line.split(delimiter);
                
                String region = split[0];
                String source = split[1];
                String type = split[2];
                
                Integer fmin = Integer.parseInt(split[3]);
                Integer fmax = Integer.parseInt(split[4]);
                
                Integer score = split[5].equals("-") ? null : Integer.parseInt(split[5]);
                Short strand = Short.parseShort(split[6]);
                Integer phase = split[7].equals("-") ? null : Integer.parseInt(split[7]);
                
                String id = split[8].replace("ID=", "");
                
                logger.debug(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t", region, source, type, fmin, fmax, score, strand, phase, id));
                
                if (! type.equals(cvterm)) {
                    logger.warn("Type of feature is not '" + cvterm +"'. Skipping...");
                    continue;
                }
                
                if (delete) {
                    
                    logger.info(String.format("Deleting %s", id));
                    
                    Feature feature = sequenceDao.getFeatureByUniqueName(id);
                    
                    if (feature == null) {
                        logger.warn(String.format("Could not find feature %s. Skipping...", id));
                        continue;
                    }
                    
                    for (FeatureLoc floc : feature.getFeatureLocs()) {
                        session.delete(floc);
                    }
                    
                    session.delete(feature);
                    
                } else {
                    
                    Feature regionFeature = sequenceDao.getFeatureByUniqueName(region);
                    
                    if (regionFeature == null) {
                        logger.warn(String.format("Could not find region feature %s. Skipping...", region));
                        continue;
                    }
                    
                    Organism organism = regionFeature.getOrganism();
                    
                    ClonedGenomicInsert insert = new ClonedGenomicInsert(organism, id, false, false, null);
                    FeatureLoc floc = new FeatureLoc(regionFeature, insert, fmin, false, fmax, false, strand, phase, 0, 0);
                    
                    logger.info(String.format("Creating %s/%s %s-%s", insert.getUniqueName(), regionFeature.getUniqueName(), floc.getFmin(), floc.getFmax()));
                    
                    session.persist(insert);
                    session.persist(floc);
                }
                
                
                
                if (n % 50 == 1) {
                    logger.info("Clearing session");
                    session.flush();
                    session.clear();
                }
                n++;
                
            }
        }

    }
    
    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "deleteall", "delete");
        return options;
    }
    
    @Override
    protected boolean processOption(String optionName, String optionValue) {
        
        logger.info(String.format("Setting option: '%s' :: '%s'", optionName, optionValue));
        
        if (optionName.equals("deleteall")) {
            deleteall = Boolean.parseBoolean(optionValue);
            return true;
        }
        if (optionName.equals("delete")) {
            delete = Boolean.parseBoolean(optionValue);
            return true;
        }
        
        return false;
    }

}
