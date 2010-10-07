package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Synonym;
import org.hibernate.Session;

/**
 * A bulk feature renaming utility. Takes a delimited file as input. First column must contain old names. Second column must contain new names. 
 * 
 * @author gv1
 *
 */
public class RenameFeature extends Loader {
	
	private static final Logger logger = Logger.getLogger(RenameFeature.class);
	
	private String delimiter = "\t";
	
	@Override
	protected void doLoad(InputStream inputStream, Session session)
			throws IOException {
		
        CvTerm synonymType = cvDao.getCvTermByNameAndCvName("previous_systematic_id", "genedb_synonym_type");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        
        while ((line = reader.readLine()) != null) { 
            if(line.length() > 0) {
            	
        		String[] split = line.split(delimiter);
            	
            	if (split.length < 2) {
            		continue;
            	}
            	
            	String oldUniqueName = split[0];
            	String newUniqueName = split[1];
            	
            	Feature feature = sequenceDao.getFeatureByUniqueName(oldUniqueName, Feature.class);
            	
            	if (oldUniqueName.length() < 1 || newUniqueName.length() < 1) {
            		throw new RuntimeException(String.format("Error on this line : %s.", line));
            	}
            	
            	logger.info(String.format("Converting '%s' -> '%s'", oldUniqueName, newUniqueName));
            	
            	if (feature != null) {
            		feature.setUniqueName(newUniqueName);
            		
            		Collection<String> previous = feature.getPreviousSystematicIds();
            		
            		logger.info("Previous Systematic IDS : " + previous);
            		
            		if (! previous.contains(oldUniqueName)) {
            			
            			logger.info("Storing '" + oldUniqueName + "' as a previous systematic ID");
            			
            			Synonym synonym = new Synonym();
            			
            			synonym.setName(oldUniqueName);
            			synonym.setSynonymSGML(oldUniqueName);
            			synonym.setType(synonymType);
            			
            			FeatureSynonym featureSynonym = feature.addSynonym(synonym);
            			
            			// must be set to false for it to be considered a previous systematic id
            			featureSynonym.setCurrent(false);
            			
            			session.persist(synonym);
            			session.persist(featureSynonym);
            			
            		} else {
            			
            			logger.info("Already a previous systematic ID: '" + oldUniqueName + "'");
            			
            		}
            		
            		if (previous.contains(newUniqueName)) {
            			
            			for (FeatureSynonym  featureSynonym: feature.getFeatureSynonyms()) {
            				
            				Synonym synonym = featureSynonym.getSynonym();
            				
            				if (synonym.getName().equals(newUniqueName) && synonym.getType().equals(synonymType)) {
            					
            					logger.warn("Removing '" + newUniqueName + "' from the synonyms list (as it's now the current name)");
            					
            					session.delete(featureSynonym);
            					session.delete(synonym);
            					
            				}
            				
            			}
            			
            		}
            		
            		session.update(feature);
            		logger.info("New name: '" + feature.getUniqueName() + "'");
            		
        		} else {
        			
        			logger.error(String.format("Could not find feature with uniquename '%s'", oldUniqueName));
        			
        		}
                
            }
        }
        
	}
	
	@Override
    protected Set<String> getOptionNames() {
		Set<String> options = new HashSet<String>();
		Collections.addAll(options, "delimiter");
        return options;
    }
	
	@Override
    protected boolean processOption(String optionName, String optionValue) {
		
		if (optionName.equals("delimiter")) {
			delimiter = optionValue;
            return true;
        }
        
        return false;
    }
	
}
