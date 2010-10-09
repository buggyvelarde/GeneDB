package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
	
	/*
	 * Used the following for testing :
	 * 
	 * --cleanup with.... (in my test db, names starting with test are expendables)

		delete from feature where uniquename like 'test%';
		delete from synonym where name like 'test%';
		
		--ready for testing with...
		
		insert into feature (uniquename, organism_id, type_id) values ('test', 213, 792);
		insert into feature (uniquename, organism_id, type_id) values ('test2', 213, 792);
		insert into feature (uniquename, organism_id, type_id) values ('test3', 213, 792);
		insert into feature (uniquename, organism_id, type_id) values ('test4', 213, 792);
		insert into feature (uniquename, organism_id, type_id) values ('testX', 213, 792);
		
		insert into synonym 
			(name, synonym_sgml, type_id)
			values 
			('test4', 'test4', (select cvterm_id from cvterm where name = 'previous_systematic_id'));
		
		insert into feature_synonym 
			(synonym_id,
			feature_id,
			pub_id,
			is_current)
			values
			(
				(select synonym_id from synonym where name = 'test4'),
				(select feature_id from feature where uniquename = 'testX'),
				1,
				false
			);

	 * And two testing text files containing : 
	 * 
		test,test123
		test2,test231
		test3,test333
		test4,test412
	 * 
	 * and (for undo) :
	 * 
		test123,test
		test231,test2
		test333,test3
		test412,test4
	 *
	 */
	
	@Override
	protected void doLoad(InputStream inputStream, Session session)
			throws IOException {
		
        final CvTerm previousSystematicIdType = cvDao.getCvTermByNameAndCvName("previous_systematic_id", "genedb_synonym_type");
        
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
            	
            	if (oldUniqueName.length() < 1 || newUniqueName.length() < 1) {
            		throw new RuntimeException(String.format("Error on this line : %s.", line));
            	}
            	
            	logger.info(String.format("Converting '%s' -> '%s'", oldUniqueName, newUniqueName));
            	
            	Feature feature = sequenceDao.getFeatureByUniqueName(oldUniqueName, Feature.class);
            	
            	if (feature == null) {
            		logger.error(String.format("Could not find feature with uniquename '%s'", oldUniqueName));
            		continue;
            	}
            	
        		feature.setUniqueName(newUniqueName);
        		
        		Collection<String> previous = feature.getPreviousSystematicIds();
        		
        		logger.info("Previous Systematic IDS : " + previous);
        		
        		if (! previous.contains(oldUniqueName)) {
        			
        			logger.info("Storing '" + oldUniqueName + "' as a previous systematic ID");
        			
        			Synonym synonym = getOrCreateSynonym(session, oldUniqueName, previousSystematicIdType);
        			
        			FeatureSynonym featureSynonym = feature.addSynonym(synonym);
        			// must be set to false for it to be considered a previous systematic id
        			featureSynonym.setCurrent(false);
        			session.persist(featureSynonym);
        			
        		} else {
        			
        			logger.info("Already a previous systematic ID: '" + oldUniqueName + "'");
        			
        		}
        		
        		
        		// This is the funny case where the newly supplied name is actually in the list
        		// of previous systematic IDs. We must try to remove it from the list, but only
        		// if we find that it's not used by other features.
        		if (previous.contains(newUniqueName)) { 
        			
        			// loop through all the synonyms
        			for (FeatureSynonym featureSynonym : feature.getFeatureSynonyms()) {
        				
        				Synonym synonym = featureSynonym.getSynonym();
        				
        				// only interested in previous systematic IDs
        				if (! synonym.getType().equals(previousSystematicIdType)) {
        					continue;
        				}
        				
        				// if there is a match, let's see if we can delete it
        				if (synonym.getName().equals(newUniqueName)) {
        					
        					logger.warn("Removing the link between the feature and the synonym (as the synonym is now the current name)");
        					
        					// first delete the feature synonym that links them
        					session.delete(featureSynonym);
        					
        					// We want to check to see if there are any other feature synonyms associated
        					// with this synonym. If there aren't any then it's safe to delete. 
        					// We flush here to make sure the count is correct.  
        					session.flush();
        					
        					if (synonym.getFeatureSynonyms().size() == 0) {
        						
        						logger.warn("Removing '" + newUniqueName + "' from the synonyms list (as it's now the current name, and no other features link to it)");
        						
        						session.delete(synonym);
        					} else {
        						
        						logger.warn("Not deleting the synonym as it's still being used by another feature");
        						
        					}
        					
        				}
        				
        			}
        			
        		}
        		
        		session.update(feature);
        		logger.info("New name: '" + feature.getUniqueName() + "'");
        		
            }
        }
        
	}
	
	/**
	 * Checks to see if a synonym has already been created, returns this if it has, creates a new if it hasn't.
	 * @param session
	 * @param name
	 * @param type
	 * @return a synonym for of the specified type and name.
	 */
	private Synonym getOrCreateSynonym(Session session, String name, CvTerm type) {
		Synonym syn = sequenceDao.getSynonymByNameAndCvTerm(name, type);;
		
		if (syn == null) {
			
			logger.info ("Creating new synonym");
			
			syn = new Synonym();
			syn.setName(name);
			syn.setSynonymSGML(name);
			syn.setType(type);
			session.persist(syn);
		} else {
			logger.info ("Reusing synonym");
		}
		
		return syn;
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
