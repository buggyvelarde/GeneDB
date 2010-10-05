package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gmod.schema.mapped.Feature;
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
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) { 
            if(line.length() > 0) {
            	try {
            		String[] split = line.split(delimiter);
                	
                	if (split.length < 2) {
                		continue;
                	}
                	
                	String oldUniqueName = split[0];
                	String newUniqueName = split[1];
                	
                	Feature feature = sequenceDao.getFeatureByUniqueName(oldUniqueName, Feature.class);
                	
                	if (oldUniqueName.length() < 1 || newUniqueName.length() < 1) {
                		throw new Exception(String.format("Error on this line : %s", line));
                	}
                	
                	logger.info(String.format("%s -> %s", oldUniqueName, newUniqueName));
                	
                	if (feature != null) {
                		feature.setUniqueName(newUniqueName);
                		session.update(feature);
                		
            		} else {
            			logger.warn(String.format("Could not find %s", oldUniqueName));
            		}
                	
                	
            	} catch (Exception e) {
            		throw new RuntimeException (String.format("Problem (%x) with this line: %s", e.getMessage(), line), e);
            		
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
