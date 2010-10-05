package org.genedb.db.loading.auxiliary;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 * 
 * Because chado_load can inject database configurations from the command line, this class makes sure that these properties are used instead of the ones in the property file,
 * when these properties are present.
 * 
 * @author gv1
 *
 */
public class SystemPropertyOverridingDataSource extends BasicDataSource {
	
	 private static final Logger logger = Logger.getLogger(SystemPropertyOverridingDataSource.class);
	
	@Override public void setUrl(String url) {
		
		String dbhost = System.getProperty("dbhost");
		String dbport = System.getProperty("dbport");
		String dbname = System.getProperty("dbname");
		
		if (dbhost != null && dbname != null) {
			
			logger.warn("Overriding url with system property");
			
			if (dbport != null) {
				url = String.format("jdbc:postgresql://%s:%s/%s", dbhost, dbport, dbname);
			} else {
				url = String.format("jdbc:postgresql://%s/%s", dbhost, dbname);				
			}
		}
		
		this.url = url;
		
	}
	
	@Override public void setUsername(String username) {
		
		if (System.getProperty("dbuser") != null) {
			logger.warn("Overriding user with system property");
			username = System.getProperty("dbuser");
		}
		
		this.username = username;
		
	}
	
	@Override public void setPassword(String password) {
		
		if (System.getProperty("dbpassword") != null) {
			logger.warn("Overriding password with system property");
			password = System.getProperty("dbpassword");
		}
		
		this.password = password;
		
	}

}
